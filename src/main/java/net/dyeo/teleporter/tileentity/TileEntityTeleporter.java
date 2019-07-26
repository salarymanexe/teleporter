package net.dyeo.teleporter.tileentity;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.network.TeleporterMessage;
import net.dyeo.teleporter.world.TeleporterNetwork;
import net.dyeo.teleporter.world.TeleporterNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import scala.Console;

public class TileEntityTeleporter extends TileEntity implements ITickable
{
	private String customName = null;
	private boolean firstUpdate = true;
	private boolean isPowered = false;

	private ItemStackHandler handler = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			TileEntityTeleporter.this.updateNode();
			TileEntityTeleporter.this.markDirty();
		}
	};

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T)this.handler;
		return super.getCapability(capability, facing);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound = super.writeToNBT(compound);
		if (this.hasCustomName()) compound.setString("CustomName", this.customName);
		compound.setBoolean("powered", this.isPowered());
		compound.setTag("Inventory", this.handler.serializeNBT());
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		if (compound.hasKey("CustomName", NBT.TAG_STRING)) this.customName = compound.getString("CustomName");
		this.setPowered(compound.getBoolean("powered"));
		this.handler.deserializeNBT(compound.getCompoundTag("Inventory"));
	}

	public boolean isPowered()
	{
		return this.isPowered;
	}

	public void setPowered(boolean isPowered)
	{
		this.isPowered = isPowered;
		updatePoweredState();
	}

	public String getName()
	{
		String unlocalizedName = "tile." + this.getWorld().getBlockState(this.getPos()).getValue(BlockTeleporter.TYPE).getUnlocalizedName() + ".name";
		return this.hasCustomName() ? this.customName : unlocalizedName;
	}

	public boolean hasCustomName()
	{
		return this.customName != null && !this.customName.isEmpty();
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
	}

	public boolean canInteractWith(EntityPlayer player)
	{
		if (this.world.getTileEntity(this.pos) != this) return false;
		return player.getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) < 64.0;
	}

	public void removeFromNetwork()
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(this.world);
		netWrapper.removeNode(this.pos, this.world.provider.getDimension());
	}

	@Override
	public void update()
	{
		if (this.firstUpdate)
		{
			if (!this.world.isRemote)
			{
				this.updateNode();
				this.markDirty();
			}
			this.firstUpdate = false;
		}
	}

	private void updatePoweredState()
	{
		if(world != null && !world.isRemote)
		{
			TeleporterMod.NETWORK_WRAPPER.sendToAll(new TeleporterMessage(this.pos, this.isPowered));
		}
	}

	private void updateNode()
	{
		if (!this.world.isRemote)
		{
			boolean isNewNode = false;

			TeleporterNetwork netWrapper = TeleporterNetwork.get(this.world);

			int tileDim = this.world.provider.getDimension();

			TeleporterNode thisNode = netWrapper.getNode(this.pos, tileDim);
			if (thisNode == null)
			{
				thisNode = new TeleporterNode();
				isNewNode = true;
			}

			thisNode.pos = this.pos;
			thisNode.dimension = tileDim;
			thisNode.type = this.world.getBlockState(this.pos).getValue(BlockTeleporter.TYPE);

			if (isNewNode)
			{
				netWrapper.addNode(thisNode);
			}

			this.updatePoweredState();
		}
	}

}
