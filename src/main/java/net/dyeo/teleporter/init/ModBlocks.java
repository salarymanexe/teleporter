package net.dyeo.teleporter.init;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.item.ItemBlockTeleporter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks
{

	public static final Block TELEPORTER = new BlockTeleporter().setRegistryName("teleporterBlock").setUnlocalizedName("teleporterBlock");

	public static String teleporterBlockId = "teleporterBlock";
	public static String enderTeleporterBlockId = "enderTeleporterBlock";


	public static void registerBlocks()
	{
		GameRegistry.register(TELEPORTER);
		GameRegistry.register(new ItemBlockTeleporter(TELEPORTER).setRegistryName(TELEPORTER.getRegistryName()));
	}


	public static void registerBlockVariants()
	{
		ModelBakery.registerItemVariants(Item.getItemFromBlock(ModBlocks.TELEPORTER),
			new ResourceLocation(TeleporterMod.MODID, teleporterBlockId),
			new ResourceLocation(TeleporterMod.MODID, enderTeleporterBlockId)
		);
	}

	public static void registerInventoryModels()
	{
		Item item = Item.getItemFromBlock(TELEPORTER);

		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(TeleporterMod.MODID + ":" + teleporterBlockId, "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 1, new ModelResourceLocation(TeleporterMod.MODID + ":" + enderTeleporterBlockId, "inventory"));

	}


	public static void registerCraftingRecipes()
	{
		if (ModConfiguration.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 0),
					new Object[] { "AAA", "DCD", "EBE", 'A', Blocks.GLASS, 'B', Items.ENDER_PEARL, 'C',
							Blocks.REDSTONE_BLOCK, 'D', Blocks.IRON_BLOCK, 'E', Items.DIAMOND });

			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 1),
					new Object[] { "AAA", "DCD", "EBE", 'A', Blocks.GLASS, 'B', Items.ENDER_EYE, 'C', Blocks.GLOWSTONE,
							'D', Blocks.OBSIDIAN, 'E', Items.DIAMOND });
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 0),
					new Object[] { "AAA", "DCD", "DBD", 'A', Blocks.GLASS, 'B', Items.ENDER_PEARL, 'C',
							Blocks.REDSTONE_BLOCK, 'D', Blocks.IRON_BLOCK });

			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 1),
					new Object[] { "AAA", "DCD", "DBD", 'A', Blocks.GLASS, 'B', Items.ENDER_EYE, 'C', Blocks.GLOWSTONE,
							'D', Blocks.OBSIDIAN });
		}
	}


}
