package net.dyeo.teleporter.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityTeleporterEntity
{

	@CapabilityInject(ITeleporterEntity.class)
	public static Capability<ITeleporterEntity> INSTANCE;

	// Registers the capability so that it may be used.
	public static void register()
	{
		CapabilityManager.INSTANCE.register(ITeleporterEntity.class, new Storage(), TeleporterEntity.class);
	}

	////////////////
	// Storage
	// Used for public exposure of capabilities.
	private static class Storage implements Capability.IStorage<ITeleporterEntity>
	{
		@Override
		public NBTBase writeNBT(Capability<ITeleporterEntity> capability, ITeleporterEntity instance, EnumFacing side)
		{
			return null;
		}

		@Override
		public void readNBT(Capability<ITeleporterEntity> capability, ITeleporterEntity instance, EnumFacing side,
				NBTBase nbt)
		{
		}
	}

	////////////////
	// Provider Class
	// Attaches a Capability's implementation to a foreign entity.
	public static class Provider implements ICapabilitySerializable<NBTTagCompound>
	{
		public static final ResourceLocation NAME = new ResourceLocation("teleporter", "entity");

		private final ITeleporterEntity cap = new TeleporterEntity();

		public Provider(Entity ent)
		{
			cap.setEntity(ent);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing)
		{
			return capability == CapabilityTeleporterEntity.INSTANCE;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing)
		{
			if (capability == CapabilityTeleporterEntity.INSTANCE)
			{
				return (T) cap;
			}

			return null;
		}

		@Override
		public NBTTagCompound serializeNBT()
		{
			return cap.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt)
		{
			cap.deserializeNBT(nbt);
		}
	}

	private CapabilityTeleporterEntity()
	{
	}

}
