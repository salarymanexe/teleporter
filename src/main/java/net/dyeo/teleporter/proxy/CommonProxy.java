package net.dyeo.teleporter.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.dyeo.teleporter.Reference;
import net.dyeo.teleporter.Teleporter;
import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.dyeo.teleporter.event.EventTeleporter;
import net.dyeo.teleporter.gui.GuiHandlerRegistry;
import net.dyeo.teleporter.gui.GuiHandlerTeleporter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class CommonProxy implements ISidedProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		this.initConfiguration(event);
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		this.initBlocks();
		this.initRecipes();
	}

	@Override
	public void load(FMLInitializationEvent event)
	{
		this.registerEvents();
	}

	void initConfiguration(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.setCategoryRequiresMcRestart("general", true);

		config.load();

		config.addCustomCategoryComment("general", "Vanilla-Inspired Teleporters Version 1.5.0 Configuration");

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
	}

	void initBlocks()
	{
		Teleporter.teleporterBlock = new BlockTeleporter().setBlockName(Reference.teleporterBlockId).setBlockTextureName("teleporter:" + Reference.teleporterBlockId);
		GameRegistry.registerBlock(Teleporter.teleporterBlock, Reference.teleporterBlockId);
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, Reference.teleporterBlockId);

		Teleporter.teleporterBlock = new BlockTeleporter().setBlockName(Reference.enderTeleporterBlockId).setBlockTextureName("teleporter:" + Reference.enderTeleporterBlockId);
		GameRegistry.registerBlock(Teleporter.teleporterBlock, Reference.enderTeleporterBlockId);
		GameRegistry.registerTileEntity(TileEntityTeleporter.class, Reference.enderTeleporterBlockId);

		NetworkRegistry.INSTANCE.registerGuiHandler(Teleporter.instance, GuiHandlerRegistry.getInstance());
		GuiHandlerRegistry.getInstance().registerGuiHandler(new GuiHandlerTeleporter(), GuiHandlerTeleporter.getGuiID());
	}

	void initRecipes()
	{
		if (Reference.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(Teleporter.teleporterBlock, Reference.numTeleporters), new Object[] { "AAA", "DCD", "EBE",

					Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.iron_block, Character.valueOf('E'), Items.diamond });

			GameRegistry.addRecipe(new ItemStack(Teleporter.enderTeleporterBlock, Reference.numTeleporters), new Object[] { "AAA", "DCD", "EBE",

					Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_eye, Character.valueOf('C'), Blocks.glowstone, Character.valueOf('D'), Blocks.obsidian, Character.valueOf('E'), Items.diamond });
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(Teleporter.teleporterBlock, Reference.numTeleporters), new Object[] { "AAA", "DCD", "DBD",

					Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.iron_block });

			GameRegistry.addRecipe(new ItemStack(Teleporter.enderTeleporterBlock, Reference.numTeleporters), new Object[] { "AAA", "DCD", "DBD",

					Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_eye, Character.valueOf('C'), Blocks.glowstone, Character.valueOf('D'), Blocks.obsidian });
		}
	}

	void registerEvents()
	{
		MinecraftForge.EVENT_BUS.register(new EventTeleporter());
	}
}
