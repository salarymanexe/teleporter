package net.dyeo.teleporter;

import net.minecraft.nbt.NBTTagCompound;

public class TeleporterNode
{
	public int posx;
	public int posy;
	public int posz;
	public int dimension;

	public TeleporterNode()
	{
		this.posx = (this.posy = this.posz = 0);
		this.dimension = 0;
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("x", this.posx);
		nbt.setInteger("y", this.posy);
		nbt.setInteger("z", this.posz);
		nbt.setInteger("dim", this.dimension);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		this.posx = nbt.getInteger("x");
		this.posy = nbt.getInteger("y");
		this.posz = nbt.getInteger("z");
		this.dimension = nbt.getInteger("dim");
	}
}
