package net.dyeo.teleporter.network;

import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;

/*
 * TeleporterNode contains the members that must be saved to the world.
 * It also contains methods to save and write and read the data to NBT.
 */
public class TeleporterNode
{
	public static enum Type
	{
		teleporterBlock, // teleports to other REGULAR teleporters in same dimension
		enderTeleporterBlock // teleports to ENDER teleporters in any dimension
	};

	public BlockPos pos;
	public int dimension;
	public Type type;

	public TeleporterNode()
	{
		pos = new BlockPos(0, 0, 0);
		dimension = 0;
		type = Type.teleporterBlock;
	}

	// write from nbt
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("x", pos.getX());
		nbt.setInteger("y", pos.getY());
		nbt.setInteger("z", pos.getZ());
		nbt.setInteger("dim", dimension);
		nbt.setInteger("type", type.ordinal());
	}

	// read from nbt
	public void readFromNBT(NBTTagCompound nbt)
	{
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		pos = new BlockPos(x, y, z);
		dimension = nbt.getInteger("dim");
		type = TeleporterNode.Type.values()[nbt.getInteger("type")];
	}

	// get tile entity associated with this node
	public TileEntityTeleporter getTileEntity()
	{
		TileEntity result = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(this.dimension).getTileEntity(this.pos);
		if (result instanceof TileEntityTeleporter) return (TileEntityTeleporter) result;
		else return null;
	}
}
