package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.block.BlockTeleporter.EnumType;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;


/**
 * TeleporterNode contains the members that must be saved to the world.
 * It also contains methods to save and write and read the data to NBT.
 *
 */
public class TeleporterNode
{

	public BlockPos pos = BlockPos.ORIGIN;
	public int dimension = 0;
	public EnumType type = EnumType.REGULAR;
	public String key = Blocks.AIR.getUnlocalizedName();

	public TeleporterNode(NBTTagCompound compound)
	{
		this.readFromNBT(compound);
	}

	public TeleporterNode(BlockPos pos, int dimension, EnumType type, String key)
	{
		this.pos = pos;
		this.dimension = dimension;
		this.type = type;
		if (key != null) this.key = key;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("x", this.pos.getX());
		nbt.setInteger("y", this.pos.getY());
		nbt.setInteger("z", this.pos.getZ());
		nbt.setInteger("dim", this.dimension);
		nbt.setInteger("type", this.type.ordinal());
		nbt.setString("key", this.key);
		return nbt;
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		this.pos = new BlockPos(x, y, z);
		this.dimension = nbt.getInteger("dim");
		this.type = EnumType.byMetadata(nbt.getInteger("type"));
		this.key = nbt.getString("key");
	}
	
	@Override
	public String toString()
	{
		return "TeleporterNode [pos=" + pos + ", dimension=" + dimension + ", type=" + type + ", key=" + key + "]";
	}

	public boolean matches(BlockPos pos, int dimension)
	{
		return this.pos.equals(pos) && this.dimension == dimension;
	}

}
