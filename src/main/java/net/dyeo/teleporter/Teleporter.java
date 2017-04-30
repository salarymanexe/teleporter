package net.dyeo.teleporter;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@Mod(modid = "teleporter", version = "1.0.2")
public class Teleporter
{
	public static Block teleporterBlock;
	public static Object instance;
	@SidedProxy(clientSide = "net.dyeo.teleporter.TeleporterClientProxy", serverSide = "net.dyeo.teleporter.TeleporterCommonProxy")
	public static TeleporterCommonProxy proxy;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		instance = this;

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.setCategoryRequiresMcRestart("general", true);

		config.load();

		config.addCustomCategoryComment("general", "Vanilla-Inspired Teleporters Version 1.7.10-1.0.2 Configuration");

		Property useDiamondsP = config.get("general", "useDiamonds", true);
		useDiamondsP.comment = "If false, removes diamonds from the crafting recipe and replaces them with quartz blocks.\nDefault is true";

		Property numTeleportersP = config.get("general", "numTeleporters", 1);
		numTeleportersP.comment = "Specifies the number of teleporters created with a single recipe.\nDefault is 1";

		Property teleportPassiveMobsP = config.get("general", "teleportPassiveMobs", true);
		teleportPassiveMobsP.comment = "Specifies whether or not passive mobs can go through teleporters.\nDefault is true";

		Property teleportHostileMobsP = config.get("general", "teleportHostileMobs", true);
		teleportHostileMobsP.comment = "Specifies whether or not hostile mobs can go through teleporters.\nDefault is true";

		Reference.useDiamonds = useDiamondsP.getBoolean(true);
		Reference.numTeleporters = numTeleportersP.getInt(1);
		Reference.teleportPassiveMobs = teleportPassiveMobsP.getBoolean(true);
		Reference.teleportHostileMobs = teleportHostileMobsP.getBoolean(true);

		config.save();

		teleporterBlock = new BlockTeleporter().setUnlocalizedName("teleporterBlock").setTextureName("teleporter:teleporterBlock");

		GameRegistry.registerBlock(teleporterBlock, "teleporterBlock");
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, "teleporterBlock");

		proxy.registerRenderers();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, GuiHandlerRegistry.getInstance());
		GuiHandlerRegistry.getInstance().registerGuiHandler(new GuiHandlerTeleporter(), GuiHandlerTeleporter.getGuiID());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		if (Reference.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock, Reference.numTeleporters), new Object[] { "AAA", "DCD", "EBE", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.quartz_block, Character.valueOf('E'), Items.diamond });
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock, Reference.numTeleporters), new Object[] { "AAA", "DCD", "DBD", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.quartz_block });
		}
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new EventTeleporter());
	}
}
