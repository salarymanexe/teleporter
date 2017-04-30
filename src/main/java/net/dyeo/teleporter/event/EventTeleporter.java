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
		if (event.getEntity() instanceof EntityLivingBase)
		{
			event.addCapability(CapabilityTeleporterEntity.Provider.NAME,
					new CapabilityTeleporterEntity.Provider(event.getEntity()));
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		Entity entityIn = event.getEntity();

		if (entityIn != null && entityIn.hasCapability(CapabilityTeleporterEntity.INSTANCE, null))
		{
			ITeleporterEntity ite = entityIn.getCapability(CapabilityTeleporterEntity.INSTANCE, null);
			if (ite != null)
			{
				ite.checkLocation();
			}
		}
	}

	@SubscribeEvent
	public void onDeath(PlayerEvent.Clone event)
	{
		if (event.isWasDeath())
		{
			ITeleporterEntity ite1 = event.getEntityPlayer().getCapability(CapabilityTeleporterEntity.INSTANCE, null);
			ite1.copy(event.getOriginal().getCapability(CapabilityTeleporterEntity.INSTANCE, null));
		}
	}

}
