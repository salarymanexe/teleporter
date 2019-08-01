package net.dyeo.teleporter.utility;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.event.TeleportEvent;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.world.TeleporterNetwork;
import net.dyeo.teleporter.world.TeleporterNode;
import net.dyeo.teleporter.world.TeleporterTeleporter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TeleporterUtility
{
	public static TextComponentTranslation getMessage(String messageName)
	{
		return new TextComponentTranslation("message." + TeleporterMod.MODID + '.' + messageName);
	}

	public static void tryTeleport(World world, BlockPos pos, Entity entity)
	{
		if (!world.isRemote)
		{
			if (entity instanceof EntityLivingBase && entity.hasCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null))
			{
				ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
				if (handler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
				{
					handler.setOnTeleporter(entity.getPosition().distanceSq(pos) <= 1);
					handler.setDimension(entity.dimension);

					if (handler.getOnTeleporter())
					{
						boolean isHostile = (entity instanceof EntityMob) || (entity instanceof EntityWolf && ((EntityWolf)entity).isAngry());
						boolean isPassive = (entity instanceof EntityAnimal);

						if ((!isHostile || ModConfiguration.teleportHostileMobs) && (!isPassive || ModConfiguration.teleportPassiveMobs))
						{
							teleport((EntityLivingBase)entity, pos);
						}
					}
				}

				if (handler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
				{
					double width = 0.25;
					double height = 0.25;

					double mx = world.rand.nextGaussian() * 0.2d;
					double my = world.rand.nextGaussian() * 0.2d;
					double mz = world.rand.nextGaussian() * 0.2d;

					world.spawnParticle(EnumParticleTypes.PORTAL,
							pos.getX() + 0.5 + world.rand.nextFloat() * width * 2.0F - width,
							pos.getY() + 1.5 + world.rand.nextFloat() * height,
							pos.getZ() + 0.5 + world.rand.nextFloat() * width * 2.0F - width, mx, my, mz
					);
				}
			}
		}
	}

	private static void teleport(EntityLivingBase entity, BlockPos pos)
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.world);
		TeleporterNode srcNode = netWrapper.getNode(pos, entity.world.provider.getDimension());
		TeleporterNode dstNode = netWrapper.getNextNode(entity, srcNode);

		ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);

		if (handler != null && srcNode != null && dstNode != null)
		{
			handler.setTeleportStatus(EnumTeleportStatus.IN_PROGRESS);

			MinecraftServer server = entity.world.getMinecraftServer();
			WorldServer dstWorld = server.getWorld(dstNode.dimension);

			IBlockState blockState = dstWorld.getBlockState(dstNode.pos);
			AxisAlignedBB boundingBox = BlockTeleporter.getBoundingBox(blockState);
			double x = dstNode.pos.getX() + (boundingBox.maxX * 0.5D);
			double y = dstNode.pos.getY() + (boundingBox.maxY);
			double z = dstNode.pos.getZ() + (boundingBox.maxZ * 0.5D);
			float yaw = blockState.getValue(BlockTeleporter.FACING).getYaw();
			float pitch = entity.rotationPitch;

			if (srcNode.type == BlockTeleporter.EnumType.REGULAR || entity.dimension == dstNode.dimension)
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
					transferPlayerToDimension((EntityPlayerMP)entity, x, y, z, yaw, pitch, dstNode.dimension);
				}
				else
				{
					transferEntityToDimension(entity, x, y, z, yaw, pitch, dstNode.dimension);
				}
			}

			entity.world.playSound(null, srcNode.pos.getX(), srcNode.pos.getY(), srcNode.pos.getZ(), ModSounds.PORTAL_ENTER, SoundCategory.BLOCKS, 0.9f, 1.0f);
			entity.world.playSound(null, dstNode.pos.getX(), dstNode.pos.getY(), dstNode.pos.getZ(), ModSounds.PORTAL_EXIT, SoundCategory.BLOCKS, 0.9f, 1.0f);
		}
		else
		{
			entity.world.playSound(null, srcNode.pos.getX(), srcNode.pos.getY(), srcNode.pos.getZ(), ModSounds.PORTAL_ERROR, SoundCategory.BLOCKS, 0.9f, 1.0f);
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

		player.setRotationYawHead(yaw);
		player.rotationYaw = yaw;
		player.rotationPitch = pitch;
		player.setPositionAndUpdate(x, y, z);
	}

	/**
	 * transfers entity to a location in the same dimension
	 */
	private static void transferEntityToLocation(EntityLivingBase entity, double x, double y, double z, float yaw, float pitch)
	{
		WorldServer world = entity.world.getMinecraftServer().getWorld(entity.dimension);
		world.getBlockState(new BlockPos(x,y,z));

		entity.setRotationYawHead(yaw);
		entity.rotationYaw = yaw;
		entity.rotationPitch = pitch;
		entity.setPositionAndUpdate(x, y, z);
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

		player.setRotationYawHead(yaw);
		player.rotationYaw = yaw;
		player.rotationPitch = pitch;
		player.setPositionAndUpdate(x,y,z);
	}

	/**
	 * transfers entity to a location in another dimension
	 */
	private static void transferEntityToDimension(EntityLivingBase entity, double x, double y, double z, float yaw, float pitch, int dstDimension)
	{
		MinecraftServer server = entity.world.getMinecraftServer();

		entity.changeDimension(dstDimension, new TeleporterTeleporter(x,y,z,yaw,pitch));

		entity.setRotationYawHead(yaw);
		entity.rotationYaw = yaw;
		entity.rotationPitch = pitch;
		entity.setPositionAndUpdate(x,y,z);
	}
}
