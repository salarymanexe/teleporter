package net.dyeo.teleporter;

import ibxm.Player;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventTeleporter 
{
	
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity instanceof EntityPlayer && TeleporterEntity.get((EntityPlayer) event.entity) == null)
		{
			// register the event handler
			TeleporterEntity.register((EntityPlayer) event.entity);
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