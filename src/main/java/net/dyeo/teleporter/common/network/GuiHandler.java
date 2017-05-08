package net.dyeo.teleporter.common.network;

import cpw.mods.fml.common.network.IGuiHandler;
import net.dyeo.teleporter.client.gui.inventory.GuiTeleporter;
import net.dyeo.teleporter.inventory.ContainerTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler
{

	public static final int GUI_ID_TELEPORTER = 0;


	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID == GUI_ID_TELEPORTER)
		{
			TileEntity tileentity = world.getTileEntity(x, y, z);
			if (tileentity instanceof TileEntityTeleporter)
			{
				TileEntityTeleporter tileentityteleporter = (TileEntityTeleporter)tileentity;
				return new ContainerTeleporter(player.inventory, tileentityteleporter);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID == GUI_ID_TELEPORTER)
		{
			TileEntity tileentity = world.getTileEntity(x, y, z);
			if (tileentity instanceof TileEntityTeleporter)
			{
				TileEntityTeleporter tileentityteleporter = (TileEntityTeleporter)tileentity;
				return new GuiTeleporter(player.inventory, tileentityteleporter);
			}
		}
		return null;
	}

}
