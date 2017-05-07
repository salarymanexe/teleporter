package net.dyeo.teleporter.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public abstract interface ISidedProxy
{
	public abstract void preInit(FMLPreInitializationEvent paramFMLPreInitializationEvent);

	public abstract void init(FMLInitializationEvent paramFMLInitializationEvent);

	public abstract void load(FMLInitializationEvent paramFMLInitializationEvent);
}
