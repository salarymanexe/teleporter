package net.dyeo.teleporter.item;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockTeleporter extends ItemBlock
{

	public ItemBlockTeleporter(Block block)
	{
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta)
	{
		return meta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return BlockTeleporter.EnumType.byMetadata(stack.getMetadata()).getUnlocalizedName();
	}
}
