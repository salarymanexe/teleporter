package net.dyeo.teleporter;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandlerTeleporter implements IGuiHandler
{
	private static final int GUIID_MBE_30 = 30;

	public static int getGuiID()
	{
		return 30;
	}

	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID != getGuiID())
		{
			System.err.println("Invalid ID: expected " + getGuiID() + ", received " + ID);
		}
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if ((tileEntity instanceof TileEntityTeleporter))
		{
			TileEntityTeleporter tileEntityInventoryBasic = (TileEntityTeleporter)tileEntity;
			return new ContainerTeleporter(player.inventory, tileEntityInventoryBasic);
		}
		return null;
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID != getGuiID())
		{
			System.err.println("Invalid ID: expected " + getGuiID() + ", received " + ID);
		}
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if ((tileEntity instanceof TileEntityTeleporter))
		{
			TileEntityTeleporter tileEntityInventoryBasic = (TileEntityTeleporter)tileEntity;
			return new GuiTeleporter(player.inventory, tileEntityInventoryBasic);
		}
		return null;
	}
}
