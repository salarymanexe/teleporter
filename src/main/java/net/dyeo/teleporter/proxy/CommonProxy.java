package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.event.TeleportEventHandler;
import net.dyeo.teleporter.init.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public void preInit()
	{
		ModConfiguration.preInit();
		this.registerSounds();
	}

	public void init()
	{
		this.registerEventHandler();
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

	private void registerEventHandler()
	{
		MinecraftForge.EVENT_BUS.register(new TeleportEventHandler());
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
