package net.dyeo.teleporter.gui;

import java.util.HashMap;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHandlerRegistry implements IGuiHandler
{
	public void registerGuiHandler(IGuiHandler handler, int guiID)
	{
		this.registeredHandlers.put(Integer.valueOf(guiID), handler);
	}

	public static GuiHandlerRegistry getInstance()
	{
		return guiHandlerRegistry;
	}

	private HashMap<Integer, IGuiHandler> registeredHandlers = new HashMap();
	private static GuiHandlerRegistry guiHandlerRegistry = new GuiHandlerRegistry();

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		IGuiHandler handler = (IGuiHandler)this.registeredHandlers.get(Integer.valueOf(ID));
		if (handler != null)
		{
			return handler.getServerGuiElement(ID, player, world, x, y, z);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		IGuiHandler handler = (IGuiHandler)this.registeredHandlers.get(Integer.valueOf(ID));
		if (handler != null)
		{
			return handler.getClientGuiElement(ID, player, world, x, y, z);
		}
		return null;
	}
}
