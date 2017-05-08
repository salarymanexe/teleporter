package net.dyeo.teleporter.init;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.block.BlockTeleporter.EnumType;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.item.ItemBlockTeleporter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks
{

	public static final Block teleporter = new BlockTeleporter().setUnlocalizedName(EnumType.REGULAR.getUnlocalizedName());

	public static void registerBlocks()
	{
		GameRegistry.registerBlock(teleporter, ItemBlockTeleporter.class, EnumType.REGULAR.getUnlocalizedName());
	}

	public static void registerBlockVariants()
	{
		ModelBakery.registerItemVariants(Item.getItemFromBlock(teleporter),
			new ResourceLocation(TeleporterMod.MODID, EnumType.REGULAR.getUnlocalizedName()),
			new ResourceLocation(TeleporterMod.MODID, EnumType.ENDER.getUnlocalizedName())
		);
	}

	public static void registerInventoryModels()
	{
		Item item = Item.getItemFromBlock(teleporter);
		for ( EnumType type : EnumType.values() )
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, type.getMetadata(), new ModelResourceLocation(TeleporterMod.MODID + ":" + type.getUnlocalizedName(), "inventory"));
		}
	}

	public static void registerCraftingRecipes()
	{
		if (ModConfiguration.useDiamonds == true)
		{
			GameRegistry.addRecipe(new ItemStack(teleporter, ModConfiguration.numTeleporters, 0),
				new Object[] { "AAA", "DCD", "EBE", 'A', Blocks.glass, 'B', Items.ender_pearl, 'C', Blocks.redstone_block, 'D', Blocks.iron_block, 'E', Items.diamond }
			);
			GameRegistry.addRecipe(new ItemStack(teleporter, ModConfiguration.numTeleporters, 1),
				new Object[] { "AAA", "DCD", "EBE", 'A', Blocks.glass, 'B', Items.ender_eye, 'C', Blocks.glowstone, 'D', Blocks.obsidian, 'E', Items.diamond }
			);
		}
		else
		{
			GameRegistry.addRecipe(new ItemStack(teleporter, ModConfiguration.numTeleporters, 0),
				new Object[] { "AAA", "DCD", "DBD", 'A', Blocks.glass, 'B', Items.ender_pearl, 'C', Blocks.redstone_block, 'D', Blocks.iron_block }
			);
			GameRegistry.addRecipe(new ItemStack(teleporter, ModConfiguration.numTeleporters, 1),
				new Object[] { "AAA", "DCD", "DBD", 'A', Blocks.glass, 'B', Items.ender_eye, 'C', Blocks.glowstone, 'D', Blocks.obsidian }
			);
		}
	}

}
