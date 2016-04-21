package net.dyeo.teleporter;

import net.dyeo.teleporter.blocks.BlockEnderTeleporter;
import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.dyeo.teleporter.event.EventTeleporter;
import net.dyeo.teleporter.gui.GuiHandlerRegistry;
import net.dyeo.teleporter.gui.GuiHandlerTeleporter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

/*
 * General Mod
 */
@Mod(modid = Reference.MODID, version = Reference.VERSION)
public class Teleporter
{
	// block definitions
	public static Block teleporterBlock;
	public static Block enderTeleporterBlock;
	
	// mod instance
	@Instance(Reference.MODID)
	public static Object instance;
	
	@SidedProxy(clientSide = "net.dyeo.teleporter.client.ClientProxy", serverSide = "net.dyeo.teleporter.server.ServerProxy")
    public static ISidedProxy proxy;
	
	//
	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		// create the configuration
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		// changing config requires restart
		config.setCategoryRequiresMcRestart(Configuration.CATEGORY_GENERAL, true);

		// load whatever is in the config file now
		config.load();
		
		config.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, "Vanilla-Inspired Teleporters Version " + Reference.VERSION + " Configuration");
		
		//Property useCustomTexturesP = config.get(config.CATEGORY_GENERAL, "useCustomTextures", false);
		//useCustomTexturesP.comment = "If true, allows the use of custom texture names, rather than the default minecraft textures.\nDefault is false";
		
		// use diamonds in recipe
		Property useDiamondsP = config.get(Configuration.CATEGORY_GENERAL, "useDiamonds", true);
		useDiamondsP.comment = "If false, removes diamonds from the crafting recipe and replaces them with quartz blocks.\nDefault is true";
		
		// number of teleporters from recipe
		Property numTeleportersP = config.get(Configuration.CATEGORY_GENERAL, "numTeleporters", 1);
		numTeleportersP.comment = "Specifies the number of teleporters created with a single recipe.\nDefault is 1";
		
		// teleport passive mobs
		Property teleportPassiveMobsP = config.get(Configuration.CATEGORY_GENERAL, "teleportPassiveMobs", true);
		teleportPassiveMobsP.comment = "Specifies whether or not passive mobs can go through teleporters.\nDefault is true";
		
		// teleport hostile mobs
		Property teleportHostileMobsP = config.get(Configuration.CATEGORY_GENERAL, "teleportHostileMobs", true);
		teleportHostileMobsP.comment = "Specifies whether or not hostile mobs can go through teleporters.\nDefault is true";
		
		//Reference.useCustomTextures = useCustomTexturesP.getBoolean(false);
		Reference.useDiamonds = useDiamondsP.getBoolean(true);
		Reference.numTeleporters = numTeleportersP.getInt(1);
		Reference.teleportPassiveMobs = teleportPassiveMobsP.getBoolean(true);
		Reference.teleportHostileMobs = teleportHostileMobsP.getBoolean(true);
		
		config.save();
		
		instance = this;
	}
	
	//
	@EventHandler
	public void init(FMLInitializationEvent event)
	{	
		// register the teleporter block
		teleporterBlock = new BlockTeleporter().setUnlocalizedName(Reference.MODID.toLowerCase() + "_" + Reference.teleporterBlockId);
		GameRegistry.registerBlock(teleporterBlock, "teleporterBlock");
				
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "teleporterBlock");
		
		NetworkRegistry.INSTANCE.registerGuiHandler(Teleporter.instance, GuiHandlerRegistry.getInstance());
		GuiHandlerRegistry.getInstance().registerGuiHandler(new GuiHandlerTeleporter(), GuiHandlerTeleporter.getGuiID());
		
		// register the ender teleporter block
		enderTeleporterBlock = new BlockEnderTeleporter().setUnlocalizedName(Reference.MODID.toLowerCase() + "_" + Reference.enderTeleporterBlockId);
		GameRegistry.registerBlock(enderTeleporterBlock, "enderTeleporterBlock");
				
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "enderTeleporterBlock");
		
		NetworkRegistry.INSTANCE.registerGuiHandler(Teleporter.instance, GuiHandlerRegistry.getInstance());
		GuiHandlerRegistry.getInstance().registerGuiHandler(new GuiHandlerTeleporter(), GuiHandlerTeleporter.getGuiID());
		
		// register recipes
		
		if(Reference.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock, Reference.numTeleporters), new Object[]{
				"AAA",
     			"DCD",
     			"EBE",
     			'A', Blocks.glass,
     			'B', Items.ender_pearl,
     			'C', Blocks.redstone_block,
     			'D', Blocks.iron_block,
     			'E', Items.diamond
			});
			
			GameRegistry.addRecipe(new ItemStack(enderTeleporterBlock,Reference.numTeleporters), new Object[]{
				"AAA",
	     		"DCD",
	     		"EBE",
	     		'A', Blocks.glass,
	     		'B', Items.ender_eye,
	     		'C', Blocks.glowstone,
	     		'D', Blocks.obsidian,
	     		'E', Items.diamond
			});
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock,Reference.numTeleporters), new Object[]{
				"AAA",
	     		"DCD",
	     		"DBD",
	     		'A', Blocks.glass,
	     		'B', Items.ender_pearl,
	     		'C', Blocks.redstone_block,
	     		'D', Blocks.iron_block
			});
			
			GameRegistry.addRecipe(new ItemStack(enderTeleporterBlock,Reference.numTeleporters), new Object[]{
				"AAA",
		    	"DCD",
		    	"DBD",
		    	'A', Blocks.glass,
		    	'B', Items.ender_eye,
		    	'C', Blocks.glowstone,
		    	'D', Blocks.obsidian
			});
		}
		
		proxy.init();
				
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new EventTeleporter());
	}
	
}
