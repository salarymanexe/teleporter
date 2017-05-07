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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class ModCommand extends CommandBase
{

	private final List<String> aliases;

	public ModCommand()
	{
		aliases = new ArrayList<String>();
		aliases.add("teleport_capability_reset");
	}

	@Override
	public String getCommandName()
	{
		return "teleport_capability_reset";
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
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
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
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender.canCommandSenderUseCommand(2, getCommandName());
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return null;
	}

}
