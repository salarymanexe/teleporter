package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.init.ModBlocks;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy
{
	public void preInit()
	{
		this.registerSounds();
		this.registerTileEntities();
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

	private void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, TileEntityTeleporter.class.getSimpleName());
	}
}
