package net.dyeo.teleporter.event;

import java.util.ArrayList;
import java.util.List;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty.EnumTeleportStatus;
import net.dyeo.teleporter.util.Vec3i;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class TeleportEventHandler
{

	private static List<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();

	public static class TeleportUpdateHandler
	{
		@SubscribeEvent
		public void onLivingUpdate(LivingEvent.LivingUpdateEvent event)
		{
			if (!event.entityLiving.worldObj.isRemote && entities.contains(event.entityLiving))
			{
				EntityLivingBase entity = event.entityLiving;
				TeleportEntityProperty handler = TeleportEntityProperty.get(entity);

				if (handler != null)
				{
					Vec3i entityPos = new Vec3i(entity);
					boolean onTeleporter = event.entityLiving.worldObj.getBlock(entityPos.getX(), entityPos.getY() - 1, entityPos.getZ()) instanceof BlockTeleporter;

					if (handler.getTeleportStatus() == EnumTeleportStatus.IN_PROGRESS)
					{
						if (onTeleporter)
						{
							handler.setTeleportStatus(EnumTeleportStatus.SUCCEEDED);
						}
						else return;
					}

					if (handler.getTeleportStatus() == EnumTeleportStatus.SUCCEEDED || handler.getTeleportStatus() == EnumTeleportStatus.FAILED)
					{
						if (!onTeleporter)
						{
							handler.setOnTeleporter(false);
							handler.setTeleportStatus(EnumTeleportStatus.INACTIVE);

							entities.remove(entity);
							if (entities.size() == 0)
							{
								MinecraftForge.EVENT_BUS.unregister(updateHandler);
							}
						}
					}
				}
			}
		}
	}

	private static TeleportUpdateHandler updateHandler = new TeleportUpdateHandler();


	@SubscribeEvent
	public void onEntityTeleported(TeleportEvent.EntityTeleportedEvent event)
	{
		if (!entities.contains(event.entityLiving))
		{
			entities.add(event.entityLiving);
		}
		if (entities.size() == 1)
		{
			MinecraftForge.EVENT_BUS.register(updateHandler);
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.entity instanceof EntityLivingBase)
		{
			EntityLivingBase entity = (EntityLivingBase)event.entity;
			TeleportEntityProperty handler = TeleportEntityProperty.get(entity);
			if (handler != null)
			{
				if (handler.getTeleportStatus() == EnumTeleportStatus.IN_PROGRESS)
				{
					if (!entities.contains(entity))
					{
						entities.add(entity);
					}
					if (entities.size() == 1)
					{
						MinecraftForge.EVENT_BUS.register(updateHandler);
					}
				}
				else
				{
					handler.setOnTeleporter(false);
					handler.setTeleportStatus(EnumTeleportStatus.INACTIVE);
				}
			}
		}
	}

}
