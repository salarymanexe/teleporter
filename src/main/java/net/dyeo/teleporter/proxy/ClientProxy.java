package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.client.update.VersionChecker;
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
		this.registerRenderers();
	}

	@Override
	public void postInit()
	{
		super.postInit();
		this.registerVersionCheck();
	}


	private void registerRenderers()
	{
		ModBlocks.registerRenderers();
	}

	private void registerVersionCheck()
	{
		VersionChecker.register();
	}
}
