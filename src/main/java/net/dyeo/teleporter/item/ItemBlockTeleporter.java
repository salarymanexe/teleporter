package net.dyeo.teleporter.item;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;

public class ItemBlockTeleporter extends ItemSlab
{

	public ItemBlockTeleporter(final BlockTeleporter block)
	{
		super(block, block, block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setRegistryName(block.getRegistryName());
	}

	@Override
	public int getMetadata(int meta)
	{
		return meta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "tile." + BlockTeleporter.EnumType.fromMetadata(stack.getMetadata()).getUnlocalizedName();
	}
}
