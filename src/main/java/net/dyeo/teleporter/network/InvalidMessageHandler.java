package net.dyeo.teleporter.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class InvalidMessageHandler implements IMessageHandler<TeleporterMessage, IMessage>
{
    @Override
    public IMessage onMessage(TeleporterMessage message, MessageContext ctx)
    {
        return null;
    }
}
