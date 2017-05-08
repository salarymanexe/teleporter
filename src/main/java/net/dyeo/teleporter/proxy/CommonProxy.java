package net.dyeo.teleporter.proxy;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty;
import net.dyeo.teleporter.init.ModBlocks;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;

public class CommonProxy
{
	public void preInit()
	{
		registerBlocks();
		registerTileEntities();
	}

	public void init()
	{
		registerExtendedEntityProperties();
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

	private void registerExtendedEntityProperties()
	{
		TeleportEntityProperty.registerProperty();
	}


	private void registerGuiHandler()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(TeleporterMod.instance, new GuiHandler());
	}

	private void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, TileEntityTeleporter.class.getSimpleName());
	}


	private void registerCraftingRecipes()
	{
		ModBlocks.registerCraftingRecipes();
	}

}
