package net.dyeo.teleporter.init;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.client.renderer.ItemRendererTeleporter;
import net.dyeo.teleporter.client.renderer.tileentity.RenderTeleporter;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.item.ItemBlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;

public class ModBlocks
{

	public static final Block teleporterBlock = new BlockTeleporter().setBlockName("teleporterBlock").setBlockTextureName(TeleporterMod.MODID + ":" + "teleporterBlock");


	public static void registerBlocks()
	{
		GameRegistry.registerBlock(teleporterBlock, ItemBlockTeleporter.class, teleporterBlock.getUnlocalizedName().substring(5));
	}

	public static void registerCraftingRecipes()
	{
		if (ModConfiguration.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock, ModConfiguration.numTeleporters),
				new Object[] { "AAA", "DCD", "EBE", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.iron_block, Character.valueOf('E'), Items.diamond }
			);
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(teleporterBlock, ModConfiguration.numTeleporters),
				new Object[] { "AAA", "DCD", "DBD", Character.valueOf('A'), Blocks.glass, Character.valueOf('B'), Items.ender_pearl, Character.valueOf('C'), Blocks.redstone_block, Character.valueOf('D'), Blocks.iron_block }
			);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerRenderers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderTeleporter());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.teleporterBlock), new ItemRendererTeleporter());
	}

}
