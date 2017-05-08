package net.dyeo.teleporter.command;

import java.util.ArrayList;
import java.util.List;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty.EnumTeleportStatus;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandTeleportReset extends CommandBase
{

	private final List<String> aliases;

	public CommandTeleportReset()
	{
		this.aliases = new ArrayList<String>();
		this.aliases.add("teleport_reset");
	}

	@Override
	public String getCommandName()
	{
		return "teleport_reset";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public List<String> getCommandAliases()
	{
		return this.aliases;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		EntityPlayerMP entity = getCommandSenderAsPlayer(sender);
		TeleportEntityProperty handler = TeleportEntityProperty.get(entity);
		if (handler != null)
		{
			handler.setOnTeleporter(false);
			handler.setTeleportStatus(EnumTeleportStatus.INACTIVE);
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return sender.canCommandSenderUseCommand(2, this.getCommandName());
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		return null;
	}

}
