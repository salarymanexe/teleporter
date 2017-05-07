package net.dyeo.teleporter.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import net.dyeo.teleporter.container.ContainerTeleporter;
import net.dyeo.teleporter.entities.TileEntityTeleporter;
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

	@Override
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

	@Override
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
