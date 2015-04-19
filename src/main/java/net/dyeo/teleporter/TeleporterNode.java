package net.dyeo.teleporter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class TeleporterNode 
{	
	public BlockPos pos;
	public int dimension;
	
	public TeleporterNode()
	{
		pos = new BlockPos(0,0,0);
		dimension = 0;
	}
	
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("x", pos.getX());
		nbt.setInteger("y", pos.getY());
		nbt.setInteger("z", pos.getZ());
		nbt.setInteger("dim", dimension);
	}
	
	public void readFromNBT(NBTTagCompound nbt) 
	{
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		pos = new BlockPos(x,y,z);
		dimension = nbt.getInteger("dim");
	}
}
