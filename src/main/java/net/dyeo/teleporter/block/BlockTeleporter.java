package net.dyeo.teleporter.block;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.blockstate.IMetaType;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.utility.TeleporterUtility;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
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
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class BlockTeleporter extends BlockSlab
{
	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", BlockTeleporter.EnumType.class);

	public BlockTeleporter()
	{
		super(Material.ROCK);

		if(!this.isDouble())
		{
			this.setCreativeTab(CreativeTabs.TRANSPORTATION);
		}

		this.setResistance(30.0F);
		this.setHardness(3.0F);
		this.setLightLevel(0.5f);
		this.useNeighborBrightness = !this.isDouble();

		IBlockState blockState = this.blockState.getBaseState();
		blockState = blockState.withProperty(HALF, EnumBlockHalf.BOTTOM);
		blockState = blockState.withProperty(TYPE, EnumType.REGULAR);
		this.setDefaultState(blockState);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			ItemStack item = player.getHeldItem(hand);
			if(!player.isSneaking())
			{
				player.openGui(TeleporterMod.instance, GuiHandler.GUI_ID_TELEPORTER, world, pos.getX(), pos.getY(), pos.getZ());
			}
			else if(hand == EnumHand.MAIN_HAND && item.isEmpty())
			{
				IBlockState blockState = this.blockState.getBaseState()
						.withProperty(HALF, state.getValue(HALF) == EnumBlockHalf.BOTTOM ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM)
						.withProperty(TYPE, state.getValue(TYPE));
				world.setBlockState(pos, blockState);
			}
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
		this.onTeleportEntity(world, pos, entity);
    }

    @Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance)
    {
		this.onTeleportEntity(world, pos, entity);
    }

    public void onTeleportEntity(World world, BlockPos pos, Entity entity)
	{
		if (!world.isRemote)
		{
			if (entity instanceof EntityLivingBase && entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
			{
				ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
				if (handler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
				{
					handler.setOnTeleporter(entity.getPosition().distanceSq(pos) <= 1);
					handler.setDimension(entity.dimension);

					if (handler.getOnTeleporter())
					{
						boolean isHostile = (entity instanceof EntityMob) || (entity instanceof EntityWolf && ((EntityWolf)entity).isAngry());
						boolean isPassive = (entity instanceof EntityAnimal);

						if ((!isHostile || ModConfiguration.teleportHostileMobs) && (!isPassive || ModConfiguration.teleportPassiveMobs))
						{
							TeleporterUtility.teleport((EntityLivingBase)entity, pos);
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
							pos.getZ() + 0.5 + world.rand.nextFloat() * width * 2.0F - width, mx, my, mz
					);
				}
			}
		}
	}

    @SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbourBlock, BlockPos fromPos)
	{
		TileEntityTeleporter tileEntity = (TileEntityTeleporter)world.getTileEntity(pos);
		if (tileEntity != null)
		{
			boolean isNowPowered = (world.isBlockIndirectlyGettingPowered(pos) > 0);
			boolean isAlreadyPowered = tileEntity.isPowered();

			if (!world.isRemote && isNowPowered != isAlreadyPowered)
			{
				tileEntity.setPowered(isNowPowered);
				// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
				// works for _most_ cases
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16, false);
				if (player != null)
				{
					TextComponentTranslation message = TeleporterUtility.getMessage(isNowPowered ? "teleporterLocked" : "teleporterUnlocked");
					player.sendStatusMessage(message, true);
				}
				tileEntity.markDirty();
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

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileEntityTeleporter();
	}

	@Override
	public String getUnlocalizedName(int meta)
	{
		return EnumType.fromMetadata(meta).getUnlocalizedName();
	}

	@Override
	public boolean isDouble()
	{
		return false;
	}

	@Override
	public IProperty<?> getVariantProperty()
	{
		return HALF;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack)
	{
		return EnumType.fromMetadata(stack.getMetadata());
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer.Builder(this)
				.add(HALF)
				.add(TYPE)
				.build();
	}

    @SuppressWarnings("deprecation")
	@Override
	public final IBlockState getStateFromMeta(final int meta)
	{
		IBlockState blockState = this.getDefaultState();
		blockState = blockState.withProperty(HALF, (meta & 0b10) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
		blockState = blockState.withProperty(TYPE, (meta & 0b01) == 0 ? EnumType.REGULAR : EnumType.ENDER);
		return blockState;
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return (state.getValue(HALF).ordinal() << 1) | state.getValue(TYPE).ordinal();
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return this.getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for ( EnumType type : EnumType.values() )
		{
			list.add(new ItemStack(this, 1, type.getMetadata()));
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)) & 0b01);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@SuppressWarnings("deprecation")
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	public static AxisAlignedBB getBoundingBox(IBlockState state)
	{
		return state.getValue(HALF) == EnumBlockHalf.BOTTOM ? AABB_BOTTOM_HALF : AABB_TOP_HALF;
	}

    @SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	public enum EnumType implements IMetaType
	{
		REGULAR(0b00, "regular", "teleporter", "teleporter"),
		ENDER(0b01, "ender", "enderTeleporter", "ender_teleporter");

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

		@Override
		public int getMetadata()
		{
			return this.meta;
		}

		@Override
		public String getUnlocalizedName()
		{
			return this.unlocalizedName;
		}

		@Override
		public String getRegistryName()
		{
			return this.registryName;
		}

		public static EnumType fromMetadata(int meta)
		{
			int temp = meta & 0b01;
			return META_LOOKUP[temp];
		}

		EnumType(int meta, String name, String unlocalizedName, String registryName)
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
