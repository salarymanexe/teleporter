package net.dyeo.teleporter.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface ITeleporterEntity extends INBTSerializable<NBTTagCompound>
{
	Entity getEntity();
	
	boolean getOnTeleporter();
	
	boolean getTeleported();
	
	int getDimension();
	
	void setEntity(Entity ent);
	
	void setOnTeleporter(boolean val);
	
	void setTeleported(boolean val);
	
	void setDimension(int val);
	
	void checkLocation();
	
	void copy(ITeleporterEntity rhs);
}
