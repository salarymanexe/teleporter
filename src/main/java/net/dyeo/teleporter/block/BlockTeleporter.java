package net.dyeo.teleporter.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityHandler;
import net.dyeo.teleporter.capabilities.ITeleporterEntity;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.network.TeleporterNode;
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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class BlockTeleporter extends BlockContainer
{

	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", BlockTeleporter.EnumType.class);


	public BlockTeleporter()
	{
		super(Material.ROCK); // Set material to rock
		this.setCreativeTab(CreativeTabs.TRANSPORTATION); // Set transport tab for creative
		this.setResistance(30.0F); // resistance: 30
		this.setHardness(3.0F); // hardness: 3.0
		this.setLightLevel(0.5F); // light level (emission): 0.5f
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.REGULAR)); // set the default metadata for this block (regular teleporter)
	}


	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { TYPE });
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(TYPE, meta == 0 ? EnumType.REGULAR : EnumType.ENDER);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(TYPE).getMetadata();
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
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
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)));
	}

	public static Vec3d getBounds()
	{
		return new Vec3d(1.0, 0.9375, 1.0);
	}

    @Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return new AxisAlignedBB(0.0F, 0.0F, 0.0F, getBounds().xCoord, getBounds().yCoord, getBounds().zCoord);
    }


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		TileEntityTeleporter result = new TileEntityTeleporter();
		return result;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote && !player.isSneaking())
		{
			player.openGui(TeleporterMod.instance, GuiHandler.GUI_ID_TELEPORTER, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		ITeleporterEntity teleEnt = null;
		teleEnt = entity.getCapability(CapabilityHandler.TELEPORT_CAPABILITY, null);
		if (teleEnt != null)
		{
			if (!world.isRemote)
			{
				TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(world, pos);

				if (teleporter != null && teleEnt != null)
				{
					if (teleEnt.getOnTeleporter() && !teleEnt.getTeleported())
					{
						boolean isHostile = (entity instanceof EntityMob) || (entity instanceof EntityWolf && ((EntityWolf)entity).isAngry());
						boolean isPassive = (entity instanceof EntityAnimal);

						if ((isHostile == false || isHostile == ModConfiguration.teleportHostileMobs) && (isPassive == false || isPassive == ModConfiguration.teleportPassiveMobs))
						{
							teleEnt.setTeleported(true); // Set entity to teleported
							TeleporterNode destinationNode = teleporter.teleport(entity); // Attempt to teleport the entity
							if (destinationNode != null) // if teleport was successful
							{
								System.out.println("Teleported " + entity.getName() + " to " + destinationNode.pos.getX() + "," + destinationNode.pos.getY() + "," + destinationNode.pos.getZ() + " : " + destinationNode.dimension);
							}
						}
					}
				}
			}

			if (!teleEnt.getTeleported())
			{
				double width = 0.25;
				double height = 0.25;

				Random rand = new Random();

				double mx = rand.nextGaussian() * 0.2d;
				double my = rand.nextGaussian() * 0.2d;
				double mz = rand.nextGaussian() * 0.2d;

				world.spawnParticle(EnumParticleTypes.PORTAL,
					pos.getX() + 0.5 + rand.nextFloat() * width * 2.0F - width,
					pos.getY() + 1.5 + rand.nextFloat() * height,
					pos.getZ() + 0.5 + rand.nextFloat() * width * 2.0F - width, mx, my, mz
				);
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbourBlock)
	{
		// get the tile entity at this block's world pos
		TileEntityTeleporter tileEntityTeleporter = TileEntityTeleporter.getTileEntityAt(world, pos);

		// check if teleporter was powered before
		boolean oldPowered = tileEntityTeleporter.isPowered();

		if (!world.isRemote && world.isBlockIndirectlyGettingPowered(pos) > 0 && tileEntityTeleporter != null)
		{
			tileEntityTeleporter.setPowered(true);
			// if block was not powered before but is now powered
			if (oldPowered == false)
			{
				// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
				// works for _most_ cases
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16, false);
				if (player != null)
				{
					TextComponentTranslation message = new TextComponentTranslation("message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + "teleporterLocked");
					player.addChatMessage(message);
				}
			}
			tileEntityTeleporter.markDirty();
		}
		else if (tileEntityTeleporter != null)
		{
			tileEntityTeleporter.setPowered(false);
			// if block was powered before but is now not powered
			if (oldPowered == true)
			{
				// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
				// works for _most_ cases
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16, false);
				if (player != null)
				{
					TextComponentTranslation message = new TextComponentTranslation("message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + "teleporterUnlocked");
					player.addChatMessage(message);
				}
			}
			tileEntityTeleporter.markDirty();
		}
	}

	// drop inventory contents when block is broken
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		IInventory inventory = world.getTileEntity(pos) instanceof IInventory ? (IInventory) world.getTileEntity(pos) : null;

		if (inventory != null)
		{
			// For each slot in the inventory
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				// If the slot is not empty
				if (inventory.getStackInSlot(i) != null)
				{
					// Create a new entity item with the item stack in the slot
					EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory.getStackInSlot(i));

					// Apply some random motion to the item
					float multiplier = 0.1f;
					float motionX = world.rand.nextFloat() - 0.5f;
					float motionY = world.rand.nextFloat() - 0.5f;
					float motionZ = world.rand.nextFloat() - 0.5f;

					item.motionX = motionX * multiplier;
					item.motionY = motionY * multiplier;
					item.motionZ = motionZ * multiplier;

					// Spawn the item in the world
					world.spawnEntityInWorld(item);
				}
			}

			TileEntityTeleporter tileEntityTeleporter = TileEntityTeleporter.getTileEntityAt(world, pos);

			if (tileEntityTeleporter != null)
			{
				tileEntityTeleporter.removeFromNetwork();
			}

			// clear the inventory so nothing else (such as another mod) can do anything with the items
			inventory.clear();
		}

		// super _must_ be called last because it removes the tile entity
		super.breakBlock(world, pos, state);
	}

	// ---------------------------------------------------------

	// the block will render in the CUTOUT layer (allows transparency of glass and such)
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	// used by the renderer to control lighting and visibility of other blocks, also by (eg) wall or fence to control whether the fence joins itself to this block
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return true;
	}

	// render using a BakedModel
	// not strictly required because the default (super method) is 3.
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}


	public static enum EnumType implements IStringSerializable
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

		public int getMetadata()
		{
			return this.meta;
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
