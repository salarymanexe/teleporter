package net.dyeo.teleporter.event;

import java.util.ArrayList;
import java.util.List;

import com.sun.media.jfxmedia.events.PlayerStateEvent;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
			EntityLivingBase entity = event.getEntityLiving();
			if (!event.getEntity().world.isRemote && entity != null && isInitialized(entity))
			{
				if (entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
				{
					ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
					boolean onTeleporter = entity.getEntityWorld().getBlockState(entity.getPosition().down()).getBlock() instanceof BlockTeleporter;

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
							finalizeEntity(entity);
						}
					}
				}
			}
		}

		@SubscribeEvent
		public void onLivingAttack(LivingAttackEvent event)
		{
			if (!event.getEntity().world.isRemote && entities.contains(event.getEntityLiving()))
			{
				EntityLivingBase entity = event.getEntityLiving();
				if (entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
				{
					ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
					event.setCanceled(event.getSource() == DamageSource.IN_WALL && handler.getTeleportStatus() != EnumTeleportStatus.INACTIVE);
				}
			}
		}
	}

	private static TeleportUpdateHandler updateHandler = new TeleportUpdateHandler();

	@SubscribeEvent
	public void onEntityTeleported(TeleportEvent.EntityTeleportedEvent event)
	{
		initializeEntity(event.getEntityLiving());
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.getEntity() instanceof EntityLivingBase)
		{
			EntityLivingBase entity = (EntityLivingBase)event.getEntity();
			if (entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
			{
				ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
				if (handler.getTeleportStatus() == EnumTeleportStatus.IN_PROGRESS)
				{
					initializeEntity(entity);
				}
				else
				{
					handler.setOnTeleporter(false);
					handler.setTeleportStatus(EnumTeleportStatus.INACTIVE);
				}
			}
		}
	}

	private static boolean isInitialized(EntityLivingBase entity)
	{
		return entities.contains(entity);
	}

	private static void initializeEntity(EntityLivingBase entity)
	{
		if (!isInitialized(entity))
		{
			if (entities.size() == 0)
			{
				MinecraftForge.EVENT_BUS.register(updateHandler);
			}
			entities.add(entity);
		}
	}

	private static void finalizeEntity(EntityLivingBase entity)
	{
		if(isInitialized(entity))
		{
			entities.remove(entity);
			if (entities.size() == 0)
			{
				MinecraftForge.EVENT_BUS.unregister(updateHandler);
			}
		}
	}

}
