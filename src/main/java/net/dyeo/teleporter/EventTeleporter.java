package net.dyeo.teleporter;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class EventTeleporter
{
	@SubscribeEvent
	public void onEntityConstructing(EntityEvent.EntityConstructing event)
	{
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

	@SubscribeEvent
	public void onCloneEntity(PlayerEvent.Clone event)
	{
		TeleporterEntity tEntity = (TeleporterEntity)event.entity.getExtendedProperties("TeleporterEntity");
		if (tEntity == null)
		{
			TeleporterEntity.register(event.entity);
		}
		if ((event.entity != null) && ((event.entity instanceof EntityLiving)))
		{
			System.out.println("Cloned Teleporter Entity: " + event.entity.getCommandSenderName());
		}
	}
}
