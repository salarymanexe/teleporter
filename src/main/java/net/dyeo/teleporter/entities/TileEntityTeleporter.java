package net.dyeo.teleporter.entities;

import java.util.Arrays;
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
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityTeleporter extends TileEntity implements IInventory, IUpdatePlayerListBox
{
	final int NUMBER_OF_SLOTS = 1;
	public ItemStack[] itemStacks = new ItemStack[1];
	boolean firstUpdate = true;
	private boolean isPowered = false;

	public boolean isPowered()
	{
		return this.isPowered;
	}

	public void setPowered(boolean isPowered)
	{
		this.isPowered = isPowered;
	}

	public String getBlockName()
	{
		BlockTeleporterBase block = (BlockTeleporterBase)this.getBlockType();
		return block.getBlockName();
	}

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
				dataForThisSlot.setByte("slot", (byte)i);
				this.itemStacks[i].writeToNBT(dataForThisSlot);
				dataForAllSlots.appendTag(dataForThisSlot);
			}
		}
		parentNBTTagCompound.setBoolean("powered", this.isPowered());

		parentNBTTagCompound.setTag("items", dataForAllSlots);
	}

	@Override
	public void readFromNBT(NBTTagCompound parentNBTTagCompound)
	{
		super.readFromNBT(parentNBTTagCompound);
		byte NBT_TYPE_COMPOUND = 10;
		NBTTagList dataForAllSlots = parentNBTTagCompound.getTagList("items", 10);

		this.setPowered(parentNBTTagCompound.getBoolean("powered"));

		Arrays.fill(this.itemStacks, null);
		for (int i = 0; i < dataForAllSlots.tagCount(); i++)
		{
			NBTTagCompound dataForOneSlot = dataForAllSlots.getCompoundTagAt(i);
			int slotIndex = dataForOneSlot.getByte("slot") & 0xFF;
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

	public String getName()
	{
		return "tile." + "teleporter".toLowerCase() + "_" + this.getBlockName() + ".name";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	public IChatComponent getDisplayName()
	{
		return this.hasCustomInventoryName() ? new ChatComponentText(this.getName()) : new ChatComponentTranslation(this.getName(), new Object[0]);
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
		TeleporterNode destination = netWrapper.getNextNode(entityIn, this.itemStacks[0], source);

		boolean teleportSuccess = false;
		if ((destination != null) && (entityIn != null))
		{
			if ((source.dimension != destination.dimension) && (entityIn.riddenByEntity != null))
			{
				return null;
			}
			if ((this.getBlockType() instanceof BlockTeleporterBase))
			{
				BlockTeleporterBase block = (BlockTeleporterBase)this.getBlockType();

				double x = destination.posx + bounds.xCoord * 0.5D;
				double y = destination.posy + (float)bounds.yCoord;
				double z = destination.posz + bounds.zCoord * 0.5D;

				float yaw = entityIn.rotationYaw;
				float pitch = entityIn.rotationPitch;
				if (block.getInterdimensional())
				{
					teleportSuccess = TeleporterUtility.transferToDimensionLocation(entityIn, destination.dimension, x, y, z, yaw, pitch);
				}
				else
				{
					teleportSuccess = TeleporterUtility.transferToLocation(entityIn, x, y, z, yaw, pitch);
				}
			}
		}
		if (teleportSuccess)
		{
			this.worldObj.playSoundEffect(source.posx, source.posy, source.posz, "teleporter".toLowerCase() + ":portalEnter", 0.9F, 1.0F);
			this.worldObj.playSoundEffect(destination.posx, destination.posy, destination.posz, "teleporter".toLowerCase() + ":portalExit", 0.9F, 1.0F);
			System.out.println("Teleport successful.");
			return destination;
		}
		this.worldObj.playSoundEffect(source.posx, source.posy, source.posz, "teleporter".toLowerCase() + ":portalError", 0.9F, 1.0F);
		System.out.println("Teleport unsuccessful.");
		return source;
	}

	public void removeFromNetwork()
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(this.worldObj, true);
		netWrapper.removeNode(this.xCoord, this.yCoord, this.zCoord, this.worldObj.provider.dimensionId);
	}

	@Override
	public void update()
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
				System.out.println("Node updated! (" + thisNode.posx + "," + thisNode.posy + "," + thisNode.posz + " @ dim " + thisNode.dimension + ")");
				this.markDirty();
			}
			this.firstUpdate = false;
		}
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
	public String getInventoryName()
	{
		return null;
	}

	public boolean isCustomInventoryName()
	{
		return false;
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
			System.out.println("Node updated! (" + thisNode.posx + "," + thisNode.posy + "," + thisNode.posz + " @ dim " + thisNode.dimension + ")");
			this.markDirty();
		}
	}
}
