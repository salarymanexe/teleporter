package net.dyeo.teleporter.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.item.ItemBlockTeleporter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModBlocks
{

	public static final Block teleporterBlock = new BlockTeleporter().setBlockName("teleporterBlock").setBlockTextureName(TeleporterMod.MODID + ":" + "teleporterBlock");
//	public static final Block enderTeleporterBlock = new BlockEnderTeleporter().setBlockName("enderTeleporterBlock").setBlockTextureName(TeleporterMod.MODID + ":" + "enderTeleporterBlock");


	public static void registerBlocks()
	{
		GameRegistry.registerBlock(teleporterBlock, ItemBlockTeleporter.class, teleporterBlock.getUnlocalizedName().substring(5));
//		GameRegistry.registerBlock(enderTeleporterBlock, ItemBlock.class, enderTeleporterBlock.getUnlocalizedName().substring(5));
	}

	public static void registerCraftingRecipes()
	{
		if (ModConfiguration.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock, ModConfiguration.numTeleporters),
				new Object[] { "AAA", "DCD", "EBE", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.iron_block, Character.valueOf('E'), Items.diamond }
			);
//			GameRegistry.addRecipe(new ItemStack(enderTeleporterBlock, ModConfiguration.numTeleporters),
//				new Object[] { "AAA", "DCD", "EBE", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_eye, Character.valueOf('C'), Blocks.glowstone, Character.valueOf('D'), Blocks.obsidian, Character.valueOf('E'), Items.diamond }
//			);
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock, ModConfiguration.numTeleporters),
				new Object[] { "AAA", "DCD", "DBD", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.iron_block }
			);
//			GameRegistry.addRecipe(new ItemStack(enderTeleporterBlock, ModConfiguration.numTeleporters),
//				new Object[] { "AAA", "DCD", "DBD", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_eye, Character.valueOf('C'), Blocks.glowstone, Character.valueOf('D'), Blocks.obsidian }
//			);
		}
	}

}
