package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.event.TeleportEvent;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.world.TeleporterTeleporter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

public class TeleporterUtility
{
	public static TextComponentTranslation getMessage(String messageName)
	{
		return new TextComponentTranslation("message." + TeleporterMod.MODID + '.' + messageName);
	}

	public static void teleport(EntityLivingBase entity, BlockPos pos)
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.world);
		TeleporterNode sourceNode = netWrapper.getNode(pos, entity.world.provider.getDimension());
		TeleporterNode destinationNode = netWrapper.getNextNode(entity, sourceNode);

		ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);

		if (handler != null && sourceNode != null && destinationNode != null)
		{
			handler.setTeleportStatus(EnumTeleportStatus.IN_PROGRESS);

			double x = destinationNode.pos.getX() + (BlockTeleporter.TELEPORTER_AABB.maxX * 0.5D);
			double y = destinationNode.pos.getY() + (BlockTeleporter.TELEPORTER_AABB.maxY);
			double z = destinationNode.pos.getZ() + (BlockTeleporter.TELEPORTER_AABB.maxZ * 0.5D);
			float yaw = entity.rotationYaw;
			float pitch = entity.rotationPitch;

			if (sourceNode.type == BlockTeleporter.EnumType.REGULAR || entity.dimension == destinationNode.dimension)
			{
				if (entity instanceof EntityPlayerMP)
				{
					transferPlayerToLocation((EntityPlayerMP)entity, x, y, z, yaw, pitch);
				}
				else
				{
					transferEntityToLocation(entity, x, y, z, yaw, pitch);
				}
			}
			else
			{
				if (entity instanceof EntityPlayerMP)
				{
					transferPlayerToDimension((EntityPlayerMP)entity, x, y, z, yaw, pitch, destinationNode.dimension);
				}
				else
				{
					transferEntityToDimension(entity, x, y, z, yaw, pitch, destinationNode.dimension);
				}
			}

			entity.world.playSound(null, sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), ModSounds.PORTAL_ENTER, SoundCategory.BLOCKS, 0.9f, 1.0f);
			entity.world.playSound(null, destinationNode.pos.getX(), destinationNode.pos.getY(), destinationNode.pos.getZ(), ModSounds.PORTAL_EXIT, SoundCategory.BLOCKS, 0.9f, 1.0f);
		}
		else
		{
			entity.world.playSound(null, sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), ModSounds.PORTAL_ERROR, SoundCategory.BLOCKS, 0.9f, 1.0f);
			handler.setTeleportStatus(EnumTeleportStatus.FAILED);
		}

		MinecraftForge.EVENT_BUS.post(new TeleportEvent.EntityTeleportedEvent(entity));
	}

	/**
	 * transfers player to a location in the same dimension
	 */
	private static void transferPlayerToLocation(EntityPlayerMP player, double x, double y, double z, float yaw, float pitch)
	{
		WorldServer world = player.world.getMinecraftServer().getWorld(player.dimension);
		world.getBlockState(new BlockPos(x,y,z));

		player.setSprinting(false);

		player.setPositionAndUpdate(x, y, z);
		player.rotationYaw = yaw;
		player.rotationPitch = pitch;
	}

	/**
	 * transfers entity to a location in the same dimension
	 */
	private static void transferEntityToLocation(EntityLivingBase entity, double x, double y, double z, float yaw, float pitch)
	{
		WorldServer world = entity.world.getMinecraftServer().getWorld(entity.dimension);
		world.getBlockState(new BlockPos(x,y,z));

		entity.setPositionAndUpdate(x, y, z);
		entity.rotationYaw = yaw;
		entity.rotationPitch = pitch;
	}

	/**
	 * transfers player to a location in another dimension
	 */
	private static void transferPlayerToDimension(EntityPlayerMP player, double x, double y, double z, float yaw, float pitch, int dstDimension)
	{
		MinecraftServer server = player.world.getMinecraftServer();
		WorldServer dstWorld = server.getWorld(dstDimension);

		player.setSprinting(false);

		dstWorld.getMinecraftServer().getPlayerList().transferPlayerToDimension(player, dstDimension, new TeleporterTeleporter(x,y,z,yaw,pitch));

		player.setPositionAndUpdate(x,y,z);
		player.rotationYaw = yaw;
		player.rotationPitch = pitch;
	}

	/**
	 * transfers entity to a location in another dimension
	 */
	private static void transferEntityToDimension(EntityLivingBase entity, double x, double y, double z, float yaw, float pitch, int dstDimension)
	{
		MinecraftServer server = entity.world.getMinecraftServer();

		entity.changeDimension(dstDimension, new TeleporterTeleporter(x,y,z,yaw,pitch));
		entity.setPositionAndUpdate(x,y,z);
	}
}
