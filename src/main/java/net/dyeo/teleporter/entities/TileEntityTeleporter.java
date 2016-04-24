package net.dyeo.teleporter.entities;

import java.util.Arrays;

import net.dyeo.teleporter.Reference;
import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.blocks.BlockTeleporterBase;
import net.dyeo.teleporter.network.TeleporterNetwork;
import net.dyeo.teleporter.network.TeleporterNode;
import net.dyeo.teleporter.utilities.TeleporterUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityTeleporter extends TileEntity implements IInventory, ITickable
{
	// Create and initialize the items variable that will store the items
	final int NUMBER_OF_SLOTS = 1;
	public ItemStack[] itemStacks = new ItemStack[NUMBER_OF_SLOTS];
	
	boolean firstUpdate = true;
	
	private boolean isPowered = false;
		
	//
	
	public boolean isPowered() {
		return isPowered;
	}

	public void setPowered(boolean isPowered) {
		this.isPowered = isPowered;
	}

	public TileEntityTeleporter()
	{ }
		
	public String getBlockName()
	{
		BlockTeleporterBase block = (BlockTeleporterBase)this.getBlockType();
		return block.getBlockName();
	}
	
	/* The following are some IInventory methods you are required to override */		
	
	// Gets the number of slots in the inventory
	@Override
	public int getSizeInventory() 
	{
		return NUMBER_OF_SLOTS;
	}

	// Gets the stack in the given slot
	@Override
	public ItemStack getStackInSlot(int slotIndex) 
	{
		return itemStacks[slotIndex];
	}

	/**
	 * Removes some of the units from itemstack in the given slot, and returns as a separate itemstack
 	 * @param slotIndex the slot number to remove the items from
	 * @param count the number of units to remove
	 * @return a new itemstack containing the units removed from the slot
	 */
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

	// overwrites the stack in the given slotIndex with the given stack
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

	// this is the maximum number if items allowed in each slot
	// this only affects things such as hoppers trying to insert items you need to use the container to enforce this for players
	// inserting items via the gui
	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	// return true if the given player is able to use this block. In this case it checks that
	// the world tileentity hasn't been replaced in the meantime, and
	// the player isn't too far away from the centre of the block
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if (this.worldObj.getTileEntity(this.pos) != this) return false;
		final double X_CENTRE_OFFSET = 0.5;
		final double Y_CENTRE_OFFSET = 0.5;
		final double Z_CENTRE_OFFSET = 0.5;
		final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
		return player.getDistanceSq(pos.getX() + X_CENTRE_OFFSET, pos.getY() + Y_CENTRE_OFFSET, pos.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
	}

	// return true if the given stack is allowed to go in the given slot.  In this case, we can insert anything.
	// this only affects things such as hoppers trying to insert items you need to use the container to enforce this for players
	// inserting items via the gui
	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack)
	{
		return true;
	}

	// save item stacks and powered state
	@Override
	public void writeToNBT(NBTTagCompound parentNBTTagCompound)
	{
		super.writeToNBT(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location

		// to use an analogy with Java, this code generates an array of hashmaps
		// The itemStack in each slot is converted to an NBTTagCompound, which is effectively a hashmap of key->value pairs such
		//   as slot=1, id=2353, count=1, etc
		// Each of these NBTTagCompound are then inserted into NBTTagList, which is similar to an array.
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
	}

	// This is where you load the data that you saved in writeToNBT
	@Override
	public void readFromNBT(NBTTagCompound parentNBTTagCompound)
	{
		super.readFromNBT(parentNBTTagCompound); // The super call is required to save and load the tiles location
		final byte NBT_TYPE_COMPOUND = 10;       // See NBTBase.createNewByType() for a listing
		NBTTagList dataForAllSlots = parentNBTTagCompound.getTagList("items", NBT_TYPE_COMPOUND);
		
		
		setPowered(parentNBTTagCompound.getBoolean("powered"));
		
		Arrays.fill(itemStacks, null);           // set all slots to empty
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

	// set all slots to empty
	@Override
	public void clear() {
		Arrays.fill(itemStacks, null);
	}

	// will add a key for this container to the lang file so we can name it in the GUI
	@Override
	public String getName() {
		return "tile." + Reference.MODID.toLowerCase() + "_" + getBlockName() + ".name";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	// standard code to look up what the human-readable name is
	@Override
	public IChatComponent getDisplayName() {
		return this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatComponentTranslation(this.getName());
	}

	// -----------------------------------------------------------------------------------------------------------
	// The following methods are not needed for this example but are part of IInventory so they must be implemented

	@Override
	public void openInventory(EntityPlayer player) 
	{
	}

	@Override
	public void closeInventory(EntityPlayer player) 
	{
		if(!worldObj.isRemote)
		{	
			boolean isNewNode = false;
			
			TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);
			
			int tileDim = worldObj.provider.getDimensionId();
			TeleporterNode thisNode = netWrapper.getNode(this.pos, tileDim, true);
			if(thisNode == null)
			{
				thisNode = new TeleporterNode();
				isNewNode = true;
			}
			
			thisNode.pos = this.pos;
			thisNode.dimension = tileDim;
					
			if(isNewNode == true)
			{
				netWrapper.addNode(thisNode);
			}
						
			System.out.println("Node updated! (" + thisNode.pos.getX() + "," + thisNode.pos.getY() + "," + thisNode.pos.getZ() + " @ dim " + thisNode.dimension + ")");
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
		
	public BlockPos getPos()
	{
		return pos;
	}
	
	public static TileEntityTeleporter getTileEntityAt(World world, BlockPos pos)
	{
		TileEntity teleEnt = world.getTileEntity(pos);
		
		if(teleEnt != null)
		{
			TileEntityTeleporter teleTileEnt;
			if(teleEnt instanceof TileEntityTeleporter) 
			{
				teleTileEnt = (TileEntityTeleporter) teleEnt;
				
				if (teleTileEnt != null)
				{
					return teleTileEnt;
				}
			}
		}
		
		return null;		
	}

	// call this when you want an entity to teleport to the next teleporter node
	public TeleporterNode teleport(Entity entityIn) 
	{
		Vec3 bounds = BlockTeleporter.getBounds();
		
		TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, false);
		TeleporterNode source = netWrapper.getNode(pos, worldObj.provider.getDimensionId(), false);
		TeleporterNode destination = netWrapper.getNextNode(entityIn, itemStacks[0], source);
				
		// teleport success variable
		boolean teleportSuccess = false;
		
		// if the destination and entity exist
		if(destination != null && entityIn != null)
		{
			// don't allow cross-dimensional teleportation if the entity is a mount and the destination is another dimension
			if (source.dimension != destination.dimension && entityIn.riddenByEntity != null) 
			{
				return null;
			}
			
			// if the block type for this block is an instance of the basic teleporter block
			if(getBlockType() instanceof BlockTeleporterBase)
			{
				BlockTeleporterBase block = (BlockTeleporterBase)getBlockType();
				
				double x = destination.pos.getX() + (bounds.xCoord*0.5f), 
					   y = destination.pos.getY() + (float)bounds.yCoord, 
					   z = destination.pos.getZ() + (bounds.zCoord*0.5f);
				
				float yaw = entityIn.rotationYaw, pitch = entityIn.rotationPitch;
				
				if(block.getInterdimensional())
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
		
		if(teleportSuccess)
		{
			worldObj.playSoundEffect(source.pos.getX(), source.pos.getY(), source.pos.getZ(), Reference.MODID.toLowerCase() + ":portalEnter", 0.9f, 1.0f);
			worldObj.playSoundEffect(destination.pos.getX(), destination.pos.getY(), destination.pos.getZ(), Reference.MODID.toLowerCase() + ":portalExit", 0.9f, 1.0f);
			System.out.println("Teleport successful.");
			return destination;
		}
		else
		{
			worldObj.playSoundEffect(source.pos.getX(), source.pos.getY(), source.pos.getZ(), Reference.MODID.toLowerCase() + ":portalError", 0.9f, 1.0f);
			System.out.println("Teleport unsuccessful.");
			return source;
		}
		
	}

	public void removeFromNetwork() 
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);
		netWrapper.removeNode(pos, worldObj.provider.getDimensionId());
	}

	@Override
	public void update() {
		
		if(firstUpdate)
		{
			if(!worldObj.isRemote)
			{	
				boolean isNewNode = false;
				
				TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);
				
				int tileDim = worldObj.provider.getDimensionId();
				TeleporterNode thisNode = netWrapper.getNode(this.pos, tileDim, true);
				if(thisNode == null)
				{
					thisNode = new TeleporterNode();
					isNewNode = true;
				}
				
				thisNode.pos = this.pos;
				thisNode.dimension = tileDim;
						
				if(isNewNode == true)
				{
					netWrapper.addNode(thisNode);
				}
							
				System.out.println("Node updated! (" + thisNode.pos.getX() + "," + thisNode.pos.getY() + "," + thisNode.pos.getZ() + " @ dim " + thisNode.dimension + ")");
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