package net.dyeo.teleporter.client;

import net.dyeo.teleporter.ISidedProxy;
import net.dyeo.teleporter.Reference;
import net.dyeo.teleporter.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ClientProxy extends CommonProxy implements ISidedProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) 
	{
		super.preInit(event);
	}

	@Override
	public void init(FMLInitializationEvent event) 
	{
		super.init(event);
		
		registerRenderers();
	}

	@Override
	public void load(FMLInitializationEvent event) 
	{
		super.load(event);		
	}
	
	void registerRenderers()
	{
		final int DEFAULT_ITEM_SUBTYPE = 0;
		
		//
		Item itemBlockTeleporter = GameRegistry.findItem(Reference.MODID.toLowerCase(), "teleporterBlock");
		ModelResourceLocation itemTeleporterModelResourceLocation = new ModelResourceLocation(Reference.MODID.toLowerCase() + ":teleporterBlock", "inventory");
		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlockTeleporter, DEFAULT_ITEM_SUBTYPE, itemTeleporterModelResourceLocation);
		
		//
		Item itemBlockEnderTeleporter = GameRegistry.findItem(Reference.MODID.toLowerCase(), "enderTeleporterBlock");
		ModelResourceLocation itemEnderTeleporterModelResourceLocation = new ModelResourceLocation(Reference.MODID.toLowerCase() + ":enderTeleporterBlock", "inventory");
		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlockEnderTeleporter, DEFAULT_ITEM_SUBTYPE, itemEnderTeleporterModelResourceLocation);
	}

}
