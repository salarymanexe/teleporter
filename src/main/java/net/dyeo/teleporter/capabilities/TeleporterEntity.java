package net.dyeo.teleporter.capabilities;

import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.network.TeleporterNetwork;
import net.dyeo.teleporter.network.TeleporterNode;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class TeleporterEntity implements ITeleporterEntity
{

	private Entity entity;

	private boolean onTeleporter = false;

	private boolean teleported = false;

	private int dimension;

	@Override
	public Entity getEntity()
	{
		return entity;
	}

	@Override
	public boolean getOnTeleporter()
	{
		return onTeleporter;
	}

	@Override
	public boolean getTeleported()
	{
		return teleported;
	}

	@Override
	public int getDimension()
	{
		return dimension;
	}

	@Override
	public void setEntity(Entity ent)
	{
		entity = ent;
	}

	@Override
	public void setOnTeleporter(boolean val)
	{
		onTeleporter = val;
	}

	@Override
	public void setTeleported(boolean val)
	{
		teleported = val;
	}

	@Override
	public void checkLocation()
	{
		if (entity != null && !entity.worldObj.isRemote)
		{
			TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.worldObj, false);

			BlockPos ppos = new BlockPos(MathHelper.floor_double(entity.posX),
					MathHelper.floor_double(entity.posY - (BlockTeleporter.getBounds().yCoord)),
					MathHelper.floor_double(entity.posZ));

			TeleporterNode node = netWrapper.getNode(new BlockPos(ppos), entity.worldObj.provider.getDimensionId(),
					false);

			this.setDimension(entity.worldObj.provider.getDimensionId());

			if (node != null)
			{
				this.setOnTeleporter(true);
			}
			else
			{
				this.setOnTeleporter(false);
				this.setTeleported(false);
			}
		}
	}

	@Override
	public void setDimension(int val)
	{
		dimension = val;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("onTeleporter", onTeleporter);
		nbt.setBoolean("teleported", teleported);
		nbt.setInteger("dimension", dimension);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		onTeleporter = nbt.getBoolean("onTeleporter");
		teleported = nbt.getBoolean("teleported");
		dimension = nbt.getInteger("dimension");
	}

	@Override
	public void copy(ITeleporterEntity rhs)
	{
		onTeleporter = rhs.getOnTeleporter();
		teleported = rhs.getTeleported();
		dimension = rhs.getDimension();
	}

}
