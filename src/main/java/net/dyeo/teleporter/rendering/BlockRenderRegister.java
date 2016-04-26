package net.dyeo.teleporter.rendering;

import net.dyeo.teleporter.Reference;
import net.dyeo.teleporter.Teleporter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class BlockRenderRegister
{

	public static void init()
	{
		// "tutorial:block_properties_black", "tutorial:block_properties_white"
		ModelBakery.registerItemVariants(Item.getItemFromBlock(Teleporter.teleporterBlock),
				new ResourceLocation(Reference.MODID.toLowerCase(), Reference.teleporterBlockId),
				new ResourceLocation(Reference.MODID.toLowerCase(), Reference.enderTeleporterBlockId));
	}

	public static void reg(Block block)
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), 0,
				new ModelResourceLocation(Reference.MODID.toLowerCase() + ":" + block.getUnlocalizedName().substring(5),
						"inventory"));
	}

	public static void reg(Block block, int meta, String file)
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), meta,
				new ModelResourceLocation(Reference.MODID.toLowerCase() + ":" + file, "inventory"));
	}

	public static void registerBlockRenderer()
	{
		reg(Teleporter.teleporterBlock);
		reg(Teleporter.teleporterBlock, 0, Reference.teleporterBlockId);
		reg(Teleporter.teleporterBlock, 1, Reference.enderTeleporterBlockId);
	}

}
