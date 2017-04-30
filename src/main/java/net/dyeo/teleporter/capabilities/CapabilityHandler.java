package net.dyeo.teleporter.capabilities;

import net.dyeo.teleporter.TeleporterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class CapabilityHandler
{

	@CapabilityInject(ITeleporterEntity.class)
	public static final Capability<ITeleporterEntity> TELEPORT_CAPABILITY = null;


	public static void registerCapabilities()
	{
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		CapabilityManager.INSTANCE.register(ITeleporterEntity.class, new Storage(), TeleporterEntity.class);
	}



	public static class Provider implements ICapabilitySerializable<NBTTagCompound>
	{

		private final ITeleporterEntity instance = new TeleporterEntity();

		public Provider(Entity ent)
		{
			instance.setEntity(ent);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing)
		{
			return capability == CapabilityHandler.TELEPORT_CAPABILITY;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing)
		{
			if (capability == CapabilityHandler.TELEPORT_CAPABILITY)
			{
				return (T) instance;
			}

			return null;
		}

		@Override
		public NBTTagCompound serializeNBT()
		{
			return instance.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt)
		{
			instance.deserializeNBT(nbt);
		}
	}



	private static class Storage implements Capability.IStorage<ITeleporterEntity>
	{
		@Override
		public NBTBase writeNBT(Capability<ITeleporterEntity> capability, ITeleporterEntity instance, EnumFacing side)
		{
			return null;
		}

		@Override
		public void readNBT(Capability<ITeleporterEntity> capability, ITeleporterEntity instance, EnumFacing side, NBTBase nbt)
		{
		}
	}



	private static class EventHandler
	{
		@SubscribeEvent
		public void onAttachCapability(AttachCapabilitiesEvent.Entity event)
		{
			if (event.getEntity() instanceof EntityLivingBase)
			{
				event.addCapability(new ResourceLocation(TeleporterMod.MODID, "entity"), new CapabilityHandler.Provider(event.getEntity()));
			}
		}

		@SubscribeEvent
		public void onLivingUpdate(LivingUpdateEvent event)
		{
			Entity entityIn = event.getEntity();

			if (entityIn != null && entityIn.hasCapability(CapabilityHandler.TELEPORT_CAPABILITY, null))
			{
				ITeleporterEntity ite = entityIn.getCapability(CapabilityHandler.TELEPORT_CAPABILITY, null);
				if (ite != null)
				{
					ite.checkLocation();
				}
			}
		}

		@SubscribeEvent
		public void onPlayerClone(PlayerEvent.Clone event)
		{
			if (event.isWasDeath())
			{
				ITeleporterEntity ite1 = event.getEntityPlayer().getCapability(CapabilityHandler.TELEPORT_CAPABILITY, null);
				ite1.copy(event.getOriginal().getCapability(CapabilityHandler.TELEPORT_CAPABILITY, null));
			}
		}
	}

}
