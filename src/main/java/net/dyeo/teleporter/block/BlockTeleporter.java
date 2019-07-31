package net.dyeo.teleporter.block;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.blockstate.IMetaType;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.utility.TeleporterUtility;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class BlockTeleporter extends BlockSlab
{
	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class);
	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", BlockTeleporter.EnumType.class);

	public static final byte FACING_MASK = 0b1100;
	public static final byte HALF_MASK 	= 0b0010;
	public static final byte TYPE_MASK 	= 0b0001;

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

		IBlockState blockState = this.blockState.getBaseState()
			.withProperty(FACING, EnumFacing.NORTH)
			.withProperty(HALF, EnumBlockHalf.BOTTOM)
			.withProperty(TYPE, EnumType.REGULAR);
		this.setDefaultState(blockState);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ)
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
						.withProperty(FACING, state.getValue(FACING))
						.withProperty(HALF, state.getValue(HALF) == EnumBlockHalf.BOTTOM ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM)
						.withProperty(TYPE, state.getValue(TYPE));
				world.setBlockState(pos, blockState);
			}
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, placer, stack);

		if (stack.hasDisplayName())
		{
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof TileEntityTeleporter)
			{
				((TileEntityTeleporter)tileentity).setCustomName(stack.getDisplayName());
			}
		}
	}

    @Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity)
    {
        TeleporterUtility.tryTeleport(world, pos, entity);
    }

    @Override
	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance)
    {
		TeleporterUtility.tryTeleport(world, pos, entity);
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
		return EnumType.fromMetadata(meta & TYPE_MASK).getUnlocalizedName();
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
		return EnumType.fromMetadata(stack.getMetadata() & TYPE_MASK);
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer.Builder(this)
				.add(FACING)
				.add(HALF)
				.add(TYPE)
				.build();
	}

    @SuppressWarnings("deprecation")
	@Override
	public final IBlockState getStateFromMeta(final int meta)
	{
		IBlockState blockState = this.getDefaultState()
				.withProperty(FACING, EnumFacing.fromMetadata((meta & FACING_MASK) >> 2))
				.withProperty(HALF, ((meta & HALF_MASK) >> 1) == 0 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM)
				.withProperty(TYPE, (meta & TYPE_MASK) == 0 ? EnumType.REGULAR : EnumType.ENDER);
		return blockState;
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return (state.getValue(FACING).getMetadata() << 2)
				| (state.getValue(HALF).ordinal() << 1)
				| state.getValue(TYPE).ordinal();
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return this.getMetaFromState(state) & TYPE_MASK;
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
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)) & TYPE_MASK);
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
		return state.getValue(HALF) == EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
	}

    @SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
					.withProperty(FACING, EnumFacing.fromMetadata(placer.getHorizontalFacing().getOpposite().getHorizontalIndex()));
	}

	public enum EnumFacing implements IStringSerializable
	{
		SOUTH(0, "south", 0.0f),
		WEST(1, "west" , 90.0f),
		NORTH(2, "north", 180.0f),
		EAST(3, "east", 270.0f);

		private final int meta;
		private final String name;
		private final float yaw;

		private static final EnumFacing[] META_LOOKUP = new EnumFacing[values().length];

		EnumFacing(int meta, String name, float yaw)
		{
			this.meta = meta;
			this.name = name;
			this.yaw = yaw;
		}

		static
		{
			for (EnumFacing value : values())
			{
				META_LOOKUP[value.getMetadata()] = value;
			}
		}

		public static EnumFacing fromMetadata(int meta)
		{
			return META_LOOKUP[meta];
		}

		public int getMetadata()
		{
			return this.meta;
		}

		@Override
		public String getName()
		{
			return name;
		}

		public float getYaw()
		{
			return yaw;
		}
	}

	public enum EnumType implements IMetaType
	{
		REGULAR(0, "regular", "teleporter", "teleporter"),
		ENDER(1, "ender", "enderTeleporter", "ender_teleporter");

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
			return META_LOOKUP[meta];
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
