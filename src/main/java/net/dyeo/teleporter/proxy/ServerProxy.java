package net.dyeo.teleporter.proxy;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.network.InvalidMessageHandler;
import net.dyeo.teleporter.network.TeleporterMessage;
import net.dyeo.teleporter.network.TeleporterMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ServerProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();
    }

    @Override
    public void init()
    {
        super.init();
        TeleporterMod.NETWORK_WRAPPER.registerMessage(InvalidMessageHandler.class, TeleporterMessage.class, 0, Side.CLIENT);
    }

    @Override
    public void postInit()
    {
        super.postInit();
    }
}
