package net.dyeo.teleporter.tileentity;

import java.util.Arrays;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.block.BlockTeleporter.EnumType;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.network.TeleporterNetwork;
import net.dyeo.teleporter.network.TeleporterNode;
import net.dyeo.teleporter.utilities.TeleporterUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class TileEntityTeleporter extends TileEntity implements IInventory, ITickable
{

	private final int NUMBER_OF_SLOTS = 1;
	public ItemStack[] itemStacks = new ItemStack[NUMBER_OF_SLOTS];

	private boolean firstUpdate = true;

	private boolean isPowered = false;


	public TileEntityTeleporter()
	{
	}


	public boolean isPowered()
	{
		return isPowered;
	}

	public void setPowered(boolean isPowered)
	{
		this.isPowered = isPowered;
	}

	public BlockTeleporter.EnumType getTypeProperty()
	{
		IBlockState state = getWorld().getBlockState(getPos());
		return state.getValue(BlockTeleporter.TYPE);
	}

	public boolean getInterdimensional()
	{
		return getTypeProperty() == EnumType.ENDER;
	}


	@Override
	public int getSizeInventory()
	{
		return NUMBER_OF_SLOTS;
	}

	@Override
	public ItemStack getStackInSlot(int slotIndex)
	{
		return itemStacks[slotIndex];
	}

	@Override
	public ItemStack decrStackSize(int slotIndex, int count)
	{
		ItemStack itemStackInSlot = getStackInSlot(slotIndex);
		if (itemStackInSlot == null) return null;

		ItemStack itemStackRemoved;
		if (itemStackInSlot.stackSize <= count)
		{
			itemStackRemoved = itemStackInSlot;
			setInventorySlotContents(slotIndex, null);
		}
		else
		{
			itemStackRemoved = itemStackInSlot.splitStack(count);
			if (itemStackInSlot.stackSize == 0)
			{
				setInventorySlotContents(slotIndex, null);
			}
		}
		markDirty();
		return itemStackRemoved;
	}

	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemstack)
	{
		itemStacks[slotIndex] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		markDirty();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		if (this.worldObj.getTileEntity(this.pos) != this) return false;
		final double X_CENTRE_OFFSET = 0.5;
		final double Y_CENTRE_OFFSET = 0.5;
		final double Z_CENTRE_OFFSET = 0.5;
		final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
		return player.getDistanceSq(pos.getX() + X_CENTRE_OFFSET, pos.getY() + Y_CENTRE_OFFSET,
				pos.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
	}

	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack)
	{
		return true;
	}

	// save item stacks and powered state
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound parentNBTTagCompound)
	{
		parentNBTTagCompound = super.writeToNBT(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location

		// to use an analogy with Java, this code generates an array of hashmaps
		// The itemStack in each slot is converted to an NBTTagCompound, which
		// is effectively a hashmap of key->value pairs such
		// as slot=1, id=2353, count=1, etc
		// Each of these NBTTagCompound are then inserted into NBTTagList, which
		// is similar to an array.
		NBTTagList dataForAllSlots = new NBTTagList();

		for (int i = 0; i < this.itemStacks.length; ++i)
		{
			if (this.itemStacks[i] != null)
			{
				NBTTagCompound dataForThisSlot = new NBTTagCompound();
				dataForThisSlot.setByte("slot", (byte) i);
				this.itemStacks[i].writeToNBT(dataForThisSlot);
				dataForAllSlots.appendTag(dataForThisSlot);
			}
		}

		parentNBTTagCompound.setBoolean("powered", isPowered());

		// the array of hashmaps is then inserted into the parent hashmap for the container
		parentNBTTagCompound.setTag("items", dataForAllSlots);

		return parentNBTTagCompound;
	}

	// This is where you load the data that you saved in writeToNBT
	@Override
	public void readFromNBT(NBTTagCompound parentNBTTagCompound)
	{
		super.readFromNBT(parentNBTTagCompound); // The super call is required
													// to save and load the
													// tiles location
		final byte NBT_TYPE_COMPOUND = 10; // See NBTBase.createNewByType() for
											// a listing
		NBTTagList dataForAllSlots = parentNBTTagCompound.getTagList("items", NBT_TYPE_COMPOUND);

		setPowered(parentNBTTagCompound.getBoolean("powered"));

		Arrays.fill(itemStacks, null); // set all slots to empty
		for (int i = 0; i < dataForAllSlots.tagCount(); ++i)
		{
			NBTTagCompound dataForOneSlot = dataForAllSlots.getCompoundTagAt(i);
			int slotIndex = dataForOneSlot.getByte("slot") & 255;

			if (slotIndex >= 0 && slotIndex < this.itemStacks.length)
			{
				this.itemStacks[slotIndex] = ItemStack.loadItemStackFromNBT(dataForOneSlot);
			}
		}

	}


	@Override
	public String getName()
	{
		return "tile." + getWorld().getBlockState(getPos()).getValue(BlockTeleporter.TYPE).getUnlocalizedName() + ".name";
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
	}


	@Override
	public void clear()
	{
		Arrays.fill(itemStacks, null);
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
		if (!worldObj.isRemote)
		{
			boolean isNewNode = false;

			TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);

			int tileDim = worldObj.provider.getDimension();
			TeleporterNode thisNode = netWrapper.getNode(this.pos, tileDim, true);
			if (thisNode == null)
			{
				thisNode = new TeleporterNode();
				isNewNode = true;
			}

			thisNode.pos = this.pos;
			thisNode.dimension = tileDim;

			if (isNewNode == true)
			{
				netWrapper.addNode(thisNode);
			}

			System.out.println("Node updated! ("
					+ thisNode.pos.getX() + ","
					+ thisNode.pos.getY() + ","
					+ thisNode.pos.getZ() + " @ dim "
					+ thisNode.dimension + ")"
					);
			markDirty();
		}
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public BlockPos getPos()
	{
		return pos;
	}

	public static TileEntityTeleporter getTileEntityAt(World world, BlockPos pos)
	{
		TileEntityTeleporter teleTileEnt = null;
		TileEntity teleEnt = world.getTileEntity(pos);

		if (teleEnt != null)
		{
			if (teleEnt instanceof TileEntityTeleporter)
			{
				teleTileEnt = (TileEntityTeleporter) teleEnt;
			}
		}

		return teleTileEnt;
	}

	// call this when you want an entity to teleport to the next teleporter node
	public TeleporterNode teleport(Entity entityIn)
	{
		Vec3d bounds = BlockTeleporter.getBounds();

		TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, false);
		TeleporterNode source = netWrapper.getNode(pos, worldObj.provider.getDimension(), false);
		TeleporterNode destination = netWrapper.getNextNode(entityIn, itemStacks[0], source);

		// teleport success variable
		boolean teleportSuccess = false;

		// if the destination and entity exist
		if (destination != null && entityIn != null)
		{
			// don't allow cross-dimensional teleportation if the entity is a
			// mount and the destination is another dimension
			if (source.dimension != destination.dimension && !entityIn.getPassengers().isEmpty())
			{
				return null;
			}

			// if the block type for this block is an instance of the basic
			// teleporter block
			if (getBlockType() instanceof BlockTeleporter)
			{
				double x = destination.pos.getX() + (bounds.xCoord * 0.5f);
				double y = destination.pos.getY() + (float)bounds.yCoord;
				double z = destination.pos.getZ() + (bounds.zCoord * 0.5f);

				float yaw = entityIn.rotationYaw;
				float pitch = entityIn.rotationPitch;

				if (getInterdimensional())
				{
					teleportSuccess = TeleporterUtility.transferToDimensionLocation(entityIn, destination.dimension, x, y, z, yaw, pitch);
					// don't allow if the entity is a mount
				}
				else
				{
					teleportSuccess = TeleporterUtility.transferToLocation(entityIn, x, y, z, yaw, pitch);
				}
			}
		}

		if (teleportSuccess)
		{
			worldObj.playSound(null, source.pos.getX(), source.pos.getY(), source.pos.getZ(), ModSounds.PORTAL_ENTER, SoundCategory.BLOCKS, 0.9f, 1.0f);
			worldObj.playSound(null, destination.pos.getX(), destination.pos.getY(), destination.pos.getZ(), ModSounds.PORTAL_EXIT, SoundCategory.BLOCKS, 0.9f, 1.0f);
			System.out.println("Teleport successful.");
			return destination;
		}
		else
		{
			worldObj.playSound(null, source.pos.getX(), source.pos.getY(), source.pos.getZ(), ModSounds.PORTAL_ERROR, SoundCategory.BLOCKS, 0.9f, 1.0f);
			System.out.println("Teleport unsuccessful.");
			return source;
		}

	}

	public void removeFromNetwork()
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);
		netWrapper.removeNode(pos, worldObj.provider.getDimension());
	}

	@Override
	public void update()
	{

		if (firstUpdate)
		{
			if (!worldObj.isRemote)
			{
				boolean isNewNode = false;

				TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);

				int tileDim = worldObj.provider.getDimension();
				TeleporterNode thisNode = netWrapper.getNode(this.pos, tileDim, true);
				if (thisNode == null)
				{
					thisNode = new TeleporterNode();
					isNewNode = true;
				}

				thisNode.pos = this.pos;
				thisNode.dimension = tileDim;

				if (isNewNode == true)
				{
					netWrapper.addNode(thisNode);
				}

				System.out.println("Node updated! (" + thisNode.pos.getX() + "," + thisNode.pos.getY() + ","
						+ thisNode.pos.getZ() + " @ dim " + thisNode.dimension + ")");
				markDirty();
			}
			firstUpdate = false;
		}

	}

	@Override
	public ItemStack removeStackFromSlot(int slotIndex)
	{
		ItemStack itemStack = getStackInSlot(slotIndex);
		if (itemStack != null) setInventorySlotContents(slotIndex, null);
		return itemStack;
	}
}
