package net.dyeo.teleporter;

import ibxm.Player;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventTeleporter 
{
	
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
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
	
	@SubscribeEvent
	public void onCloneEntity(PlayerEvent.Clone event)
	{
		TeleporterEntity tEntity = (TeleporterEntity)event.entity.getExtendedProperties(TeleporterEntity.EXT_PROP_NAME);
		if(tEntity == null)
		{
			TeleporterEntity.register(event.entity);
		}
		if (event.entity != null && event.entity instanceof EntityLiving)
		{
			System.out.println("Cloned Teleporter Entity: " + event.entity.getName());
		}
	}
		
}