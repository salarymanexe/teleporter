package net.dyeo.teleporter;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
 
@Mod(modid = Reference.MODID, version = Reference.VERSION)
public class Teleporter
{
	
	public static Block teleporterBlock;
	
	public static Object instance;
	
	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		//FMLCommonHandler.instance().bus().register(events);
		//MinecraftForge.EVENT_BUS.register(events);
		instance = this;
		
		teleporterBlock = new BlockTeleporter().setUnlocalizedName("teleporter_teleporterBlock");
		GameRegistry.registerBlock(teleporterBlock, "teleporterBlock");
		
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "teleporterBlock");
		
		NetworkRegistry.INSTANCE.registerGuiHandler(Teleporter.instance, GuiHandlerRegistry.getInstance());
		GuiHandlerRegistry.getInstance().registerGuiHandler(new GuiHandlerTeleporter(), GuiHandlerTeleporter.getGuiID());
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{	
		
		GameRegistry.addRecipe(new ItemStack(teleporterBlock), new Object[]{
			"AAA",
     		"DCD",
     		"EBE",
     		'A', Blocks.glass,
     		'B', Items.ender_pearl,
     		'C', Blocks.redstone_block,
     		'D', Blocks.quartz_block,
     		'E', Items.diamond
		});
		
		//
		
		//Minecraft.installResource("sound3/teleporter/extremecow.ogg", new File(Minecraft.getMinecraft().mcDataDir, "resources/assets/teleporter/sounds/extremecow.ogg"));
		
		//
		
		if(event.getSide() == Side.CLIENT)
	    {
			Item itemBlockInventoryBasic = GameRegistry.findItem("teleporter", "teleporterBlock");
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("teleporter:teleporterBlock", "inventory");
			final int DEFAULT_ITEM_SUBTYPE = 0;
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlockInventoryBasic, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
    		
	    }
		
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new EventTeleporter());
	}
	
}
