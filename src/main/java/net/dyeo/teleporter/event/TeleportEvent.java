package net.dyeo.teleporter.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;

public class TeleportEvent
{

	public static class EntityTeleportedEvent extends LivingEvent
	{
		public EntityTeleportedEvent(EntityLivingBase entity)
		{
			super(entity);
		}
	}

}