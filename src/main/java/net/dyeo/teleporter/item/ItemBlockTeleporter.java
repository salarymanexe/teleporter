package net.dyeo.teleporter.item;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

public class ItemBlockTeleporter extends ItemBlockWithMetadata
{

	public ItemBlockTeleporter(Block block)
	{
		super(block, block);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "tile." + BlockTeleporter.EnumType.byMetadata(stack.getItemDamage()).getUnlocalizedName();
	}

}
