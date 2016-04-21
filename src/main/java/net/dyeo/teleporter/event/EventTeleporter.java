package net.dyeo.teleporter.event;

import net.dyeo.teleporter.entities.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventTeleporter 
{

	// we must add our entity extension to every living entity created so that it can use the teleporter
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity instanceof EntityLivingBase)
		{
			TeleporterEntity tEntity = (TeleporterEntity)event.entity.getExtendedProperties(TeleporterEntity.EXT_PROP_NAME);
			if(tEntity == null)
			{
				TeleporterEntity.register(event.entity);
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		Entity entityIn = event.entity;
		
		if(entityIn != null)
		{
		
			TeleporterEntity tentity = TeleporterEntity.get(entityIn);
		
			if(tentity != null)
			{
				tentity.checkLocation();
			}
		}
	}
		
}