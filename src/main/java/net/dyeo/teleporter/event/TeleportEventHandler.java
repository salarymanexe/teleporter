package net.dyeo.teleporter.event;

import java.util.ArrayList;
import java.util.List;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TeleportEventHandler
{

	private static List<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();


	private static class TeleportUpdateHandler
	{
		@SubscribeEvent
		public void onLivingUpdate(LivingUpdateEvent event)
		{
			if (!event.getEntity().getEntityWorld().isRemote && entities.contains(event.getEntityLiving()))
			{
				EntityLivingBase entity = event.getEntityLiving();

				if (entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
				{
					ITeleportHandler handler = ((ITeleportHandler)entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null));

					boolean onTeleporter = entity.getEntityWorld().getBlockState(entity.getPosition().down()).getBlock() instanceof BlockTeleporter;

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

		if (!entities.contains(event.getEntityLiving()))
		{
			entities.add(event.getEntityLiving());
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
		if (event.getEntity() instanceof EntityLivingBase)
		{
			EntityLivingBase entity = (EntityLivingBase)event.getEntity();
			if (entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
			{
				ITeleportHandler handler = ((ITeleportHandler)entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null));
				if (handler.getTeleportStatus() != EnumTeleportStatus.IN_PROGRESS)
				{
					handler.setOnTeleporter(false);
					handler.setTeleportStatus(EnumTeleportStatus.INACTIVE);
				}
			}
		}
	}


//	@SubscribeEvent
//	public void onEntityTravelledToDimension(EntityTravelToDimensionEvent event)
//	{
//		System.out.println("onEntityTravelledToDimension");
//	}
//
//	@SubscribeEvent
//	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
//	{
//		System.out.println("onPlayerChangedDimension");
//	}



}
