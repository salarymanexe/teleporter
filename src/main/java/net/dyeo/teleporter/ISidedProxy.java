package net.dyeo.teleporter;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface ISidedProxy {

	public void preInit(FMLPreInitializationEvent event);

	public void init(FMLInitializationEvent event);
	
	public void load(FMLInitializationEvent event);
	
}
