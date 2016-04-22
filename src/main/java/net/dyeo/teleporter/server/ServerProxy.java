package net.dyeo.teleporter.server;

import net.dyeo.teleporter.ISidedProxy;
import net.dyeo.teleporter.common.CommonProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy implements ISidedProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event) 
	{
		super.preInit(event);
	}

	@Override
	public void init(FMLInitializationEvent event) 
	{
		super.init(event);
	}

	@Override
	public void load(FMLInitializationEvent event) 
	{
		super.load(event);
	}
}
