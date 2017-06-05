/**
 * 
 */
package net.dyeo.teleporter.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.dyeo.teleporter.teleport.TeleporterNetwork;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Dan
 *
 */
public class CommandTeleporters extends CommandBase
{
	
	private final List<String> aliases;

	public CommandTeleporters()
	{
		this.aliases = new ArrayList<String>();
		this.aliases.add("teleporters");
	}

	@Override
	public String getName()
	{
		return "teleporters";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/teleporters";
	}

	@Override
	public List<String> getAliases()
	{
		return this.aliases;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{		
		TeleporterNetwork network = TeleporterNetwork.get(sender.getEntityWorld());

		TextComponentString chat = new TextComponentString("--- There are currently " + network.getSubnets().size() + " teleporter networks ---");
		chat.getStyle().setColor(TextFormatting.DARK_GREEN);
		sender.sendMessage(chat);
		
		Iterator<String> it = network.getSubnets().iterator();
		while(it.hasNext())
		{
			String s = it.next();
			chat = new TextComponentString("(" + network.getSubnetSize(s) + ") " + s);
			sender.sendMessage(chat);
		}		
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return null;
	}

}
