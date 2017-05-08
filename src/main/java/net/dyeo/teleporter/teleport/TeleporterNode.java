package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;


/**
 * TeleporterNode contains the members that must be saved to the world.
 * It also contains methods to save and write and read the data to NBT.
 *
 */
public class TeleporterNode
{

	public BlockPos pos;
	public int dimension;
	public BlockTeleporter.EnumType type;

	public TeleporterNode()
	{
		this.pos = new BlockPos(0, 0, 0);
		this.dimension = 0;
		this.type = BlockTeleporter.EnumType.REGULAR;
	}

	public TeleporterNode(NBTTagCompound compound)
	{
		this.readFromNBT(compound);
	}


	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("x", this.pos.getX());
		nbt.setInteger("y", this.pos.getY());
		nbt.setInteger("z", this.pos.getZ());
		nbt.setInteger("dim", this.dimension);
		nbt.setInteger("type", this.type.ordinal());
		return nbt;
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		this.pos = new BlockPos(x, y, z);
		this.dimension = nbt.getInteger("dim");
		this.type = BlockTeleporter.EnumType.byMetadata(nbt.getInteger("type"));
	}

	public TileEntityTeleporter getTileEntity()
	{
		TileEntity result = MinecraftServer.getServer().worldServerForDimension(this.dimension).getTileEntity(this.pos);
		return  (result instanceof TileEntityTeleporter ? (TileEntityTeleporter) result : null);
	}


	@Override
	public String toString()
	{
		return "{ \"x\":" + this.pos.getX() + ", \"y\":" + this.pos.getY() + ", \"z\":" + this.pos.getZ() + ", \"dim\":" + this.dimension + ", \"type\":" + this.type + " }";
	}

	public boolean matches(BlockPos pos, int dimension)
	{
		return this.pos.equals(pos) && this.dimension == dimension;
	}

}