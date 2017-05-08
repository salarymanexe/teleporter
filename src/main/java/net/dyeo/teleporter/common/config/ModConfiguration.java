package net.dyeo.teleporter.common.config;

import java.io.File;
import cpw.mods.fml.common.Loader;
import net.dyeo.teleporter.TeleporterMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ModConfiguration
{
	private static Configuration config = null;

	public static final String CATEGORY_UPDATES = "updates";

	public static boolean useDiamonds = true;
	public static int numTeleporters = 1;
	public static boolean teleportPassiveMobs = true;
	public static boolean teleportHostileMobs = true;

	public static boolean checkForUpdates = true;
	public static boolean promptForLatest = true;
	public static boolean promptForRecommended = true;


	public static void preInit()
	{
		File configFile = new File(Loader.instance().getConfigDir(), TeleporterMod.MODID + ".cfg");

		config = new Configuration(configFile);

		config.setCategoryRequiresMcRestart(Configuration.CATEGORY_GENERAL, true);
		config.load();
		config.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, "Vanilla-Inspired Teleporters Version " + TeleporterMod.VERSION + " Configuration");


		Property propUseDiamonds = config.get(Configuration.CATEGORY_GENERAL, "useDiamonds", useDiamonds, "If false, removes diamonds from the crafting recipe and replaces them with quartz blocks.\nDefault is true");
		Property propNumTeleporters = config.get(Configuration.CATEGORY_GENERAL, "numTeleporters", numTeleporters, "Specifies the number of teleporters created with a single recipe.\nDefault is 1");
		Property propTeleportPassiveMobs = config.get(Configuration.CATEGORY_GENERAL, "teleportPassiveMobs", teleportPassiveMobs, "Specifies whether or not passive mobs can go through teleporters.\nDefault is true");
		Property propTeleportHostileMobs = config.get(Configuration.CATEGORY_GENERAL, "teleportHostileMobs", teleportHostileMobs, "Specifies whether or not hostile mobs can go through teleporters.\nDefault is true");

		Property propCheckForUpdates = config.get(ModConfiguration.CATEGORY_UPDATES, "checkForUpdates", checkForUpdates, "Should the mod check for updates on startup");
		Property propPromptForLatest = config.get(ModConfiguration.CATEGORY_UPDATES, "promptForLatest", promptForLatest, "Alert the user when there is a new version");
		Property propPromptForRecommended = config.get(ModConfiguration.CATEGORY_UPDATES, "promptForRecommended", promptForRecommended, "Alert the user when there is a new recommended version");

		useDiamonds = propUseDiamonds.getBoolean();
		numTeleporters = propNumTeleporters.getInt();
		teleportPassiveMobs = propTeleportPassiveMobs.getBoolean();
		teleportHostileMobs = propTeleportHostileMobs.getBoolean();

		checkForUpdates = propCheckForUpdates.getBoolean();
		promptForLatest = propPromptForLatest.getBoolean();
		promptForRecommended = propPromptForRecommended.getBoolean();

		if (config.hasChanged()) config.save();
	}
}
