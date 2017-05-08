package net.dyeo.teleporter.teleport;

import com.google.common.base.Throwables;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.event.TeleportEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;

public class TeleporterUtility
{

	public static TeleporterNode teleport(EntityLivingBase entity, BlockPos pos)
	{
		boolean teleportSuccess = false;

		TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.worldObj);
		TeleporterNode sourceNode = netWrapper.getNode(pos, entity.worldObj.provider.getDimensionId());
		TeleporterNode destinationNode = netWrapper.getNextNode(entity, sourceNode);

		ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);

		if (destinationNode != null)
		{
			handler.setTeleportStatus(EnumTeleportStatus.IN_PROGRESS);

			double x = destinationNode.pos.getX() + (BlockTeleporter.TELEPORTER_AABB.maxX * 0.5D);
			double y = destinationNode.pos.getY() + (BlockTeleporter.TELEPORTER_AABB.maxY);
			double z = destinationNode.pos.getZ() + (BlockTeleporter.TELEPORTER_AABB.maxZ * 0.5D);
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
			entity.worldObj.playSoundEffect(sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), TeleporterMod.MODID + ":portalEnter", 0.9f, 1.0f);
			entity.worldObj.playSoundEffect(destinationNode.pos.getX(), destinationNode.pos.getY(), destinationNode.pos.getZ(), TeleporterMod.MODID + ":portalExit", 0.9f, 1.0f);
		}
		else
		{
			entity.worldObj.playSoundEffect(sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), TeleporterMod.MODID + ":portalError", 0.9f, 1.0f);
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
	private static boolean transferPlayerToDimension(EntityPlayerMP entity, double posX, double posY, double posZ, float yaw, float pitch, int dimensionId)
	{
		WorldServer srcWorldServer = entity.mcServer.worldServerForDimension(entity.dimension);
		WorldServer dstWorldServer = entity.mcServer.worldServerForDimension(dimensionId);

		// fire player change dimension event and check that action is valid before continuing
		if (MinecraftForge.EVENT_BUS.post(new PlayerChangedDimensionEvent(entity, entity.dimension, dimensionId))) return false;

		// (hard) set the player's dimension to the destination dimension
		entity.dimension = dimensionId;

		// send a player respawn packet to the destination dimension so the player respawns there
		entity.playerNetServerHandler.sendPacket(
			new S07PacketRespawn(
				entity.dimension,
				entity.worldObj.getDifficulty(),
				entity.worldObj.getWorldInfo().getTerrainType(),
				entity.theItemInWorldManager.getGameType()
			)
		);

		srcWorldServer.removeEntity(entity); // remove the original player entity
		entity.isDead = false; // make sure the player isn't dead (removeEntity sets player as dead)

		entity.mountEntity((Entity) null);
		if (entity.riddenByEntity != null)
		{
			entity.riddenByEntity.mountEntity((Entity) null);
		}

		ServerConfigurationManager serverConfigurationManager = entity.mcServer.getConfigurationManager();

		dstWorldServer.spawnEntityInWorld(entity); // spawn the player in the new world
		dstWorldServer.updateEntityWithOptionalForce(entity, false); // update the entity (do not force)
		entity.setWorld(dstWorldServer); // set the player's world to the new world
		serverConfigurationManager.preparePlayer(entity, srcWorldServer);
		entity.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, yaw, pitch); // set player's location (net server handler)
		entity.theItemInWorldManager.setWorld(dstWorldServer); // set item in world manager's world to the same as the player
		serverConfigurationManager.updateTimeAndWeatherForPlayer(entity, dstWorldServer); // update time and weather for the player so that it's the same as the world
		serverConfigurationManager.syncPlayerInventory(entity); // sync the player's inventory
		entity.addExperience(0); // add no experience (syncs experience)
		entity.setPlayerHealthUpdated(); // update player's health


		// fire the dimension changed event so that minecraft swithces dimensions properly
		FMLCommonHandler.instance().firePlayerChangedDimensionEvent(
			entity,
			srcWorldServer.provider.getDimensionId(),
			dstWorldServer.provider.getDimensionId()
		);

		return true;
	}


	/**
	 * transfer entity to dimension. do not transfer player using this method! use transferPlayerToDimension
	 */
	private static boolean transferEntityToDimension(EntityLivingBase entity, double posX, double posY, double posZ, float yaw, float pitch, int dimensionId)
	{
		int srcDimension = entity.worldObj.provider.getDimensionId();

		MinecraftServer minecraftServer = MinecraftServer.getServer();
		WorldServer srcWorldServer = minecraftServer.worldServerForDimension(srcDimension);
		WorldServer dstWorldServer = minecraftServer.worldServerForDimension(dimensionId);

		if (dstWorldServer != null)
		{
			Class<? extends Entity> entityClass = entity.getClass();

			srcWorldServer.removeEntity(entity);

			try
			{
				Entity dstEntity = entityClass.getConstructor(World.class).newInstance((World) dstWorldServer);

				dstEntity.setPositionAndRotation(posX, posY, posZ, yaw, pitch);

				dstEntity.forceSpawn = true;
				dstWorldServer.spawnEntityInWorld(dstEntity);
				dstEntity.forceSpawn = false;

				dstWorldServer.updateEntityWithOptionalForce(dstEntity, false);
			}
			catch (Exception e)
			{
				// teleport unsuccessful
				Throwables.propagate(e);
				return false;
			}

			// teleport successful
			return true;
		}
		else
		{
			// teleport unsuccessful
			System.out.println("Destination world server does not exist.");
			return false;
		}
	}
}
