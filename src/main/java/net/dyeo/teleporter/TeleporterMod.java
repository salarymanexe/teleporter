package net.dyeo.teleporter;

import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;


@Mod(modid = TeleporterMod.MODID, name = TeleporterMod.NAME, version = TeleporterMod.VERSION)
public class TeleporterMod
{

	public static final String MODID = "teleporter";
	public static final String NAME = "Vanilla-Inspired Teleporters";
	public static final String VERSION = "1.6.0";

	private static final String CLIENT_PROXY_CLASS = "net.dyeo.teleporter.proxy.ClientProxy";
	private static final String SERVER_PROXY_CLASS = "net.dyeo.teleporter.proxy.CommonProxy";



	@Instance(MODID)
	public static Object instance;

	@SidedProxy(clientSide = CLIENT_PROXY_CLASS, serverSide = SERVER_PROXY_CLASS)
	public static CommonProxy proxy;


	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ModConfiguration.preInit();
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
	}

}
