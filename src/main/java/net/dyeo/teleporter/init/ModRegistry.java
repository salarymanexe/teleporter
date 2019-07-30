package net.dyeo.teleporter.init;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.blockstate.IMetaType;
import net.dyeo.teleporter.item.ItemBlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Console;

import java.util.HashMap;
import java.util.Map;

public class ModRegistry
{
    private static Map<String, Block> blocks;
    private static Map<Item, IMetaType[]> items;
    private static Map<Class<? extends TileEntity>, Block> tileEntities;
    private static Map<Class<? extends TileEntity>, TileEntitySpecialRenderer> renderers;

    private static void initializeBlocks()
    {
        blocks = new HashMap<>();
        blocks.put("teleporter", new BlockTeleporter());
    }

    private static void initializeItems()
    {
        items = new HashMap<>();

        items.put(new ItemBlockTeleporter((BlockTeleporter)blocks.get("teleporter")),
                new BlockTeleporter.EnumType[]
                {
                        BlockTeleporter.EnumType.REGULAR,
                        BlockTeleporter.EnumType.ENDER,
                });
    }

    private static void initializeTileEntities()
    {
        tileEntities = new HashMap<>();
        tileEntities.put(TileEntityTeleporter.class, blocks.get("teleporter"));
    }

    private static void initializeRenderers()
    {
        renderers = new HashMap<>();
    }

    @Mod.EventBusSubscriber(modid = TeleporterMod.MODID)
    public static class EventHandlers
    {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            initializeBlocks();
            for (Map.Entry<String, Block> pair : blocks.entrySet())
            {
                pair.getValue().setUnlocalizedName(pair.getKey()).setRegistryName(pair.getKey());
                Console.println(pair.getValue().getRegistryName().toString());
                event.getRegistry().register(pair.getValue());
            }

            initializeTileEntities();
            for (Map.Entry<Class<? extends TileEntity>, Block> pair : tileEntities.entrySet())
            {
                GameRegistry.registerTileEntity(pair.getKey(), pair.getValue().getRegistryName());
            }
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            initializeItems();
            for (Item item : items.keySet())
            {
                Console.println(item.getRegistryName().toString());
                event.getRegistry().register(item);
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onModelRegistry(final ModelRegistryEvent event)
        {
            for (Map.Entry<Item, IMetaType[]> pair : items.entrySet())
            {
                if(pair.getKey().getHasSubtypes())
                {
                    for(IMetaType type : pair.getValue())
                    {
                        ModelResourceLocation location = new ModelResourceLocation(TeleporterMod.MODID + ":" + type.getRegistryName(), "inventory");
                        ModelLoader.setCustomModelResourceLocation(pair.getKey(), type.getMetadata(), location);
                    }
                }
                else
                {
                    ModelResourceLocation location = new ModelResourceLocation(TeleporterMod.MODID + ":" + pair.getKey().getRegistryName(), "inventory");
                    ModelLoader.setCustomModelResourceLocation(pair.getKey(), 0, location);
                }
            }

            initializeRenderers();
            for (Map.Entry<Class<? extends TileEntity>, TileEntitySpecialRenderer> pair : renderers.entrySet())
            {
                ClientRegistry.bindTileEntitySpecialRenderer(pair.getKey(), pair.getValue());
            }
        }
    }
}
