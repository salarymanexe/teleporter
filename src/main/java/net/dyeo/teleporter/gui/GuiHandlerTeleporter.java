package net.dyeo.teleporter.gui;

import net.dyeo.teleporter.container.ContainerTeleporter;
import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandlerTeleporter implements IGuiHandler
{
	private static final int GUIID_MBE_30 = 30;

	public static int getGuiID()
	{
		return GUIID_MBE_30;
	}

	// Gets the server side element for the given gui id- this should return a
	// container
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID != getGuiID())
		{
			System.err.println("Invalid ID: expected " + getGuiID() + ", received " + ID);
		}

		BlockPos xyz = new BlockPos(x, y, z);
		TileEntity tileEntity = world.getTileEntity(xyz);
		if (tileEntity instanceof TileEntityTeleporter)
		{
			TileEntityTeleporter tileEntityInventoryBasic = (TileEntityTeleporter) tileEntity;
			return new ContainerTeleporter(player.inventory, tileEntityInventoryBasic);
		}
		return null;
	}

	// Gets the client side element for the given gui id- this should return a
	// gui
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID != getGuiID())
		{
			System.err.println("Invalid ID: expected " + getGuiID() + ", received " + ID);
		}

		BlockPos xyz = new BlockPos(x, y, z);
		TileEntity tileEntity = world.getTileEntity(xyz);
		if (tileEntity instanceof TileEntityTeleporter)
		{
			TileEntityTeleporter tileEntityInventoryBasic = (TileEntityTeleporter) tileEntity;
			return new GuiTeleporter(player.inventory, tileEntityInventoryBasic);
		}
		return null;
	}
}
