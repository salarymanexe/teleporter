package net.dyeo.teleporter;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

public class TeleporterClientProxy extends TeleporterCommonProxy
{
	public void registerRenderers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderBlockTeleporter());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Teleporter.teleporterBlock), new RenderItemTeleporter());
	}
}
