package net.dyeo.teleporter.init;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.block.BlockTeleporter.EnumType;
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

	public static final Block TELEPORTER = new BlockTeleporter().setRegistryName("teleporter").setUnlocalizedName("teleporter");


	public static void registerBlocks()
	{
		GameRegistry.register(TELEPORTER);
		GameRegistry.register(new ItemBlockTeleporter(TELEPORTER).setRegistryName(TELEPORTER.getRegistryName()));
	}


	public static void registerBlockVariants()
	{
		ModelBakery.registerItemVariants(
			Item.getItemFromBlock(ModBlocks.TELEPORTER),
			new ResourceLocation(TeleporterMod.MODID, EnumType.REGULAR.getRegistryName()),
			new ResourceLocation(TeleporterMod.MODID, EnumType.ENDER.getRegistryName()),
			new ResourceLocation(TeleporterMod.MODID, EnumType.RECALL.getRegistryName()),
			new ResourceLocation(TeleporterMod.MODID, EnumType.RECALL_ENDER.getRegistryName())
		);
	}

	public static void registerInventoryModels()
	{
		Item item = Item.getItemFromBlock(TELEPORTER);
		for ( EnumType type : EnumType.values() )
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, type.getMetadata(), new ModelResourceLocation(TeleporterMod.MODID + ":" + type.getRegistryName(), "inventory"));
		}
	}


	public static void registerCraftingRecipes()
	{
		if (ModConfiguration.useDiamonds == true)
		{
			// teleporter (diamonds)
			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 0),
					new Object[] { "AAA", 
								   "DCD", 
								   "EBE", 
								   'A', Blocks.GLASS, 
								   'B', Items.ENDER_PEARL, 
								   'C', Blocks.REDSTONE_BLOCK, 
								   'D', Blocks.IRON_BLOCK, 
								   'E', Items.DIAMOND });

			// ender teleporter (diamonds)
			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 1),
					new Object[] { "AAA", 
								   "DCD", 
								   "EBE", 
								   'A', Blocks.GLASS, 
								   'B', Items.ENDER_EYE, 
								   'C', Blocks.GLOWSTONE,
								   'D', Blocks.OBSIDIAN, 
								   'E', Items.DIAMOND });
		}
		else
		{
			// teleporter (no diamonds)
			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 0),
					new Object[] { "AAA", 
								   "DCD", 
								   "DBD", 
								   'A', Blocks.GLASS, 
								   'B', Items.ENDER_PEARL, 
								   'C',	Blocks.REDSTONE_BLOCK, 
								   'D', Blocks.IRON_BLOCK });

			// ender teleporter (no diamonds)
			GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 1),
					new Object[] { "AAA", 
								   "DCD", 
								   "DBD", 
								   'A', Blocks.GLASS, 
								   'B', Items.ENDER_EYE, 
								   'C', Blocks.GLOWSTONE,
								   'D', Blocks.OBSIDIAN });
		}
		
		// recall teleporter
		GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 2),
				new Object[] { "AAA", 
							   "ECE", 
							   "DBD", 
							   'A', ItemStack.EMPTY, 
							   'B', Items.ENDER_PEARL, 
							   'C',	Blocks.REDSTONE_BLOCK, 
							   'D', Blocks.IRON_BLOCK,
							   'E', Blocks.GLASS });

		// recall ender teleporter
		GameRegistry.addRecipe(new ItemStack(ModBlocks.TELEPORTER, ModConfiguration.numTeleporters, 3),
				new Object[] { "AAA", 
							   "ECE", 
							   "DBD", 
							   'A', ItemStack.EMPTY,
							   'B', Items.ENDER_EYE, 
							   'C', Blocks.GLOWSTONE,
							   'D', Blocks.OBSIDIAN,
							   'E', Blocks.GLASS });
	}


}
