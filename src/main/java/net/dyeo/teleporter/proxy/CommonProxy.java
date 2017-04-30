package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityHandler;
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
		registerBlocks();
		registerSounds();
		registerTileEntities();
	}

	public void init()
	{
		registerCapabilities();
		registerCraftingRecipes();
		registerGuiHandler();
	}

	public void postInit()
	{
	}




	private void registerBlocks()
	{
		ModBlocks.registerBlocks();
	}

	private void registerCapabilities()
	{
		CapabilityHandler.registerCapabilities();
	}

	private void registerCraftingRecipes()
	{
		ModBlocks.registerCraftingRecipes();
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
