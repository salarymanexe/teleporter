package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.init.ModBlocks;

public class ClientProxy extends CommonProxy
{

	@Override
	public void preInit()
	{
		super.preInit();
	}

	@Override
	public void init()
	{
		super.init();
		registerBlockVariants();
		registerInventoryModels();
	}

	@Override
	public void postInit()
	{
		super.postInit();
	}



	private void registerBlockVariants()
	{
		ModBlocks.registerBlockVariants();
	}

	private void registerInventoryModels()
	{
		ModBlocks.registerInventoryModels();
	}

}
