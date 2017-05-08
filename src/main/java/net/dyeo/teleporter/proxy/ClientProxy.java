package net.dyeo.teleporter.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.dyeo.teleporter.client.renderer.ItemRendererTeleporter;
import net.dyeo.teleporter.client.renderer.tileentity.RenderTeleporter;
import net.dyeo.teleporter.init.ModBlocks;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

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
	}


	private void registerRenderers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderTeleporter());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.teleporterBlock), new ItemRendererTeleporter());
	}
}
