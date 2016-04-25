package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.rendering.BlockRenderRegister;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy implements ISidedProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) 
	{
		super.preInit(event);
		
	}

	@Override
	public void init(FMLInitializationEvent event) 
	{
		super.init(event);

	    BlockRenderRegister.init();
	    
		registerRenderers();
	}

	@Override
	public void load(FMLInitializationEvent event) 
	{
		super.load(event);		
	}
	
	void registerRenderers()
	{		
		BlockRenderRegister.registerBlockRenderer();
	}

}
