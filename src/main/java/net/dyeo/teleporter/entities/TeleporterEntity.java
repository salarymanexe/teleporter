package net.dyeo.teleporter.entities;

import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.network.TeleporterNetwork;
import net.dyeo.teleporter.network.TeleporterNode;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class TeleporterEntity implements IExtendedEntityProperties
{
	public static final String EXT_PROP_NAME = "TeleporterEntity";
	private final Entity entity;
	private boolean teleported;
	private boolean onTeleporter;
	int dimension;

	public boolean getTeleported()
	{
		return this.teleported;
	}

	public void setTeleported(boolean teleported)
	{
		this.teleported = teleported;
	}

	public boolean getOnTeleporter()
	{
		return this.onTeleporter;
	}

	public void setOnTeleporter(boolean onTeleporter)
	{
		this.onTeleporter = onTeleporter;
	}

	public TeleporterEntity(Entity entity)
	{
		this.entity = entity;
	}

	public static final void register(Entity entity)
	{
		entity.registerExtendedProperties("TeleporterEntity", new TeleporterEntity(entity));
	}

	public static final TeleporterEntity get(Entity entity)
	{
		return (TeleporterEntity)entity.getExtendedProperties("TeleporterEntity");
	}

	public void copy(TeleporterEntity old)
	{
		this.dimension = old.dimension;
		this.setTeleported(old.getTeleported());
		this.setOnTeleporter(old.getOnTeleporter());
	}

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		compound.setInteger("dimension", this.dimension);
		compound.setBoolean("teleported", this.getTeleported());
		compound.setBoolean("onTeleporter", this.getOnTeleporter());
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		this.dimension = compound.getInteger("dimension");
		this.setTeleported(compound.getBoolean("teleported"));
		this.setOnTeleporter(compound.getBoolean("onTeleporter"));
	}

	@Override
	public void init(Entity entity, World world)
	{
	}

	public void checkLocation()
	{
		if (!this.entity.worldObj.isRemote)
		{
			TeleporterNetwork netWrapper = TeleporterNetwork.get(this.entity.worldObj, false);

			int posx = MathHelper.floor_double(this.entity.posX);
			int posy = MathHelper.floor_double(this.entity.posY - BlockTeleporter.getBounds().yCoord);
			int posz = MathHelper.floor_double(this.entity.posZ);

			TeleporterNode node = netWrapper.getNode(posx, posy, posz, this.entity.worldObj.provider.dimensionId, false);
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
}
