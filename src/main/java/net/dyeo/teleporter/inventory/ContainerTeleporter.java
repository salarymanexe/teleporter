package net.dyeo.teleporter.inventory;

import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;


public class ContainerTeleporter extends Container
{
	private final TileEntityTeleporter tileEntity;

	public ContainerTeleporter(InventoryPlayer playerInventory, TileEntityTeleporter tileEntity)
	{
		this.tileEntity = tileEntity;
		IItemHandler itemHandler = this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		// container slot
		this.addSlotToContainer(new SlotItemHandler(itemHandler, 0, 79, 35));

		// player inventory slots
		int offsetX = 8;
		int offsetY = 85;
		for (int row = 0; row < 3; ++row)
		{
			for (int col = 0; col < 9; ++col)
			{
				int index = col + (row * 9) + 9;
				int xPosition = offsetX + (col * 18);
				int yPosition = offsetY + (row * 18);
				this.addSlotToContainer(new Slot(playerInventory, index, xPosition, yPosition));
			}
		}

		// player hotbar slots
		offsetY = 143;
		for (int col = 0; col < 9; ++col)
		{
			int index = col;
			int xPosition = offsetX + (col * 18);
			int yPosition = offsetY;
			this.addSlotToContainer(new Slot(playerInventory, index, xPosition, yPosition));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return this.tileEntity.canInteractWith(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < 1)
			{
				if (!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 0, 1, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0)
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

}
