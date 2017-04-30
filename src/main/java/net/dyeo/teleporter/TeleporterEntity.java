package net.dyeo.teleporter;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class TeleporterEntity implements IExtendedEntityProperties
{
	public static final String EXT_PROP_NAME = "TeleporterEntity";
	private final Entity entity;
	boolean teleported;
	boolean onTeleporter;
	int dimension;

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
		this.teleported = old.teleported;
		this.onTeleporter = old.onTeleporter;
	}

	public void saveNBTData(NBTTagCompound compound)
	{
	}

	public void loadNBTData(NBTTagCompound compound)
	{
	}

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
				this.onTeleporter = true;
			}
			else
			{
				this.onTeleporter = false;
				this.teleported = false;
			}
		}
	}
}
