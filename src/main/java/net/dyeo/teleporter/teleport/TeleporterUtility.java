package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty.EnumTeleportStatus;
import net.dyeo.teleporter.event.TeleportEvent;
import net.dyeo.teleporter.init.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

public class TeleporterUtility
{

	public static TeleporterNode teleport(EntityLivingBase entity, int posX, int posY, int posZ)
	{
		boolean teleportSuccess = false;

		TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.worldObj);
		TeleporterNode sourceNode = netWrapper.getNode(posX, posY, posZ, entity.worldObj.provider.dimensionId);
		TeleporterNode destinationNode = netWrapper.getNextNode(entity, sourceNode);

		TeleportEntityProperty handler = TeleportEntityProperty.get(entity);

		if (destinationNode != null)
		{
			handler.setTeleportStatus(EnumTeleportStatus.IN_PROGRESS);

			double x = destinationNode.x + (BlockTeleporter.TELEPORTER_AABB.maxX * 0.5D);
			double y = destinationNode.y + (BlockTeleporter.TELEPORTER_AABB.maxY);
			double z = destinationNode.z + (BlockTeleporter.TELEPORTER_AABB.maxZ * 0.5D);
			float yaw = entity.rotationYaw;
			float pitch = entity.rotationPitch;

			if (sourceNode.type == BlockTeleporter.EnumType.REGULAR || entity.dimension == destinationNode.dimension)
			{
				teleportSuccess = transferToLocation(entity, x, y, z, yaw, pitch);
			}
			else
			{
				// don't allow cross-dimensional teleportation if the entity is a mount and the destination is another dimension
				if (!(sourceNode.dimension != destinationNode.dimension && entity.riddenByEntity != null))
				{
					if (entity instanceof EntityPlayerMP)
					{
						teleportSuccess = transferPlayerToDimension((EntityPlayerMP)entity, x, y, z, yaw, pitch, destinationNode.dimension);
					}
					else if (entity instanceof EntityLivingBase)
					{
						teleportSuccess = transferEntityToDimension(entity, x, y, z, yaw, pitch, destinationNode.dimension);
					}
				}
			}
		}

		if (teleportSuccess)
		{
			entity.worldObj.playSoundEffect(sourceNode.x, sourceNode.y, sourceNode.z, ModSounds.PORTAL_ENTER, 0.9F, 1.0F);
			entity.worldObj.playSoundEffect(destinationNode.x, destinationNode.y, destinationNode.z, ModSounds.PORTAL_EXIT, 0.9F, 1.0F);
		}
		else
		{
			entity.worldObj.playSoundEffect(sourceNode.x, sourceNode.y, sourceNode.z, ModSounds.PORTAL_ERROR, 0.9F, 1.0F);
			handler.setTeleportStatus(EnumTeleportStatus.FAILED);
		}

		MinecraftForge.EVENT_BUS.post(new TeleportEvent.EntityTeleportedEvent(entity));
		return destinationNode;
	}


	/**
	 * transfers entity to a location in the same dimension
	 */
	private static boolean transferToLocation(EntityLivingBase entity, double posX, double posY, double posZ, float yaw, float pitch)
	{
		entity.setPositionAndUpdate(posX, posY, posZ);
		entity.rotationYaw = yaw;
		entity.rotationPitch = pitch;
		return true;
	}


	/**
	 * transfer player to dimension, retaining all information and not dying
	 */
	private static boolean transferPlayerToDimension(EntityPlayerMP entity, double posX, double posY, double posZ, float yaw, float pitch, int dstDimension)
	{
		int sourceDimension = entity.worldObj.provider.dimensionId;

		WorldServer destinationWorldServer = MinecraftServer.getServer().worldServerForDimension(dstDimension);
		if (destinationWorldServer != null)
		{
			entity.addExperienceLevel(0);

			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(entity, dstDimension);
			if (sourceDimension == 1)
			{
				entity.setLocationAndAngles(posX, posY, posZ, yaw, pitch);
				destinationWorldServer.spawnEntityInWorld(entity);
				destinationWorldServer.updateEntityWithOptionalForce(entity, false);
			}
			transferToLocation(entity, posX, posY, posZ, yaw, pitch);

			return true;
		}
		System.out.println("Destination world server does not exist.");
		return false;
	}


	/**
	 * transfer entity to dimension. do not transfer player using this method! use transferPlayerToDimension
	 */
	private static boolean transferEntityToDimension(EntityLivingBase srcEntity, double posX, double posY, double posZ, float yaw, float pitch, int dstDimension)
	{
		int sourceDimension = srcEntity.worldObj.provider.dimensionId;

		MinecraftServer minecraftServer = MinecraftServer.getServer();
		WorldServer srcWorldServer = minecraftServer.worldServerForDimension(sourceDimension);
		WorldServer dstWorldServer = minecraftServer.worldServerForDimension(dstDimension);

		if (dstWorldServer != null)
		{
			Class<? extends Entity> entityClass = srcEntity.getClass();
			srcWorldServer.removeEntity(srcEntity);

			try
			{
				Entity dstEntity = entityClass.getConstructor(World.class).newInstance((World)dstWorldServer);

				dstEntity.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
				dstEntity.forceSpawn = true;
				dstWorldServer.spawnEntityInWorld(dstEntity);
				dstEntity.forceSpawn = false;
				dstWorldServer.updateEntityWithOptionalForce(dstEntity, false);

				return true;
			}
			catch (Exception ex){ TeleporterMod.LOGGER.catching(ex); }
		}

		return false;
	}

}
