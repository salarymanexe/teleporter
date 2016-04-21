package net.dyeo.teleporter.utilities;

import net.dyeo.teleporter.entities.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;

public class TeleporterUtility 
{

	// transfers the entity to the selected location in the selected dimension
	public static boolean transferToDimensionLocation(Entity sourceEntity, int dimensionDestination, double x, double y, double z, float yaw, float pitch)
	{				
		if(sourceEntity != null)
		{	
			// if the dimensions are the same, we can fall back to the transfer to location teleport
			if(sourceEntity.dimension == dimensionDestination)
			{
				return TeleporterUtility.transferToLocation(sourceEntity, x, y, z, sourceEntity.rotationYaw, sourceEntity.rotationPitch);
			}
			else
			{
				if(sourceEntity instanceof EntityPlayerMP)
				{
					System.out.println("EntityPlayerMp");
					return _transferPlayerToDimension((EntityPlayerMP)sourceEntity, dimensionDestination, x, y, z, yaw, pitch);
				}
				else if(sourceEntity instanceof EntityLivingBase)
				{
					System.out.println("EntityLivingBase");
					return TeleporterUtility._transferEntityToDimension(sourceEntity, dimensionDestination, x, y, z, sourceEntity.rotationYaw, sourceEntity.rotationPitch);
				}
			}
		}
		System.out.println("Non Entity");
		return false;
	}
	
	// transfers entity to a location in the same dimension
	public static boolean transferToLocation(Entity sourceEntity, double x, double y, double z, float yaw, float pitch)
	{
		try
		{
			sourceEntity.rotationYaw = yaw;
			sourceEntity.rotationPitch = pitch;
			sourceEntity.setPositionAndUpdate(x, y, z);
		}
		catch(Exception e)
		{
			System.out.println("EXCEPTION: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	// transfer player to dimension, retaining all information and not dying
	static boolean _transferPlayerToDimension(EntityPlayerMP sourcePlayer, int dimensionDestination, double x, double y, double z, float yaw, float pitch) 
	{

		// get the server configuration manager for the player
		ServerConfigurationManager serverConfigurationManager = sourcePlayer.mcServer.getConfigurationManager();
		
		// get the world server for the player's current dimension
		WorldServer sourceWorldServer = sourcePlayer.mcServer.worldServerForDimension(sourcePlayer.dimension);
		// get the world server for the destination dimension
		WorldServer destinationWorldServer = sourcePlayer.mcServer.worldServerForDimension(dimensionDestination);

		// fire player change dimension event and check that action is valid before continuing
		PlayerChangedDimensionEvent playerChangedDimensionEvent = new PlayerChangedDimensionEvent(sourcePlayer, sourcePlayer.dimension, dimensionDestination);
		if (FMLCommonHandler.instance().bus().post(playerChangedDimensionEvent) == true)
		{
			return false;
		}

		// (hard) set the player's dimension to the destination dimension
		sourcePlayer.dimension = dimensionDestination;
		
		// send a player respawn packet to the destination dimension so the player respawns there
		sourcePlayer.playerNetServerHandler.sendPacket(
				new S07PacketRespawn(
						sourcePlayer.dimension,
						sourcePlayer.worldObj.getDifficulty(), 
						sourcePlayer.worldObj.getWorldInfo().getTerrainType(),
						sourcePlayer.theItemInWorldManager.getGameType()
						)
				);

		// remove the original player entity
		sourceWorldServer.removeEntity(sourcePlayer);
		// make sure the player isn't dead (removeEntity sets player as dead)
		sourcePlayer.isDead = false;

		sourcePlayer.mountEntity((Entity) null);
		if (sourcePlayer.riddenByEntity != null) 
		{
			sourcePlayer.riddenByEntity.mountEntity((Entity) null);
		}

		// spawn the player in the new world
		destinationWorldServer.spawnEntityInWorld(sourcePlayer);
		// apply no new forces to the entity
		destinationWorldServer.updateEntityWithOptionalForce(sourcePlayer, false);

		// set the player's world to the new world
		sourcePlayer.setWorld(destinationWorldServer);
		
		// remove the player from the original world
		serverConfigurationManager.func_72375_a(sourcePlayer, sourceWorldServer);

		// set player's location (net server handler)
		sourcePlayer.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
		
		// set item in world manager's world to the same as the player
		sourcePlayer.theItemInWorldManager.setWorld(destinationWorldServer);
		
		// update time and weather for the player so that it's the same as the world
		sourcePlayer.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(sourcePlayer, destinationWorldServer);
		sourcePlayer.mcServer.getConfigurationManager().syncPlayerInventory(sourcePlayer);
		
		// add no experience (syncs experience)
		sourcePlayer.addExperience(0);
		// update player's health
		sourcePlayer.setPlayerHealthUpdated();

		// fire the dimension changed event so that minecraft swithces dimensions properly
		FMLCommonHandler.instance().firePlayerChangedDimensionEvent( sourcePlayer, sourceWorldServer.provider.getDimensionId(), destinationWorldServer.provider.getDimensionId());

        TeleporterUtility.transferToLocation(sourcePlayer, x, y, z, sourcePlayer.rotationYaw, sourcePlayer.rotationPitch);
		
		return true;
	}
	
	// transfer entity to dimension. do not transfer player using this method! use _transferPlayerToDimension
	static boolean _transferEntityToDimension(Entity sourceEntity, int destinationDimension, double x, double y, double z, float yaw, float pitch)
	{		
		WorldServer sourceWorldServer = MinecraftServer.getServer().worldServerForDimension(sourceEntity.dimension);
		WorldServer destinationWorldServer = MinecraftServer.getServer().worldServerForDimension(destinationDimension);
		
		if(sourceWorldServer == null || destinationWorldServer == null)
		{
        	System.out.println("sourceWorldServer == null || destinationWorldServer == null");
			return false;
		}
				
		//
		NBTTagCompound tagCompound = new NBTTagCompound();
		
		// write source entity to the nbt tag
		sourceEntity.writeToNBTOptional(tagCompound);
                       
        // create entity from saved nbt tag
        Entity destinationEntity = EntityList.createEntityFromNBT(tagCompound, destinationWorldServer);
        
        if(sourceEntity != null && destinationEntity != null)
        {
    		// remove entity from source world
            sourceWorldServer.removeEntity(sourceEntity);
            
        	// set entity location and orientation
        	destinationEntity.setLocationAndAngles(x, y, z, yaw, pitch);     

        	// spawn entity in destination world
        	destinationWorldServer.spawnEntityInWorld(destinationEntity);
        
        	// force update the entity and set its world
        	destinationWorldServer.updateEntityWithOptionalForce(destinationEntity, false);
        	destinationEntity.setWorld(destinationWorldServer);
                
        	// register destination entity with IExtendedEntityProperties - TeleporterEntity
        	TeleporterEntity destinationEntityProperties = TeleporterEntity.get(destinationEntity);
            
        	// so the entity doesn't teleport again when the dimension loads
        	destinationEntityProperties.setOnTeleporter(true);
        	destinationEntityProperties.setTeleported(true);
        		
        	// finally, apply the teleportation transfer to ensure the enemy is in the correct location
        	TeleporterUtility.transferToLocation(destinationEntity, x, y, z, yaw, pitch);

    		return true;
        }
        else
        {
        	System.out.println("sourceEntity == null || destinationEntity == null");
        	return false;
        }
	}
}
