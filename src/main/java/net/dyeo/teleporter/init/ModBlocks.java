package net.dyeo.teleporter.init;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.block.BlockTeleporter.EnumType;
import net.dyeo.teleporter.item.ItemBlockTeleporter;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks
{
	public static final Block TELEPORTER = new BlockTeleporter().setRegistryName("teleporter").setUnlocalizedName("teleporter");

	@EventBusSubscriber(modid = TeleporterMod.MODID)
	public static class EventHandlers
	{

		@SubscribeEvent
		public static void registerBlocks(final RegistryEvent.Register<Block> event)
		{
			event.getRegistry().register(TELEPORTER);
		}

		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event)
		{
			event.getRegistry().register(new ItemBlockTeleporter(TELEPORTER));
		}

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onModelRegistry(final ModelRegistryEvent event)
		{
			Item item = Item.getItemFromBlock(TELEPORTER);
			for ( EnumType type : EnumType.values() )
			{
				ModelLoader.setCustomModelResourceLocation(item, type.getMetadata(), new ModelResourceLocation(TeleporterMod.MODID + ":" + type.getRegistryName(), "inventory"));
			}
		}
	}

}
