package net.dyeo.teleporter.command;

import java.util.ArrayList;
import java.util.List;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;

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
	public void processCommand(ICommandSender sender, String[] args) throws CommandException
	{
		EntityPlayerMP entity = getCommandSenderAsPlayer(sender);
		if (entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
		{
			ITeleportHandler handler = ((ITeleportHandler)entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null));
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
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
	{
		return null;
	}

}
