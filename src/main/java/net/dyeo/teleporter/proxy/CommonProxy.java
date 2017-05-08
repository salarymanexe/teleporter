package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.init.ModBlocks;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy
{

	public void preInit()
	{
		this.registerBlocks();
		this.registerTileEntities();
	}

	public void init()
	{
		this.registerCapabilities();
		this.registerCraftingRecipes();
		this.registerGuiHandler();
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
		CapabilityTeleportHandler.registerCapabilities();
	}

	private void registerCraftingRecipes()
	{
		ModBlocks.registerCraftingRecipes();
	}

	private void registerGuiHandler()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(TeleporterMod.instance, new GuiHandler());
	}

	private void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, TileEntityTeleporter.class.getSimpleName());
	}

}
