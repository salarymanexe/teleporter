package net.dyeo.teleporter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/*
 * General Mod
 */
@Mod(modid = Reference.MODID, version = Reference.VERSION)
public class Teleporter
{
	
	public static Block teleporterBlock;
	
	public static Object instance;
	
	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.setCategoryRequiresMcRestart(Configuration.CATEGORY_GENERAL, true);

		config.load();
		
		config.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, "Vanilla-Inspired Teleporters Version " + Minecraft.getMinecraft().getVersion() + "-" + Reference.VERSION + " Configuration");
		
		//Property useCustomTexturesP = config.get(config.CATEGORY_GENERAL, "useCustomTextures", false);
		//useCustomTexturesP.comment = "If true, allows the use of custom texture names, rather than the default minecraft textures.\nDefault is false";
		
		Property useDiamondsP = config.get(Configuration.CATEGORY_GENERAL, "useDiamonds", true);
		useDiamondsP.comment = "If false, removes diamonds from the crafting recipe and replaces them with quartz blocks.\nDefault is true";
		
		Property numTeleportersP = config.get(Configuration.CATEGORY_GENERAL, "numTeleporters", 1);
		numTeleportersP.comment = "Specifies the number of teleporters created with a single recipe.\nDefault is 1";
		
		Property teleportPassiveMobsP = config.get(Configuration.CATEGORY_GENERAL, "teleportPassiveMobs", true);
		teleportPassiveMobsP.comment = "Specifies whether or not passive mobs can go through teleporters.\nDefault is true";
		
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
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{	
		teleporterBlock = new BlockTeleporter().setUnlocalizedName(Reference.MODID.toLowerCase() + "_teleporterBlock");
		GameRegistry.registerBlock(teleporterBlock, "teleporterBlock");
				
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "teleporterBlock");
		
		NetworkRegistry.INSTANCE.registerGuiHandler(Teleporter.instance, GuiHandlerRegistry.getInstance());
		GuiHandlerRegistry.getInstance().registerGuiHandler(new GuiHandlerTeleporter(), GuiHandlerTeleporter.getGuiID());
		
		if(Reference.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock,Reference.numTeleporters), 
				new Object[]{
				"AAA",
     			"DCD",
     			"EBE",
     			'A', Blocks.glass,
     			'B', Items.ender_pearl,
     			'C', Blocks.redstone_block,
     			'D', Blocks.quartz_block,
     			'E', Items.diamond
			});
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock,Reference.numTeleporters), 
				new Object[]{
				"AAA",
	     		"DCD",
	     		"DBD",
	     		'A', Blocks.glass,
	     		'B', Items.ender_pearl,
	     		'C', Blocks.redstone_block,
	     		'D', Blocks.quartz_block
			});
		}
		
		//
		
		if(event.getSide() == Side.CLIENT)
	    {
			Item itemBlockInventoryBasic = GameRegistry.findItem(Reference.MODID.toLowerCase(), "teleporterBlock");
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(Reference.MODID.toLowerCase() + ":teleporterBlock", "inventory");
			
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
