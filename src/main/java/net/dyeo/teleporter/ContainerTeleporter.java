package net.dyeo.teleporter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTeleporter extends Container
{
	private TileEntityTeleporter tileEntityTeleporter;
	private final int HOTBAR_SLOT_COUNT = 9;
	private final int PLAYER_INVENTORY_ROW_COUNT = 3;
	private final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
	private final int PLAYER_INVENTORY_SLOT_COUNT = 27;
	private final int VANILLA_SLOT_COUNT = 36;
	private final int VANILLA_FIRST_SLOT_INDEX = 0;
	private final int TE_INVENTORY_FIRST_SLOT_INDEX = 36;
	private final int TE_INVENTORY_SLOT_COUNT = 1;

	public ContainerTeleporter(InventoryPlayer invPlayer, TileEntityTeleporter tileEntityTeleporter)
	{
		this.tileEntityTeleporter = tileEntityTeleporter;

		int SLOT_X_SPACING = 18;
		int SLOT_Y_SPACING = 18;
		int HOTBAR_XPOS = 8;
		int HOTBAR_YPOS = 142;
		for (int x = 0; x < 9; x++)
		{
			int slotNumber = x;
			addSlotToContainer(new Slot(invPlayer, slotNumber, 8 + 18 * x, 142));
		}
		int PLAYER_INVENTORY_XPOS = 8;
		int PLAYER_INVENTORY_YPOS = 84;
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				int slotNumber = 9 + y * 9 + x;
				int xpos = 8 + x * 18;
				int ypos = 84 + y * 18;
				addSlotToContainer(new Slot(invPlayer, slotNumber, xpos, ypos));
			}
		}
		if (1 != tileEntityTeleporter.getSizeInventory())
		{
			System.err.println("Mismatched slot count in ContainerTeleporter(1) and TileTeleporter (" + tileEntityTeleporter.getSizeInventory() + ")");
		}
		int TILE_INVENTORY_XPOS = 79;
		int TILE_INVENTORY_YPOS = 35;
		for (int x = 0; x < 1; x++)
		{
			int slotNumber = x;
			addSlotToContainer(new Slot(tileEntityTeleporter, slotNumber, 79 + 18 * x, 35));
		}
	}

	public boolean canInteractWith(EntityPlayer player)
	{
		return this.tileEntityTeleporter.isUseableByPlayer(player);
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int sourceSlotIndex)
	{
		Slot sourceSlot = (Slot)this.inventorySlots.get(sourceSlotIndex);
		if ((sourceSlot == null) || (!sourceSlot.getHasStack()))
		{
			return null;
		}
		ItemStack sourceStack = sourceSlot.getStack();
		ItemStack copyOfSourceStack = sourceStack.copy();
		if ((sourceSlotIndex >= 0) && (sourceSlotIndex < 36))
		{
			if (!mergeItemStack(sourceStack, 36, 37, false))
			{
				return null;
			}
		}
		else if ((sourceSlotIndex >= 36) && (sourceSlotIndex < 37))
		{
			if (!mergeItemStack(sourceStack, 0, 36, false))
			{
				return null;
			}
		}
		else
		{
			System.err.print("Invalid slotIndex:" + sourceSlotIndex);
			return null;
		}
		if (sourceStack.stackSize == 0)
		{
			sourceSlot.putStack(null);
		}
		else
		{
			sourceSlot.onSlotChanged();
		}
		sourceSlot.onPickupFromSlot(player, sourceStack);
		return copyOfSourceStack;
	}

	public void onContainerClosed(EntityPlayer playerIn)
	{
		super.onContainerClosed(playerIn);
		this.tileEntityTeleporter.closeChest();
	}
}
