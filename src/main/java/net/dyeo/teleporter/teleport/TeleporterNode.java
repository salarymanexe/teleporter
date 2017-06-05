package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;


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
	public String key;

	public TeleporterNode()
	{
		this.pos = new BlockPos(0, 0, 0);
		this.dimension = 0;
		this.type = BlockTeleporter.EnumType.REGULAR;
		this.key = ItemStack.EMPTY.getUnlocalizedName();
	}
	
	public TeleporterNode(BlockPos pos, int dimension)
	{
		this.pos = pos;
		this.dimension = dimension;
		
		TileEntityTeleporter tile = (TileEntityTeleporter)FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimension).getTileEntity(pos);
		
		if(tile != null)
		{
			this.type = tile.getWorld().getBlockState(this.pos).getValue(BlockTeleporter.TYPE);
			this.key = TeleporterNetwork.getItemKey(tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0));
		}
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
		this.type = BlockTeleporter.EnumType.byMetadata(nbt.getInteger("type"));
		this.key = nbt.getString("key");
	}

	public TileEntityTeleporter getTileEntity()
	{
		TileEntity result = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(this.dimension).getTileEntity(this.pos);
		if (result instanceof TileEntityTeleporter) {
			return (TileEntityTeleporter) result;
		}
		else {
			return null;
		}
	}


	@Override
	public String toString()
	{
		return "{ \"x\":" + this.pos.getX() + ", \"y\":" + this.pos.getY() + ", \"z\":" + this.pos.getZ() + ", \"dim\":" + this.dimension + ", \"type\":" + this.type + ", \"key\":" + this.key + " }";
	}

	public boolean matches(BlockPos pos, int dimension)
	{
		return this.pos.equals(pos) && this.dimension == dimension;
	}

}
