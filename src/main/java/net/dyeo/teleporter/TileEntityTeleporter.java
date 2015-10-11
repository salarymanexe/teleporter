package net.dyeo.teleporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTeleporter extends TileEntity implements IInventory, IUpdatePlayerListBox
{
	// Create and initialize the items variable that will store the items
	final int NUMBER_OF_SLOTS = 1;
	public ItemStack[] itemStacks = new ItemStack[NUMBER_OF_SLOTS];
	
	boolean firstUpdate = true;
	
	boolean isPowered = false;
		
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
	public ItemStack decrStackSize(int slotIndex, int count) {
		ItemStack itemStackInSlot = getStackInSlot(slotIndex);
		if (itemStackInSlot == null) return null;

		ItemStack itemStackRemoved;
		if (itemStackInSlot.stackSize <= count) {
			itemStackRemoved = itemStackInSlot;
			setInventorySlotContents(slotIndex, null);
		} else {
			itemStackRemoved = itemStackInSlot.splitStack(count);
			if (itemStackInSlot.stackSize == 0) {
				setInventorySlotContents(slotIndex, null);
			}
		}
		markDirty();
		return itemStackRemoved;
	}

	// overwrites the stack in the given slotIndex with the given stack
	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemstack) {
		itemStacks[slotIndex] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
		}
		markDirty();
	}

	// This is the maximum number if items allowed in each slot
	// This only affects things such as hoppers trying to insert items you need to use the container to enforce this for players
	// inserting items via the gui
	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	// Return true if the given player is able to use this block. In this case it checks that
	// 1) the world tileentity hasn't been replaced in the meantime, and
	// 2) the player isn't too far away from the centre of the block
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if (this.worldObj.getTileEntity(this.pos) != this) return false;
		final double X_CENTRE_OFFSET = 0.5;
		final double Y_CENTRE_OFFSET = 0.5;
		final double Z_CENTRE_OFFSET = 0.5;
		final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
		return player.getDistanceSq(pos.getX() + X_CENTRE_OFFSET, pos.getY() + Y_CENTRE_OFFSET, pos.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
	}

	// Return true if the given stack is allowed to go in the given slot.  In this case, we can insert anything.
	// This only affects things such as hoppers trying to insert items you need to use the container to enforce this for players
	// inserting items via the gui
	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack) {
		return true;
	}

	// This is where you save any data that you don't want to lose when the tile entity unloads
	// In this case, it saves the itemstacks stored in the container
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
				dataForThisSlot.setByte("Slot", (byte) i);
				this.itemStacks[i].writeToNBT(dataForThisSlot);
				dataForAllSlots.appendTag(dataForThisSlot);
			}
		}
		
		parentNBTTagCompound.setBoolean("Powered", isPowered);
				
		// the array of hashmaps is then inserted into the parent hashmap for the container
		parentNBTTagCompound.setTag("Items", dataForAllSlots);
	}

	// This is where you load the data that you saved in writeToNBT
	@Override
	public void readFromNBT(NBTTagCompound parentNBTTagCompound)
	{
		super.readFromNBT(parentNBTTagCompound); // The super call is required to save and load the tiles location
		final byte NBT_TYPE_COMPOUND = 10;       // See NBTBase.createNewByType() for a listing
		NBTTagList dataForAllSlots = parentNBTTagCompound.getTagList("Items", NBT_TYPE_COMPOUND);
		
		isPowered = parentNBTTagCompound.getBoolean("Powered");
		
		Arrays.fill(itemStacks, null);           // set all slots to empty
		for (int i = 0; i < dataForAllSlots.tagCount(); ++i) 
		{
			NBTTagCompound dataForOneSlot = dataForAllSlots.getCompoundTagAt(i);
			int slotIndex = dataForOneSlot.getByte("Slot") & 255;
			

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
		return "tile." + Reference.MODID.toLowerCase() + "_teleporterBlock.name";
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

	/**
	 * This method removes the entire contents of the given slot and returns it.
	 * Used by containers such as crafting tables which return any items in their slots when you close the GUI
	 * @param slotIndex
	 * @return
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex) {
		ItemStack itemStack = getStackInSlot(slotIndex);
		if (itemStack != null) setInventorySlotContents(slotIndex, null);
		return itemStack;
	}

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
						
			System.out.println("[Teleporter][closeInventory] Node updated! (" + thisNode.pos.getX() + "," + thisNode.pos.getY() + "," + thisNode.pos.getZ() + " @ dim " + thisNode.dimension + ")");
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

	public TeleporterNode teleport(Entity entityIn) 
	{
		//System.out.println("[Teleporter] Teleported " + entityIn.getName() + " to ");
		
		Vec3 bounds = BlockTeleporter.getBounds();
		
		TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, false);
		TeleporterNode source = netWrapper.getNode(pos, worldObj.provider.getDimensionId(), false);
		TeleporterNode destination = netWrapper.getNextNode(entityIn, worldObj, itemStacks[0], source);
		
		if(destination != null && entityIn != null)
		{
			entityIn.setPositionAndUpdate(destination.pos.getX() + (bounds.xCoord*0.5f), destination.pos.getY() + (float)bounds.yCoord, destination.pos.getZ() + (bounds.zCoord*0.5f));
			
			worldObj.playSoundEffect(source.pos.getX(), source.pos.getY(), source.pos.getZ(), Reference.MODID.toLowerCase() + ":portalEnter", 0.9f, 1.0f);
			worldObj.playSoundEffect(destination.pos.getX(), destination.pos.getY(), destination.pos.getZ(), Reference.MODID.toLowerCase() + ":portalExit", 0.9f, 1.0f);
					
		}
		else
		{
			worldObj.playSoundEffect(source.pos.getX(), source.pos.getY(), source.pos.getZ(), Reference.MODID.toLowerCase() + ":portalError", 0.9f, 1.0f);
		}
		
		return destination;
		
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
							
				System.out.println("[Teleporter][closeInventory] Node updated! (" + thisNode.pos.getX() + "," + thisNode.pos.getY() + "," + thisNode.pos.getZ() + " @ dim " + thisNode.dimension + ")");
				markDirty();
			}
			firstUpdate = false;
		}
		
	}
}