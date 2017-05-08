package net.dyeo.teleporter.capabilities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ITeleportHandler extends INBTSerializable<NBTTagCompound>
{

	boolean getOnTeleporter();

	EnumTeleportStatus getTeleportStatus();

	int getDimension();

	void setOnTeleporter(boolean value);

	void setTeleportStatus(EnumTeleportStatus value);

	void setDimension(int value);

}