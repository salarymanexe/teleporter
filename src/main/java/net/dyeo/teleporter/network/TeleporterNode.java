package net.dyeo.teleporter.network;

import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

public class TeleporterNode
{
	public int posx;
	public int posy;
	public int posz;
	public int dimension;
	public Type type;

	public static enum Type
	{
		teleporterBlock, enderTeleporterBlock;

		private Type()
		{
		}
	}

	public TeleporterNode()
	{
		this.posx = 0;
		this.posy = 0;
		this.posz = 0;
		this.dimension = 0;
		this.type = Type.teleporterBlock;
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("x", this.posx);
		nbt.setInteger("y", this.posy);
		nbt.setInteger("z", this.posz);
		nbt.setInteger("dim", this.dimension);
		nbt.setInteger("type", this.type.ordinal());
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		this.posx = nbt.getInteger("x");
		this.posy = nbt.getInteger("y");
		this.posz = nbt.getInteger("z");
		this.dimension = nbt.getInteger("dim");
		this.type = Type.values()[nbt.getInteger("type")];
	}

	public TileEntityTeleporter getTileEntity()
	{
		TileEntity result = MinecraftServer.getServer().worldServerForDimension(this.dimension).getTileEntity(this.posx, this.posy, this.posz);
		if ((result instanceof TileEntityTeleporter))
		{
			return (TileEntityTeleporter)result;
		}
		return null;
	}
}
