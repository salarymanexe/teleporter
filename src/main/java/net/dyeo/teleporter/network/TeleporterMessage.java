package net.dyeo.teleporter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TeleporterMessage implements IMessage
{
    public BlockPos pos;
    public boolean isPowered;

    public TeleporterMessage()
    {
        this.pos = BlockPos.ORIGIN;
        this.isPowered = false;
    }

    public TeleporterMessage(BlockPos pos, boolean isPowered)
    {
        this.pos = pos;
        this.isPowered = isPowered;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.isPowered = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
        buf.writeBoolean(this.isPowered);
    }
}
