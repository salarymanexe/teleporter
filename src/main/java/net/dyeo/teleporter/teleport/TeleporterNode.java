package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

public class TeleporterNode
{
	public int x;
	public int y;
	public int z;
	public int dimension;
	public BlockTeleporter.EnumType type;


	public TeleporterNode()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.dimension = 0;
		this.type = BlockTeleporter.EnumType.REGULAR;
	}

	public TeleporterNode(NBTTagCompound compound)
	{
		this.readFromNBT(compound);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("x", this.x);
		nbt.setInteger("y", this.y);
		nbt.setInteger("z", this.z);
		nbt.setInteger("dim", this.dimension);
		nbt.setInteger("type", this.type.ordinal());
		return nbt;
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		this.x = nbt.getInteger("x");
		this.y = nbt.getInteger("y");
		this.z = nbt.getInteger("z");
		this.dimension = nbt.getInteger("dim");
		this.type = BlockTeleporter.EnumType.byMetadata(nbt.getInteger("type"));
	}

	public TileEntityTeleporter getTileEntity()
	{
		TileEntity result = MinecraftServer.getServer().worldServerForDimension(this.dimension).getTileEntity(this.x, this.y, this.z);
		if ((result instanceof TileEntityTeleporter))
		{
			return (TileEntityTeleporter)result;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "{ \"x\":" + this.x + ", \"y\":" + this.y + ", \"z\":" + this.z + ", \"dim\":" + this.dimension + ", \"type\":" + this.type + " }";
	}

	public boolean matches(int x, int y, int z, int dimension)
	{
		return this.x == x && this.y== y && this.z == z && this.dimension == dimension;
	}

}
