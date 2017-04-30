package net.dyeo.teleporter;

import java.util.Arrays;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityTeleporter extends TileEntity implements IInventory, IUpdatePlayerListBox
{
	final int NUMBER_OF_SLOTS = 1;
	public ItemStack[] itemStacks = new ItemStack[1];
	boolean firstUpdate = true;
	boolean isPowered = false;

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
		ItemStack itemStackInSlot = getStackInSlot(slotIndex);
		if (itemStackInSlot == null)
		{
			return null;
		}
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
		this.itemStacks[slotIndex] = itemstack;
		if ((itemstack != null) && (itemstack.stackSize > getInventoryStackLimit()))
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
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound parentNBTTagCompound)
	{
		super.writeToNBT(parentNBTTagCompound);

		NBTTagList dataForAllSlots = new NBTTagList();
		for (int i = 0; i < this.itemStacks.length; i++)
		{
			if (this.itemStacks[i] != null)
			{
				NBTTagCompound dataForThisSlot = new NBTTagCompound();
				dataForThisSlot.setByte("Slot", (byte)i);
				this.itemStacks[i].writeToNBT(dataForThisSlot);
				dataForAllSlots.appendTag(dataForThisSlot);
			}
		}
		parentNBTTagCompound.setBoolean("Powered", this.isPowered);

		parentNBTTagCompound.setTag("Items", dataForAllSlots);
	}

	@Override
	public void readFromNBT(NBTTagCompound parentNBTTagCompound)
	{
		super.readFromNBT(parentNBTTagCompound);
		byte NBT_TYPE_COMPOUND = 10;
		NBTTagList dataForAllSlots = parentNBTTagCompound.getTagList("Items", 10);

		this.isPowered = parentNBTTagCompound.getBoolean("Powered");

		Arrays.fill(this.itemStacks, null);
		for (int i = 0; i < dataForAllSlots.tagCount(); i++)
		{
			NBTTagCompound dataForOneSlot = dataForAllSlots.getCompoundTagAt(i);
			int slotIndex = dataForOneSlot.getByte("Slot") & 0xFF;
			if ((slotIndex >= 0) && (slotIndex < this.itemStacks.length))
			{
				this.itemStacks[slotIndex] = ItemStack.loadItemStackFromNBT(dataForOneSlot);
			}
		}
	}

	public void clear()
	{
		Arrays.fill(this.itemStacks, null);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex)
	{
		ItemStack itemStack = getStackInSlot(slotIndex);
		if (itemStack != null)
		{
			setInventorySlotContents(slotIndex, null);
		}
		return itemStack;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
		if (!this.worldObj.isRemote)
		{
			boolean isNewNode = false;

			TeleporterNetwork netWrapper = TeleporterNetwork.get(this.worldObj, true);

			int tileDim = this.worldObj.provider.dimensionId;
			TeleporterNode thisNode = netWrapper.getNode(this.xCoord, this.yCoord, this.zCoord, tileDim, true);
			if (thisNode == null)
			{
				thisNode = new TeleporterNode();
				isNewNode = true;
			}
			thisNode.posx = this.xCoord;
			thisNode.posy = this.yCoord;
			thisNode.posz = this.zCoord;
			thisNode.dimension = tileDim;
			if (isNewNode == true)
			{
				netWrapper.addNode(thisNode);
			}
			System.out.println("[Teleporter][closeInventory] Node updated! (" + thisNode.posx + "," + thisNode.posy + "," + thisNode.posz + " @ dim " + thisNode.dimension + ")");
			markDirty();
		}
	}

	public static TileEntityTeleporter getTileEntityAt(World world, int posx, int posy, int posz)
	{
		TileEntity teleEnt = world.getTileEntity(posx, posy, posz);
		if (teleEnt != null)
		{
			if ((teleEnt instanceof TileEntityTeleporter))
			{
				TileEntityTeleporter teleTileEnt = (TileEntityTeleporter)teleEnt;
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
		Vec3 bounds = BlockTeleporter.getBounds();

		TeleporterNetwork netWrapper = TeleporterNetwork.get(this.worldObj, false);
		TeleporterNode source = netWrapper.getNode(this.xCoord, this.yCoord, this.zCoord, this.worldObj.provider.dimensionId, false);
		TeleporterNode destination = netWrapper.getNextNode(entityIn, this.worldObj, this.itemStacks[0], source);
		if ((destination != null) && (entityIn != null))
		{
			if ((entityIn instanceof EntityLivingBase))
			{
				((EntityLivingBase)entityIn).setPositionAndUpdate(destination.posx + bounds.xCoord * 0.5D, destination.posy + (float)bounds.yCoord, destination.posz + bounds.zCoord * 0.5D);

				this.worldObj.playSoundEffect(source.posx, source.posy, source.posz, "teleporter".toLowerCase() + ":portalEnter", 0.9F, 1.0F);
				this.worldObj.playSoundEffect(destination.posx, destination.posy, destination.posz, "teleporter".toLowerCase() + ":portalExit", 0.9F, 1.0F);
			}
		}
		else
		{
			this.worldObj.playSoundEffect(source.posx, source.posy, source.posz, "teleporter".toLowerCase() + ":portalError", 0.9F, 1.0F);
		}
		return destination;
	}

	public void removeFromNetwork()
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(this.worldObj, true);
		netWrapper.removeNode(this.xCoord, this.yCoord, this.zCoord, this.worldObj.provider.dimensionId);
	}

	@Override
	public void updateEntity()
	{
		if (this.firstUpdate)
		{
			if (!this.worldObj.isRemote)
			{
				boolean isNewNode = false;

				TeleporterNetwork netWrapper = TeleporterNetwork.get(this.worldObj, true);

				int tileDim = this.worldObj.provider.dimensionId;
				TeleporterNode thisNode = netWrapper.getNode(this.xCoord, this.yCoord, this.zCoord, tileDim, true);
				if (thisNode == null)
				{
					thisNode = new TeleporterNode();
					isNewNode = true;
				}
				thisNode.posx = this.xCoord;
				thisNode.posy = this.yCoord;
				thisNode.posz = this.zCoord;
				thisNode.dimension = tileDim;
				if (isNewNode == true)
				{
					netWrapper.addNode(thisNode);
				}
				System.out.println("[Teleporter][closeInventory] Node updated! (" + thisNode.posx + "," + thisNode.posy + "," + thisNode.posz + " @ dim " + thisNode.dimension + ")");
				markDirty();
			}
			this.firstUpdate = false;
		}
	}

	@Override
	public String getInventoryName()
	{
		return "Teleporter";
	}

	@Override
	public boolean isCustomInventoryName()
	{
		return false;
	}

	@Override
	public void update()
	{
	}
}
