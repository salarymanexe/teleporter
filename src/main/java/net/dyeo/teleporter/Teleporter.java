package net.dyeo.teleporter;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/*
 * General Mod
 */
@Mod(modid = Reference.MODID, version = Reference.VERSION)
public class Teleporter
{
	// block definitions
	public static Block teleporterBlock;
	public static Block enderTeleporterBlock;
	
	// mod instance
	@Instance(Reference.MODID)
	public static Object instance;
	
	@SidedProxy(clientSide = "net.dyeo.teleporter.client.ClientProxy", serverSide = "net.dyeo.teleporter.common.CommonProxy")
    public static ISidedProxy proxy;
	
	//
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
		
		instance = this;
	}
	
	//
	@EventHandler
	public void init(FMLInitializationEvent event)
	{				
		proxy.init(event);
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{		
		proxy.load(event);
	}
	
}
