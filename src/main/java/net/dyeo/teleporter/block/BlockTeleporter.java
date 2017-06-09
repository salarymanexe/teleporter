package net.dyeo.teleporter.block;

import javax.annotation.Nullable;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.init.ModBlocks;
import net.dyeo.teleporter.teleport.TeleporterNode;
import net.dyeo.teleporter.teleport.TeleporterUtility;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;


public class BlockTeleporter extends BlockContainer
{

	public static final AxisAlignedBB TELEPORTER_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, /*0.9375D*/1.0D, 1.0D);
	public static final AxisAlignedBB RECALL_TELEPORTER_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
	
	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", BlockTeleporter.EnumType.class);


	public BlockTeleporter()
	{
		super(Material.ROCK);
		this.setCreativeTab(CreativeTabs.TRANSPORTATION);
		this.setResistance(30.0F);
		this.setHardness(3.0F);
		this.setLightLevel(0.5F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.REGULAR));
	}


	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote && !player.isSneaking())
		{
			player.openGui(TeleporterMod.instance, GuiHandler.GUI_ID_TELEPORTER, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

		if (stack.hasDisplayName())
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileEntityTeleporter)
			{
				((TileEntityTeleporter)tileentity).setCustomName(stack.getDisplayName());
			}
		}
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity)
	{
		TeleporterNode destinationNode = null;
		
		IBlockState state = world.getBlockState(pos);
		EnumType type = EnumType.byMetadata(getMetaFromState(state));
		
		if (entity instanceof EntityLivingBase && entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
		{
			ITeleportHandler teleportHandler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
			if (!world.isRemote)
			{
				if (teleportHandler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
				{
					teleportHandler.setOnTeleporter(entity.getPosition().distanceSq(pos) <= 1);
					teleportHandler.setDimension(entity.dimension);

					if (teleportHandler.getOnTeleporter())
					{
						boolean isHostile = (entity instanceof EntityMob) || (entity instanceof EntityWolf && ((EntityWolf)entity).isAngry());
						boolean isPassive = (entity instanceof EntityAnimal);

						if ((isHostile == false || isHostile == ModConfiguration.teleportHostileMobs) && (isPassive == false || isPassive == ModConfiguration.teleportPassiveMobs))
						{
							destinationNode = TeleporterUtility.teleport((EntityLivingBase)entity, pos);
						}
					}
				}
			}

			if (teleportHandler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
			{
				TileEntityTeleporter tEnt = (TileEntityTeleporter)world.getTileEntity(pos);
				if(tEnt != null)
				{
					tEnt.spawnParticles();
				}
			}
			
			if(type.isRecall() && entity instanceof EntityPlayerMP && destinationNode != null)
			{                
				WorldServer nextWorld = world.getMinecraftServer().worldServerForDimension(destinationNode.dimension);
				breakBlockRecall(world, nextWorld, pos, destinationNode.pos, state, (EntityPlayerMP)entity);
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbourBlock, BlockPos fromPos)
	{
		if (!world.isRemote)
		{
			TileEntityTeleporter tileentity = (TileEntityTeleporter)world.getTileEntity(pos);
			if (tileentity != null)
			{
				boolean isNowPowered = (world.isBlockIndirectlyGettingPowered(pos) > 0);
				boolean isAlreadyPowered = tileentity.isPowered();

				tileentity.setPowered(isNowPowered);

				if (isNowPowered != isAlreadyPowered)
				{
					// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
					// works for _most_ cases
					EntityPlayer player = world.getClosestPlayer(fromPos.getX(), fromPos.getY(), fromPos.getZ(), 32, false);
					if (player != null)
					{
						String translationKey = "message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + (isNowPowered ? "teleporterLocked" : "teleporterUnlocked");
						TextComponentTranslation message = new TextComponentTranslation(translationKey);
						player.sendMessage(message);
					}
				}
				
				tileentity.markDirty();
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntityTeleporter tileEntityTeleporter = (TileEntityTeleporter)world.getTileEntity(pos);
		if (tileEntityTeleporter != null)
		{
			tileEntityTeleporter.removeFromNetwork();

			if (tileEntityTeleporter.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			{
				IItemHandler handler = tileEntityTeleporter.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				ItemStack stack = handler.getStackInSlot(0);
				if (!stack.isEmpty())
				{
					stack = stack.copy();
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}

		// super _must_ be called last because it removes the tile entity
		super.breakBlock(world, pos, state);
	}
	
	public void breakBlockRecall(World world, World nextWorld, BlockPos pos, BlockPos nextPos, IBlockState state, EntityPlayerMP player)
	{
		TileEntityTeleporter tileEntityTeleporter = (TileEntityTeleporter)world.getTileEntity(pos);
		if (tileEntityTeleporter != null)
		{
			tileEntityTeleporter.removeFromNetwork();

			if (tileEntityTeleporter.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			{
				IItemHandler handler = tileEntityTeleporter.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				ItemStack stack = handler.getStackInSlot(0);
				if (!stack.isEmpty())
				{
					InventoryHelper.spawnItemStack(nextWorld, nextPos.getX(), nextPos.getY()+1, nextPos.getZ(), stack);
				}
			}
		}

		InventoryHelper.spawnItemStack(nextWorld, nextPos.getX(), nextPos.getY()+1, nextPos.getZ(), new ItemStack(ModBlocks.TELEPORTER,1,getMetaFromState(state)));
		world.setBlockToAir(pos);
		while(world.getTileEntity(pos) != null)
		{
			world.removeTileEntity(pos);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityTeleporter();
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { TYPE });
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(TYPE, EnumType.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(TYPE).getMetadata();
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return this.getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for ( EnumType type : EnumType.values() )
		{
			list.add(new ItemStack(item, 1, type.getMetadata()));
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		EnumType type = EnumType.byMetadata(getMetaFromState(state));
		return type.isRecall() ? RECALL_TELEPORTER_AABB : TELEPORTER_AABB;
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		EnumType type = EnumType.byMetadata(getMetaFromState(state));
		return type.isRecall() ? RECALL_TELEPORTER_AABB : TELEPORTER_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
    @Override
    public boolean isFullyOpaque(IBlockState state)
    {
        return false;
    }
	
	@Override
	public boolean isFullBlock(IBlockState state)
	{
		EnumType type = EnumType.byMetadata(getMetaFromState(state));
		return !type.isRecall();
	}
    
    @Override
    public boolean isFullCube(IBlockState state)
    {
        EnumType type = EnumType.byMetadata(getMetaFromState(state));
        return !type.isRecall();
    }

	public static enum EnumType implements IStringSerializable
	{
		REGULAR(0, "regular", "teleporter", "teleporter"),
		ENDER(1, "ender", "enderTeleporter", "ender_teleporter"),
		RECALL(2, "recall", "recallTeleporter", "recall_teleporter"),
		RECALL_ENDER(3, "recall_ender", "recallEnderTeleporter", "recall_ender_teleporter");

		private final int meta;
		private final String name;
		private final String unlocalizedName;
		private final String registryName;

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];

		@Override
		public String getName()
		{
			return this.name;
		}

		public int getMetadata()
		{
			return this.meta;
		}
		
		public boolean isEnder()
		{
			return (this.meta & 1) == 1;
		}
		
		public boolean isRecall()
		{
			return (this.meta & 2) == 2;
		}

		public String getUnlocalizedName()
		{
			return this.unlocalizedName;
		}

		public String getRegistryName()
		{
			return this.registryName;
		}

		public static EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		private EnumType(int meta, String name, String unlocalizedName, String registryName)
		{
			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
			this.registryName = registryName;
		}

		static
		{
			for (EnumType value : values())
			{
				META_LOOKUP[value.getMetadata()] = value;
			}
		}

	}
}
