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
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("onTeleporter", this.onTeleporter);
		compound.setInteger("teleportStatus", this.teleportStatus.ordinal());
		compound.setInteger("dimension", this.dimension);
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound)
	{
		this.onTeleporter = compound.getBoolean("onTeleporter");
		this.teleportStatus = EnumTeleportStatus.values()[compound.getInteger("teleportStatus")];
		this.dimension = compound.getInteger("dimension");
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
