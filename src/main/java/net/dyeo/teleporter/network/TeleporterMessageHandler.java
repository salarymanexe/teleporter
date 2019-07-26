package net.dyeo.teleporter.network;

import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeleporterMessageHandler implements IMessageHandler<TeleporterMessage, IMessage>
{
    @Override
    public IMessage onMessage(TeleporterMessage message, MessageContext ctx)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;

        if(world.isRemote)
        {
            minecraft.addScheduledTask(() -> {
               TileEntityTeleporter tileEntity = (TileEntityTeleporter)world.getTileEntity(message.pos);
               if(tileEntity != null)
               {
                   tileEntity.setPowered(message.isPowered);
               }
            });
        }

        return null;
    }
}
