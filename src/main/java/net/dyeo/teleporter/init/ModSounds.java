package net.dyeo.teleporter.init;

import net.dyeo.teleporter.TeleporterMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ModSounds
{
	private static int size = 0;

	public static SoundEvent PORTAL_ENTER; // = new SoundEvent(new ResourceLocation(TeleporterMod.MODID, "portal_enter")).setRegistryName(new ResourceLocation(TeleporterMod.MODID, "portal_enter"));
	public static SoundEvent PORTAL_EXIT; // = new SoundEvent(new ResourceLocation(TeleporterMod.MODID, "portal_exit")).setRegistryName(new ResourceLocation(TeleporterMod.MODID, "portal_exit"));
	public static SoundEvent PORTAL_ERROR; // = new SoundEvent(new ResourceLocation(TeleporterMod.MODID, "portal_error")).setRegistryName(new ResourceLocation(TeleporterMod.MODID, "portal_error"));


	public static void registerSounds()
	{
		size = SoundEvent.REGISTRY.getKeys().size();

		PORTAL_ENTER = registerSound("portal_enter");
		PORTAL_EXIT = registerSound("portal_exit");
		PORTAL_ERROR = registerSound("portal_error");
	}

	private static SoundEvent registerSound(String name)
	{
		ResourceLocation location = new ResourceLocation(TeleporterMod.MODID, name);
		SoundEvent event = new SoundEvent(location);

		SoundEvent.REGISTRY.register(size, location, event);
		size++;

		return event;
	}

}
