package net.dyeo.teleporter.capabilities;

import net.minecraft.nbt.NBTTagCompound;

public class TeleportHandler implements ITeleportHandler
{
	private boolean onTeleporter = false;
	private EnumTeleportStatus teleportStatus = EnumTeleportStatus.INACTIVE;
	private int dimension = 0;

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("onTeleporter", this.onTeleporter);
		nbt.setInteger("teleportStatus", this.teleportStatus.ordinal());
		nbt.setInteger("dimension", this.dimension);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		this.onTeleporter = nbt.getBoolean("onTeleporter");
		this.teleportStatus = EnumTeleportStatus.values()[nbt.getInteger("teleportStatus")];
		this.dimension = nbt.getInteger("dimension");
	}

	@Override
	public boolean getOnTeleporter()
	{
		return this.onTeleporter;
	}

	@Override
	public EnumTeleportStatus getTeleportStatus()
	{
		return this.teleportStatus;
	}

	@Override
	public int getDimension()
	{
		return this.dimension;
	}

	@Override
	public void setOnTeleporter(boolean value)
	{
		this.onTeleporter = value;
	}

	@Override
	public void setTeleportStatus(EnumTeleportStatus value)
	{
		this.teleportStatus = value;
	}

	@Override
	public void setDimension(int value)
	{
		this.dimension = value;
	}

}
