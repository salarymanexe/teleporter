package net.dyeo.teleporter.proxy;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty;
import net.dyeo.teleporter.init.ModBlocks;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;

public class CommonProxy
{

	public void preInit()
	{
		this.registerBlocks();
		this.registerSounds();
		this.registerTileEntities();
	}

	public void init()
	{
		this.registerExtendedEntityProperties();
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

	private void registerCraftingRecipes()
	{
		ModBlocks.registerCraftingRecipes();
	}

	private void registerExtendedEntityProperties()
	{
		TeleportEntityProperty.registerProperty();
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