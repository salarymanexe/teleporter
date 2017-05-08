package net.dyeo.teleporter.capabilities;

import net.dyeo.teleporter.TeleporterMod;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityTeleportHandler
{

	@CapabilityInject(ITeleportHandler.class)
	public static final Capability<ITeleportHandler> TELEPORT_CAPABILITY = null;


	public static void registerCapabilities()
	{
		CapabilityManager.INSTANCE.register(ITeleportHandler.class, new Storage(), TeleportHandler.class);
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}


	public static class Provider implements ICapabilitySerializable<NBTBase>
	{

		private final ITeleportHandler instance = TELEPORT_CAPABILITY.getDefaultInstance();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing)
		{
			return capability == CapabilityTeleportHandler.TELEPORT_CAPABILITY;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing)
		{
			return capability == CapabilityTeleportHandler.TELEPORT_CAPABILITY ? (T)this.instance : null;
		}

		@Override
		public NBTBase serializeNBT()
		{
			return TELEPORT_CAPABILITY.getStorage().writeNBT(TELEPORT_CAPABILITY, this.instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt)
		{
			TELEPORT_CAPABILITY.getStorage().readNBT(TELEPORT_CAPABILITY, this.instance, null, nbt);
		}
	}


	private static class Storage implements Capability.IStorage<ITeleportHandler>
	{
		@Override
		public NBTBase writeNBT(Capability<ITeleportHandler> capability, ITeleportHandler instance, EnumFacing side)
		{
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<ITeleportHandler> capability, ITeleportHandler instance, EnumFacing side, NBTBase nbt)
		{
			instance.deserializeNBT((NBTTagCompound)nbt);
		}
	}


	private static class EventHandler
	{
		@SubscribeEvent
		public void onAttachCapability(AttachCapabilitiesEvent.Entity event)
		{
			if (event.getEntity() instanceof EntityLivingBase)
			{
				event.addCapability(new ResourceLocation(TeleporterMod.MODID, "entity"), new CapabilityTeleportHandler.Provider());
			}
		}

		@SubscribeEvent
		public void onPlayerDeath(PlayerEvent.Clone event)
		{
			if (event.wasDeath)
			{
				final ITeleportHandler oldHandler = event.original.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
				final ITeleportHandler newHandler = event.entityPlayer.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
				newHandler.deserializeNBT(oldHandler.serializeNBT());
			}
		}

	}

}
