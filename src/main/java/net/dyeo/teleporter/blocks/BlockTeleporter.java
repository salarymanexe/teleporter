package net.dyeo.teleporter.blocks;

import java.util.List;
import java.util.Random;

import net.dyeo.teleporter.Reference;
import net.dyeo.teleporter.Teleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleporterEntity;
import net.dyeo.teleporter.capabilities.ITeleporterEntity;
import net.dyeo.teleporter.network.TeleporterNode;
import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.dyeo.teleporter.gui.GuiHandlerTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Standard Teleporter Block Class
// Has a 1-slot container for teleporter key
public class BlockTeleporter extends BlockContainer implements IMetaBlockName
{
	// constructs the block
	public BlockTeleporter(String name)
	{
		// Set material to rock
		super(Material.rock);
		// Set the name of this block
		this.setUnlocalizedName(name);
		// Set transport tab for creative
		this.setCreativeTab(CreativeTabs.tabTransport);
		// resistance: 30
		this.setResistance(30);
		// hardness: 3.0
		this.setHardness(3.0f);
		// light level (emission): 0.5f
		this.setLightLevel(0.5f);
		// block bounds 0-1, 0-1, 0-1
		this.setBlockBounds(0.0f, 0.0f, 0.0f, (float) getBounds().xCoord, (float) getBounds().yCoord,
				(float) getBounds().zCoord);
		// set the default metadata for this block (regular teleporter)
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.REGULAR));
	}

	public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", BlockTeleporter.EnumType.class);

	public enum EnumType implements IStringSerializable
	{
		REGULAR(0, "regular"), ENDER(1, "ender");

		private int ID;
		private String name;

		private EnumType(int ID, String name)
		{
			this.ID = ID;
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public String toString()
		{
			return getName();
		}

		public int getID()
		{
			return ID;
		}
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] { TYPE });
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(TYPE, meta == 0 ? EnumType.REGULAR : EnumType.ENDER);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		EnumType type = state.getValue(TYPE);
		return type.getID();
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
	{
		list.add(new ItemStack(itemIn, 1, 0)); // Meta 0
		list.add(new ItemStack(itemIn, 1, 1)); // Meta 1
	}

	@Override
	public String getSpecialName(ItemStack stack)
	{
		return stack.getItemDamage() == 0 ? EnumType.REGULAR.getName() : EnumType.ENDER.getName();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)));
	}

	// gets a human readable message to be used in the teleporter network
	public ChatComponentTranslation GetMessage(String messageName)
	{
		return new ChatComponentTranslation(
				"message." + Reference.MODID.toLowerCase() + '_' + this.getClass().getSimpleName() + '.' + messageName);
	}

	public static Vec3 getBounds()
	{
		return new Vec3(1.0f, 0.9375f, 1.0f);
	}

	// called when the block is placed or loaded client side to get the tile
	// entity for the block
	// should return a new instance of the tile entity for the block
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		TileEntityTeleporter result = new TileEntityTeleporter();
		return result;
	}

	// called when the block is right clicked
	// in this case it's used to open the teleporter key gui when right-clicked
	// by a player
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumFacing side, float hitX, float hitY, float hitZ)
	{
		// Uses the gui handler registered to open the gui for the given gui id
		// open on the server side only
		if (!worldIn.isRemote && !playerIn.isSneaking())
		{
			playerIn.openGui(Teleporter.instance, GuiHandlerTeleporter.getGuiID(), worldIn, pos.getX(), pos.getY(),
					pos.getZ());
		}

		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
	{
		ITeleporterEntity teleEnt = null;
		teleEnt = entityIn.getCapability(CapabilityTeleporterEntity.INSTANCE, null);
		if (teleEnt != null)
		{
			if (!worldIn.isRemote)
			{
				TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(worldIn, pos);

				if (teleporter != null && teleEnt != null)
				{
					if (teleEnt.getOnTeleporter() && !teleEnt.getTeleported())
					{
						boolean isHostile = (entityIn instanceof EntityMob)
								|| (entityIn instanceof EntityWolf && ((EntityWolf) entityIn).isAngry());

						boolean isPassive = (entityIn instanceof EntityAnimal);

						if ((isHostile == false || isHostile == Reference.teleportHostileMobs)
								&& (isPassive == false || isPassive == Reference.teleportPassiveMobs))
						{
							// Set entity to teleported
							teleEnt.setTeleported(true);

							// Attempt to teleport the entity
							TeleporterNode destinationNode = teleporter.teleport(entityIn);

							// if teleport was successful
							if (destinationNode != null)
							{
								System.out.println("Teleported " + entityIn.getName() + " to "
										+ destinationNode.pos.getX() + "," + destinationNode.pos.getY() + ","
										+ destinationNode.pos.getZ() + " : " + destinationNode.dimension);
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

				worldIn.spawnParticle(EnumParticleTypes.PORTAL,
						pos.getX() + 0.5 + rand.nextFloat() * width * 2.0F - width,
						pos.getY() + 1.5 + rand.nextFloat() * height,
						pos.getZ() + 0.5 + rand.nextFloat() * width * 2.0F - width, mx, my, mz);
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbourBlock)
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
				// there is no way in forge to determine who
				// activated/deactivated the teleporter, so we simply get the
				// closest player
				// works for _most_ cases
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16);
				if (player != null)
				{
					player.addChatMessage(GetMessage("teleporterLocked"));
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
				// there is no way in forge to determine who
				// activated/deactivated the teleporter, so we simply get the
				// closest player
				// works for _most_ cases
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16);
				if (player != null)
				{
					player.addChatMessage(GetMessage("teleporterUnlocked"));
				}
			}
			tileEntityTeleporter.markDirty();
		}
	}

	// drop inventory contents when block is broken
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
		IInventory inventory = worldIn.getTileEntity(pos) instanceof IInventory
				? (IInventory) worldIn.getTileEntity(pos) : null;

		if (inventory != null)
		{
			// For each slot in the inventory
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				// If the slot is not empty
				if (inventory.getStackInSlot(i) != null)
				{
					// Create a new entity item with the item stack in the slot
					EntityItem item = new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							inventory.getStackInSlot(i));

					// Apply some random motion to the item
					float multiplier = 0.1f;
					float motionX = worldIn.rand.nextFloat() - 0.5f;
					float motionY = worldIn.rand.nextFloat() - 0.5f;
					float motionZ = worldIn.rand.nextFloat() - 0.5f;

					item.motionX = motionX * multiplier;
					item.motionY = motionY * multiplier;
					item.motionZ = motionZ * multiplier;

					// Spawn the item in the world
					worldIn.spawnEntityInWorld(item);
				}
			}

			TileEntityTeleporter tileEntityTeleporter = TileEntityTeleporter.getTileEntityAt(worldIn, pos);

			if (tileEntityTeleporter != null)
			{
				tileEntityTeleporter.removeFromNetwork();
			}

			// clear the inventory so nothing else (such as another mod) can do
			// anything with the items
			inventory.clear();
		}

		// super _must_ be called last because it removes the tile entity
		super.breakBlock(worldIn, pos, state);
	}

	// ---------------------------------------------------------

	// the block will render in the CUTOUT layer (allows transparency of glass
	// and such)
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	// used by the renderer to control lighting and visibility of other blocks,
	// also by
	// (eg) wall or fence to control whether the fence joins itself to this
	// block
	@Override
	public boolean isFullCube()
	{
		return true;
	}

	// render using a BakedModel
	// not strictly required because the default (super method) is 3.
	@Override
	public int getRenderType()
	{
		return 3;
	}

}
