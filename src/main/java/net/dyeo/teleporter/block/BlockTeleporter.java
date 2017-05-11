package net.dyeo.teleporter.block;

import java.util.List;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.inventory.InventoryHelper;
import net.dyeo.teleporter.teleport.TeleporterNode;
import net.dyeo.teleporter.teleport.TeleporterUtility;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;


public class BlockTeleporter extends BlockContainer
{

	public static final AxisAlignedBB TELEPORTER_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);
	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", BlockTeleporter.EnumType.class);


	public BlockTeleporter()
	{
		super(Material.rock);
		this.setCreativeTab(CreativeTabs.tabTransport);
		this.setResistance(30);
		this.setHardness(3.0f);
		this.setLightLevel(0.5f);
		this.setBlockBounds(0.0f, 0.0f, 0.0f, (float)TELEPORTER_AABB.maxX, (float)TELEPORTER_AABB.maxY, (float)TELEPORTER_AABB.maxZ);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.REGULAR));
	}


	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
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
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		if (entity instanceof EntityLivingBase && entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
		{
			ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
			if (!world.isRemote)
			{
				if (handler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
				{
					handler.setOnTeleporter(entity.getPosition().distanceSq(pos) <= 1);
					handler.setDimension(entity.dimension);

					if (handler.getOnTeleporter())
					{
						boolean isHostile = (entity instanceof EntityMob) || (entity instanceof EntityWolf && ((EntityWolf) entity).isAngry());
						boolean isPassive = (entity instanceof EntityAnimal);

						if ((isHostile == false || isHostile == ModConfiguration.teleportHostileMobs) && (isPassive == false || isPassive == ModConfiguration.teleportPassiveMobs))
						{
							TeleporterNode destinationNode = TeleporterUtility.teleport((EntityLivingBase)entity, pos);
						}
					}
				}
			}

			if (handler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
			{
				double width = 0.25;
				double height = 0.25;

				double mx = world.rand.nextGaussian() * 0.2d;
				double my = world.rand.nextGaussian() * 0.2d;
				double mz = world.rand.nextGaussian() * 0.2d;

				world.spawnParticle(EnumParticleTypes.PORTAL,
					pos.getX() + 0.5 + world.rand.nextFloat() * width * 2.0F - width,
					pos.getY() + 1.5 + world.rand.nextFloat() * height,
					pos.getZ() + 0.5 + world.rand.nextFloat() * width * 2.0F - width,
					mx, my, mz
				);
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbourBlock)
	{
		if (!world.isRemote)
		{
			TileEntityTeleporter tileEntity = (TileEntityTeleporter)world.getTileEntity(pos);
			if (tileEntity != null)
			{
				boolean isNowPowered = (world.isBlockIndirectlyGettingPowered(pos) > 0);
				boolean isAlreadyPowered = tileEntity.isPowered();

				tileEntity.setPowered(isNowPowered);

				if (isNowPowered != isAlreadyPowered)
				{
					// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
					// works for _most_ cases
					EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16);
					if (player != null)
					{
						String translationKey = "message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + (isNowPowered ? "teleporterLocked" : "teleporterUnlocked");
						ChatComponentTranslation message = new ChatComponentTranslation(translationKey);
						player.addChatMessage(message);
					}
					tileEntity.markDirty();
				}
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntityTeleporter tileEntity = (TileEntityTeleporter)world.getTileEntity(pos);
		if (tileEntity != null)
		{
			tileEntity.removeFromNetwork();

			if (tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			{
				IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				ItemStack stack = handler.getStackInSlot(0);
				if (stack != null)
				{
					stack = stack.copy();
					InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}

		// super _must_ be called last because it removes the tile entity
		super.breakBlock(world, pos, state);
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		TileEntityTeleporter result = new TileEntityTeleporter();
		return result;
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] { TYPE });
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(TYPE, meta == 0 ? EnumType.REGULAR : EnumType.ENDER);
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
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
	{
		for ( EnumType type : EnumType.values() )
		{
			list.add(new ItemStack(item, 1, type.getMetadata()));
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)));
	}


	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public int getRenderType()
	{
		return 3;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isFullCube()
	{
		return true;
	}



	public enum EnumType implements IStringSerializable
	{
		REGULAR(0, "regular", "teleporterBlock"),
		ENDER(1, "ender", "enderTeleporterBlock");

		private final int meta;
		private final String name;
		private final String unlocalizedName;

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

		public String getUnlocalizedName()
		{
			return this.unlocalizedName;
		}

		public static EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		private EnumType(int meta, String name, String unlocalizedName)
		{
			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
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
