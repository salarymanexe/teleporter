package net.dyeo.teleporter.event;

import net.dyeo.teleporter.capabilities.CapabilityTeleporterEntity;
import net.dyeo.teleporter.capabilities.ITeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventTeleporter 
{

	@SubscribeEvent
	public void onAttachCapability(AttachCapabilitiesEvent.Entity event)
	{
		if(event.getEntity() instanceof EntityLivingBase)
		{
			event.addCapability(CapabilityTeleporterEntity.Provider.NAME, new CapabilityTeleporterEntity.Provider(event.getEntity()));
		}
	}
	
	/*@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity instanceof EntityLivingBase)
		{
			TeleporterEntityProperties tEntity = (TeleporterEntityProperties)event.entity.getExtendedProperties(TeleporterEntityProperties.EXT_PROP_NAME);
			if(tEntity == null)
			{
				TeleporterEntityProperties.register(event.entity);
			}
		}
	}*/
	
	/*@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		Entity entityIn = event.entity;
		
		if(entityIn != null)
		{
		
			TeleporterEntityProperties tentity = TeleporterEntityProperties.get(entityIn);
		
			if(tentity != null)
			{
				tentity.checkLocation();
			}
		}
	}*/
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		Entity entityIn = event.entity;
		
		if(entityIn != null && entityIn.hasCapability(CapabilityTeleporterEntity.INSTANCE, null))
		{
			ITeleporterEntity ite = entityIn.getCapability(CapabilityTeleporterEntity.INSTANCE, null);
			if(ite != null)
			{
				ite.checkLocation();
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(PlayerEvent.Clone event)
	{
		if(event.wasDeath)
		{
			ITeleporterEntity ite1 = event.entityPlayer.getCapability(CapabilityTeleporterEntity.INSTANCE, null);
			ite1.copy(event.original.getCapability(CapabilityTeleporterEntity.INSTANCE, null));
		}
	}
		
}