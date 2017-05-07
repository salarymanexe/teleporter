package net.dyeo.teleporter.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.dyeo.teleporter.entities.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class EventTeleporter
{
	@SubscribeEvent
	public void onEntityConstructing(EntityEvent.EntityConstructing event)
	{
		if ((event.entity instanceof EntityLivingBase))
		{
			TeleporterEntity tEntity = (TeleporterEntity)event.entity.getExtendedProperties("TeleporterEntity");
			if (tEntity == null)
			{
				TeleporterEntity.register(event.entity);
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event)
	{
		Entity entityIn = event.entity;
		if (entityIn != null)
		{
			TeleporterEntity tentity = TeleporterEntity.get(entityIn);
			if (tentity != null)
			{
				tentity.checkLocation();
			}
		}
	}
}
