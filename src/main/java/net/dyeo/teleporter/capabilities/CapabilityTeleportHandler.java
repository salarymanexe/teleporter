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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class CapabilityTeleportHandler
{

	@CapabilityInject(ITeleportHandler.class)
	public static final Capability<ITeleportHandler> TELEPORT_CAPABILITY = null;


	public static void registerCapabilities()
	{
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		CapabilityManager.INSTANCE.register(ITeleportHandler.class, new Storage(), TeleportHandler.class);
	}



	public static class Provider implements ICapabilitySerializable<NBTBase>
	{

		private final ITeleportHandler instance = TELEPORT_CAPABILITY.getDefaultInstance();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing)
		{
			return capability == CapabilityTeleportHandler.TELEPORT_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing)
		{
			return capability == CapabilityTeleportHandler.TELEPORT_CAPABILITY ? TELEPORT_CAPABILITY.<T>cast(this.instance) : null;
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
		public void onAttachCapability(final AttachCapabilitiesEvent<Entity> event)
		{
			if (event.getObject() instanceof EntityLivingBase)
			{
				event.addCapability(new ResourceLocation(TeleporterMod.MODID, "entity"), new CapabilityTeleportHandler.Provider());
			}
		}

		@SubscribeEvent
		public void onPlayerClone(PlayerEvent.Clone event)
		{
			if (event.isWasDeath())
			{
				final ITeleportHandler oldHandler = event.getOriginal().getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
				final ITeleportHandler newHandler = event.getEntityPlayer().getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
				newHandler.deserializeNBT(oldHandler.serializeNBT());
			}
		}

	}

}
