package net.dyeo.teleporter.init;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.common.config.ModConfiguration;

public class ModSounds
{
	public static String PORTAL_ENTER = TeleporterMod.MODID + ":portal_enter";
	public static String PORTAL_EXIT = TeleporterMod.MODID + ":portal_exit";
	public static String PORTAL_ERROR = TeleporterMod.MODID + ":portal_error";


	public static void registerSounds()
	{
		PORTAL_ENTER = ModConfiguration.soundEffectTeleporterEnter;
		PORTAL_EXIT = ModConfiguration.soundEffectTeleporterExit;
		PORTAL_ERROR = ModConfiguration.soundEffectTeleporterError;
	}

}
