package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.init.ModSounds;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public void preInit()
	{
		this.registerSounds();
	}

	public void init()
	{
		this.registerCapabilities();
		this.registerGuiHandler();
	}

	public void postInit()
	{
	}

	private void registerCapabilities()
	{
		CapabilityTeleportHandler.registerCapabilities();
	}

	private void registerGuiHandler()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(TeleporterMod.instance, new GuiHandler());
	}

	private void registerSounds()
	{
		ModSounds.registerSounds();
	}
}
