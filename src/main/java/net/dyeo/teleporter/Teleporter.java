package net.dyeo.teleporter;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.dyeo.teleporter.proxy.ISidedProxy;
import net.minecraft.block.Block;

@Mod(modid = "teleporter", version = "1.5.0")
public class Teleporter
{
	public static Block teleporterBlock;
	public static Block enderTeleporterBlock;
	@Mod.Instance("teleporter")
	public static Object instance;
	@SidedProxy(clientSide = "net.dyeo.teleporter.proxy.ClientProxy", serverSide = "net.dyeo.teleporter.proxy.ServerProxy")
	public static ISidedProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		instance = this;

		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		proxy.load(event);
	}
}
