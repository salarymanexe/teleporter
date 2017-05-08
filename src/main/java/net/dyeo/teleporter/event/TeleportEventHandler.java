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
					boolean onTeleporter = event.entityLiving.worldObj.getBlock(entityPos.getX(), entityPos.getY(), entityPos.getZ()) instanceof BlockTeleporter;

					if (handler.getTeleportStatus() == EnumTeleportStatus.IN_PROGRESS)
					{
						if (onTeleporter)
						{
							System.out.println("onLivingUpdate :: Setting teleportStatus = " + EnumTeleportStatus.SUCCEEDED);
							handler.setTeleportStatus(EnumTeleportStatus.SUCCEEDED);
						}
						else return;
					}

					if (handler.getTeleportStatus() == EnumTeleportStatus.SUCCEEDED || handler.getTeleportStatus() == EnumTeleportStatus.FAILED)
					{
						if (!onTeleporter)
						{
							System.out.println("onLivingUpdate :: Setting onTeleporter = " + false);
							System.out.println("onLivingUpdate :: Setting teleportStatus = " + EnumTeleportStatus.INACTIVE);

							handler.setOnTeleporter(false);
							handler.setTeleportStatus(EnumTeleportStatus.INACTIVE);

							entities.remove(entity);
							if (entities.size() == 0)
							{
								System.out.println("onLivingUpdate :: unregistering for updates");
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
		System.out.println("onEntityTeleported");

		if (!entities.contains(event.entityLiving))
		{
			entities.add(event.entityLiving);
		}
		if (entities.size() == 1)
		{
			System.out.println("onEntityTeleported :: registering for updates");
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

			if (handler != null && handler.getTeleportStatus() != EnumTeleportStatus.IN_PROGRESS)
			{
				handler.setOnTeleporter(false);
				handler.setTeleportStatus(EnumTeleportStatus.INACTIVE);
			}
		}
	}

}
