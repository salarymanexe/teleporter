package net.dyeo.teleporter.tileentity;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.teleport.TeleporterNetwork;
import net.dyeo.teleporter.teleport.TeleporterNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityTeleporter extends TileEntity implements IInventory
{

	private String customName = null;
	final int NUMBER_OF_SLOTS = 1;
	public ItemStack[] itemStacks = new ItemStack[1];
	boolean firstUpdate = true;
	private boolean isPowered = false;


	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);
		NBTTagList nbtSlots = new NBTTagList();

		for (int i = 0; i < this.itemStacks.length; i++)
		{
			if (this.itemStacks[i] != null)
			{
				NBTTagCompound nbtSlot = new NBTTagCompound();
				nbtSlot.setByte("Slot", (byte)i);
				this.itemStacks[i].writeToNBT(nbtSlot);
				nbtSlots.appendTag(nbtSlot);
			}
		}

		compound.setTag("Items", nbtSlots);
		compound.setBoolean("powered", this.isPowered());
		if (this.hasCustomInventoryName()) compound.setString("CustomName", this.customName);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		NBTTagList nbtSlots = compound.getTagList("Items", NBT.TAG_COMPOUND);
		this.itemStacks = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbtSlots.tagCount(); i++)
		{
			NBTTagCompound nbtSlot = nbtSlots.getCompoundTagAt(i);
			int slotIndex = nbtSlot.getByte("Slot") & 0xFF;
			if ((slotIndex >= 0) && (slotIndex < this.itemStacks.length))
			{
				this.itemStacks[slotIndex] = ItemStack.loadItemStackFromNBT(nbtSlot);
			}
		}

		this.setPowered(compound.getBoolean("powered"));
		if (compound.hasKey("CustomName", NBT.TAG_STRING)) this.customName = compound.getString("CustomName");
	}



	public boolean isPowered()
	{
		return this.isPowered;
	}

	public void setPowered(boolean isPowered)
	{
		this.isPowered = isPowered;
	}

	public void removeFromNetwork()
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(this.worldObj);
		netWrapper.removeNode(this.xCoord, this.yCoord, this.zCoord, this.worldObj.provider.dimensionId);
	}

	@Override
	public void updateEntity()
	{
		if (this.firstUpdate)
		{
			if (!this.worldObj.isRemote)
			{
				this.updateNode();
				this.markDirty();
			}
			this.firstUpdate = false;
		}
	}

	private void updateNode()
	{
		if (!this.worldObj.isRemote)
		{
			boolean isNewNode = false;

			TeleporterNetwork netWrapper = TeleporterNetwork.get(this.worldObj);

			int tileDim = this.worldObj.provider.dimensionId;

			TeleporterNode thisNode = netWrapper.getNode(this.xCoord, this.yCoord, this.zCoord, tileDim);
			if (thisNode == null)
			{
				thisNode = new TeleporterNode();
				isNewNode = true;
			}

			thisNode.x = this.xCoord; thisNode.y = this.yCoord; thisNode.z = this.zCoord;
			thisNode.dimension = tileDim;
			thisNode.type = BlockTeleporter.EnumType.byMetadata(this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));

			if (isNewNode == true)
			{
				netWrapper.addNode(thisNode);
			}

			System.out.println("Node updated : " + thisNode.toString());
		}
	}

//	public String getName()
//	{
//		String unlocalizedName = "tile." + BlockTeleporter.EnumType.byMetadata(this.getBlockMetadata()).getUnlocalizedName() + ".name";
//		return this.hasCustomInventoryName() ? this.customName : unlocalizedName;
//	}


//	public IChatComponent getDisplayName()
//	{
//		return this.hasCustomInventoryName() ? new ChatComponentText(this.getName()) : new ChatComponentTranslation(this.getName(), new Object[0]);
//	}


	// ***** IInventory Members *****

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slotIndex)
	{
		return this.itemStacks[slotIndex];
	}

	@Override
	public ItemStack decrStackSize(int slotIndex, int count)
	{
		ItemStack itemStackInSlot = this.getStackInSlot(slotIndex);
		if (itemStackInSlot == null)
		{
			return null;
		}
		ItemStack itemStackRemoved;
		if (itemStackInSlot.stackSize <= count)
		{
			itemStackRemoved = itemStackInSlot;
			this.setInventorySlotContents(slotIndex, null);
		}
		else
		{
			itemStackRemoved = itemStackInSlot.splitStack(count);
			if (itemStackInSlot.stackSize == 0)
			{
				this.setInventorySlotContents(slotIndex, null);
			}
		}
		this.markDirty();
		return itemStackRemoved;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex)
	{
		ItemStack itemStack = this.getStackInSlot(slotIndex);
		if (itemStack != null)
		{
			this.setInventorySlotContents(slotIndex, null);
		}
		return itemStack;
	}

	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemstack)
	{
		this.itemStacks[slotIndex] = itemstack;
		if ((itemstack != null) && (itemstack.stackSize > this.getInventoryStackLimit()))
		{
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		this.markDirty();
	}

	@Override
	public String getInventoryName()
	{
		String unlocalizedName = "tile." + BlockTeleporter.EnumType.byMetadata(this.getBlockMetadata()).getUnlocalizedName() + ".name";
		return this.hasCustomInventoryName() ? this.customName : unlocalizedName;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return this.customName != null;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this)
		{
			return false;
		}
		double X_CENTRE_OFFSET = 0.5D;
		double Y_CENTRE_OFFSET = 0.5D;
		double Z_CENTRE_OFFSET = 0.5D;
		double MAXIMUM_DISTANCE_SQ = 64.0D;
		return player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) < 64.0D;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
		if (!this.worldObj.isRemote)
		{
			this.updateNode();
			this.markDirty();
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack)
	{
		return true;
	}

	// ***** IInventory Members *****

}
