package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.event.TeleportEvent;
import net.dyeo.teleporter.init.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TeleporterUtility
{

	public static TeleporterNode teleport(EntityLivingBase entity, BlockPos pos)
	{
		boolean teleportSuccess = false;

		TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.world);
		TeleporterNode sourceNode = netWrapper.getNode(pos, entity.world.provider.getDimension());
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
				if (!(sourceNode.dimension != destinationNode.dimension && !entity.getPassengers().isEmpty()))
				{
					if (entity instanceof EntityPlayerMP)
					{
						teleportSuccess = transferPlayerToDimension((EntityPlayerMP)entity, x, y, z, yaw, pitch, destinationNode.dimension);
					}
					else if (entity instanceof EntityLivingBase)
					{
						teleportSuccess = false;
						//teleportSuccess = transferEntityToDimension(entity, x, y, z, yaw, pitch, destinationNode.dimension);
					}
				}
			}
		}

		if (teleportSuccess)
		{
			entity.world.playSound(null, sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), ModSounds.PORTAL_ENTER, SoundCategory.BLOCKS, 0.9f, 1.0f);
			entity.world.playSound(null, destinationNode.pos.getX(), destinationNode.pos.getY(), destinationNode.pos.getZ(), ModSounds.PORTAL_EXIT, SoundCategory.BLOCKS, 0.9f, 1.0f);
		}
		else
		{
			entity.world.playSound(null, sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), ModSounds.PORTAL_ERROR, SoundCategory.BLOCKS, 0.9f, 1.0f);
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
	private static boolean transferPlayerToDimension(EntityPlayerMP srcPlayer, double posX, double posY, double posZ, float yaw, float pitch, int dstDimension)
	{
		WorldServer srcWorldServer = DimensionManager.getWorld(srcPlayer.dimension);
		WorldServer dstWorldServer = DimensionManager.getWorld(dstDimension);

		// fire player change dimension event and check that action is valid before continuing
		if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(srcPlayer, dstDimension)) return false;

//		PlayerChangedDimensionEvent playerChangedDimensionEvent = new PlayerChangedDimensionEvent(srcPlayer, srcPlayer.dimension, dstDimension);
//		if (MinecraftForge.EVENT_BUS.post(playerChangedDimensionEvent)) return false;

		// (hard) set the player's dimension to the destination dimension
		srcPlayer.dimension = dstDimension;

		// send a player respawn packet to the destination dimension so the player respawns there
		srcPlayer.connection.sendPacket(
			new SPacketRespawn(
				srcPlayer.dimension,
				srcPlayer.world.getDifficulty(),
				srcPlayer.world.getWorldInfo().getTerrainType(),
				srcPlayer.interactionManager.getGameType()
			)
		);


		srcWorldServer.removeEntity(srcPlayer); // remove the original player entity
		srcPlayer.isDead = false; // make sure the player isn't dead (removeEntity sets player as dead)

		// TODO
//		srcPlayer.mountEntity((Entity) null);
//		if (srcPlayer.riddenByEntity != null)
//		{
//			srcPlayer.riddenByEntity.mountEntity((Entity) null);
//		}

		PlayerList serverConfigurationManager = srcPlayer.mcServer.getPlayerList();

		dstWorldServer.spawnEntity(srcPlayer); // spawn the player in the new world
		dstWorldServer.updateEntityWithOptionalForce(srcPlayer, false); // update the entity (do not force)
		srcPlayer.setWorld(dstWorldServer); // set the player's world to the new world
		serverConfigurationManager.preparePlayer(srcPlayer, srcWorldServer);
		srcPlayer.connection.setPlayerLocation(posX, posY, posZ, yaw, pitch); // set player's location (net server handler)
		srcPlayer.interactionManager.setWorld(dstWorldServer); // set item in world manager's world to the same as the player
		serverConfigurationManager.updateTimeAndWeatherForPlayer(srcPlayer, dstWorldServer); // update time and weather for the player so that it's the same as the world
		serverConfigurationManager.syncPlayerInventory(srcPlayer); // sync the player's inventory
		srcPlayer.addExperience(0); // add no experience (syncs experience)
		srcPlayer.setPlayerHealthUpdated(); // update player's health


		// fire the dimension changed event so that minecraft swithces dimensions properly
		FMLCommonHandler.instance().firePlayerChangedDimensionEvent(
			srcPlayer,
			srcWorldServer.provider.getDimension(),
			dstWorldServer.provider.getDimension()
		);

		return true;
	}


	/**
	 * transfer entity to dimension. do not transfer player using this method! use transferPlayerToDimension
	 */
	private static boolean transferEntityToDimension(EntityLivingBase srcEntity, double posX, double posY, double posZ, float yaw, float pitch, int dstDimension)
	{
		int srcDimension = srcEntity.world.provider.getDimension();

		WorldServer srcWorldServer = DimensionManager.getWorld(srcDimension);
		WorldServer dstWorldServer = DimensionManager.getWorld(dstDimension);

		if (dstWorldServer != null)
		{
			Class<? extends Entity> entityClass = srcEntity.getClass();
			srcWorldServer.removeEntity(srcEntity);
			try
			{
				EntityLivingBase dstEntity = (EntityLivingBase)(entityClass.getConstructor(World.class).newInstance((World) dstWorldServer));

				dstEntity.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
				dstEntity.forceSpawn = true;
				dstWorldServer.spawnEntity(dstEntity);
				dstEntity.forceSpawn = false;
				dstWorldServer.updateEntityWithOptionalForce(dstEntity, false);

				return true;
			}
			catch (Exception ex){ TeleporterMod.LOGGER.catching(ex); }
		}

		return false;
	}
}
