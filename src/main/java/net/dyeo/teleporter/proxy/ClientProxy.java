package net.dyeo.teleporter.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy implements ISidedProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);

		this.registerRenderers();
	}

	@Override
	public void load(FMLInitializationEvent event)
	{
		super.load(event);
	}

	void registerRenderers()
	{
//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderBlockTeleporter("textures/blocks/teleporterBlock.png", "textures/blocks/teleporterBlock.obj"));
//		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Teleporter.teleporterBlock), new RenderItemTeleporter());

//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderBlockTeleporter("textures/blocks/teleporterBlock.png", "models/block/teleporterBlock.obj"));
//		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(Teleporter.teleporterBlock), new RenderItemTeleporter());
	}
}
