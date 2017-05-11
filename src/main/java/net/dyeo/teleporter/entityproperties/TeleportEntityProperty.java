package net.dyeo.teleporter.entityproperties;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.dyeo.teleporter.TeleporterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

public class TeleportEntityProperty implements IExtendedEntityProperties
{

	public static final String PROP_NAME = TeleporterMod.MODID + "_TeleporterEntityData";

	public static final void registerProperty()
	{
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}

	public static final TeleportEntityProperty get(Entity entity)
	{
		return (TeleportEntityProperty)entity.getExtendedProperties(PROP_NAME);
	}


	public enum EnumTeleportStatus
	{
		INACTIVE,
		IN_PROGRESS,
		SUCCEEDED,
		FAILED
	}


	private boolean onTeleporter = false;
	private EnumTeleportStatus teleportStatus = EnumTeleportStatus.INACTIVE;
	private int dimension = 0;


	public TeleportEntityProperty()
	{
	}

	@Override
	public void init(Entity entity, World world)
	{
	}

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		compound.setBoolean("onTeleporter", this.onTeleporter);
		compound.setInteger("teleportStatus", this.teleportStatus.ordinal());
		compound.setInteger("dimension", this.dimension);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		this.onTeleporter = compound.getBoolean("onTeleporter");
		this.teleportStatus = EnumTeleportStatus.values()[compound.getInteger("teleportStatus")];
		this.dimension = compound.getInteger("dimension");
	}


	public boolean getOnTeleporter()
	{
		return this.onTeleporter;
	}

	public EnumTeleportStatus getTeleportStatus()
	{
		return this.teleportStatus;
	}

	public int getDimension()
	{
		return this.dimension;
	}

	public void setOnTeleporter(boolean value)
	{
		this.onTeleporter = value;
	}

	public void setTeleportStatus(EnumTeleportStatus value)
	{
		this.teleportStatus = value;
	}

	public void setDimension(int value)
	{
		this.dimension = value;
	}






	public void copy(TeleportEntityProperty old)
	{
		this.dimension = old.dimension;
		this.setTeleportStatus(old.getTeleportStatus());
		this.setOnTeleporter(old.getOnTeleporter());
	}



	public static class EventHandler
	{
		@SubscribeEvent
		public void onEntityConstructing(EntityEvent.EntityConstructing event)
		{
			if (event.entity instanceof EntityLivingBase)
			{
				if (event.entity.getExtendedProperties(PROP_NAME) == null)
				{
					event.entity.registerExtendedProperties(PROP_NAME, new TeleportEntityProperty());
				}
			}
		}
	}
}
