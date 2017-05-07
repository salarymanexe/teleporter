package net.dyeo.teleporter.utilities;

import com.google.common.base.Throwables;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.event.TeleportEvent;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.network.TeleporterNetwork;
import net.dyeo.teleporter.network.TeleporterNode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TeleporterUtility
{

	public static TeleporterNode teleport(EntityLivingBase entityIn, BlockPos pos)
	{
		boolean teleportSuccess = false;

		TeleporterNetwork netWrapper = TeleporterNetwork.instance(entityIn.worldObj);
		TeleporterNode sourceNode = netWrapper.getNode(pos, entityIn.worldObj.provider.getDimension());
		TeleporterNode destinationNode = netWrapper.getNextNode(entityIn, sourceNode);

		ITeleportHandler handler = entityIn.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);

		if (destinationNode != null)
		{
			double x = destinationNode.pos.getX() + (BlockTeleporter.TELEPORTER_AABB.maxX * 0.5D); // double x = destination.pos.getX() + (bounds.xCoord * 0.5f);
			double y = destinationNode.pos.getY() + (BlockTeleporter.TELEPORTER_AABB.maxY); // double y = destination.pos.getY() + (float)bounds.yCoord;
			double z = destinationNode.pos.getZ() + (BlockTeleporter.TELEPORTER_AABB.maxZ * 0.5D); // double z = destination.pos.getZ() + (bounds.zCoord * 0.5f);
			float yaw = entityIn.rotationYaw;
			float pitch = entityIn.rotationPitch;

			System.out.println("teleport :: Setting teleportStatus to " + EnumTeleportStatus.IN_PROGRESS);
			handler.setTeleportStatus(EnumTeleportStatus.IN_PROGRESS);


			if (sourceNode.type == BlockTeleporter.EnumType.REGULAR || entityIn.dimension == destinationNode.dimension)
			{
				teleportSuccess = transferToLocation(entityIn, x, y, z, yaw, pitch);
			}
			else
			{
				// don't allow cross-dimensional teleportation if the entity is a mount and the destination is another dimension
				if (!(sourceNode.dimension != destinationNode.dimension && !entityIn.getPassengers().isEmpty()))
				{
					if (entityIn instanceof EntityPlayerMP)
					{
						teleportSuccess = transferPlayerToDimension((EntityPlayerMP)entityIn, destinationNode.dimension, x, y, z, yaw, pitch);
					}
					else if (entityIn instanceof EntityLivingBase)
					{
						teleportSuccess = transferEntityToDimension(entityIn, destinationNode.dimension, x, y, z, yaw, pitch);
					}
				}
			}
		}

		if (teleportSuccess)
		{
			entityIn.worldObj.playSound(null, sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), ModSounds.PORTAL_ENTER, SoundCategory.BLOCKS, 0.9f, 1.0f);
			entityIn.worldObj.playSound(null, destinationNode.pos.getX(), destinationNode.pos.getY(), destinationNode.pos.getZ(), ModSounds.PORTAL_EXIT, SoundCategory.BLOCKS, 0.9f, 1.0f);
		}
		else
		{
			entityIn.worldObj.playSound(null, sourceNode.pos.getX(), sourceNode.pos.getY(), sourceNode.pos.getZ(), ModSounds.PORTAL_ERROR, SoundCategory.BLOCKS, 0.9f, 1.0f);

			System.out.println("teleport :: Setting teleportStatus to " + EnumTeleportStatus.FAILED);
			handler.setTeleportStatus(EnumTeleportStatus.FAILED);
		}

		MinecraftForge.EVENT_BUS.post(new TeleportEvent.EntityTeleportedEvent(entityIn));
		return destinationNode;
	}


	/**
	 * transfers entity to a location in the same dimension
	 */
	private static boolean transferToLocation(EntityLivingBase srcEntity, double x, double y, double z, float yaw, float pitch)
	{
		try
		{

			srcEntity.setPositionAndUpdate(x, y, z);
			srcEntity.rotationYaw = yaw;
			srcEntity.rotationPitch = pitch;

		}
		catch (Exception e)
		{
			Throwables.propagate(e);
			return false;
		}
		return true;
	}


	/**
	 * transfer player to dimension, retaining all information and not dying
	 */
	private static boolean transferPlayerToDimension(EntityPlayerMP srcPlayer, int dstDimension, double x, double y, double z, float yaw, float pitch)
	{
		WorldServer srcWorldServer = srcPlayer.mcServer.worldServerForDimension(srcPlayer.dimension);
		WorldServer dstWorldServer = srcPlayer.mcServer.worldServerForDimension(dstDimension);

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
				srcPlayer.worldObj.getDifficulty(),
				srcPlayer.worldObj.getWorldInfo().getTerrainType(),
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

		dstWorldServer.spawnEntityInWorld(srcPlayer); // spawn the player in the new world
		dstWorldServer.updateEntityWithOptionalForce(srcPlayer, false); // update the entity (do not force)
		srcPlayer.setWorld(dstWorldServer); // set the player's world to the new world
		serverConfigurationManager.preparePlayer(srcPlayer, srcWorldServer);
		srcPlayer.connection.setPlayerLocation(x, y, z, yaw, pitch); // set player's location (net server handler)
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
	private static boolean transferEntityToDimension(EntityLivingBase srcEntity, int dstDimension, double x, double y, double z, float yaw, float pitch)
	{

		ITeleportHandler handler = srcEntity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);

		int srcDimension = srcEntity.worldObj.provider.getDimension();

		MinecraftServer minecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();

		WorldServer srcWorldServer = minecraftServer.worldServerForDimension(srcDimension);
		WorldServer dstWorldServer = minecraftServer.worldServerForDimension(dstDimension);

		if (dstWorldServer != null)
		{
			Class<? extends Entity> entityClass = srcEntity.getClass();

			srcWorldServer.removeEntity(srcEntity);

			try
			{
				EntityLivingBase dstEntity = (EntityLivingBase)(entityClass.getConstructor(World.class).newInstance((World) dstWorldServer));

				dstEntity.setPositionAndRotation(x, y, z, yaw, pitch);

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
