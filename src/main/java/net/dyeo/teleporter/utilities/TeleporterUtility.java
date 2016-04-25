package net.dyeo.teleporter.utilities;

import com.google.common.base.Throwables;

import net.dyeo.teleporter.capabilities.CapabilityTeleporterEntity;
import net.dyeo.teleporter.capabilities.ITeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
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
				return TeleporterUtility.transferToLocation(sourceEntity, x, y, z, yaw, pitch);
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
					return TeleporterUtility._transferEntityToDimension(sourceEntity, dimensionDestination, x, y, z, yaw, pitch);
				}
			}
		}
		System.out.println("Non Entity");
		return false;
	}
	
	// transfers entity to a location in the same dimension
	public static boolean transferToLocation(Entity entityIn, double x, double y, double z, float yaw, float pitch)
	{
		try
		{
			entityIn.setPositionAndUpdate(x, y, z);
			entityIn.rotationYaw = yaw; 
			entityIn.rotationPitch = pitch;
		}
		catch(Exception e)
		{
            Throwables.propagate(e);
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
			if (MinecraftForge.EVENT_BUS.post(playerChangedDimensionEvent) == true)
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
			// update the entity (do not force)
			destinationWorldServer.updateEntityWithOptionalForce(sourcePlayer, false);

			// set the player's world to the new world
			sourcePlayer.setWorld(destinationWorldServer);
			
			//serverConfigurationManager.func_72375_a(sourcePlayer, sourceWorldServer);
			serverConfigurationManager.preparePlayer(sourcePlayer, sourceWorldServer);

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
	static boolean _transferEntityToDimension(Entity entityIn, int destinationDimension, double x, double y, double z, float yaw, float pitch)
	{		
		int sourceDimension = entityIn.worldObj.provider.getDimensionId();
		
		MinecraftServer minecraftServer = MinecraftServer.getServer();
		
        WorldServer sourceWorldServer = minecraftServer.worldServerForDimension(sourceDimension);
        WorldServer destinationWorldServer = minecraftServer.worldServerForDimension(destinationDimension);

        if(destinationWorldServer != null)
        {   
    		NBTTagCompound tagCompound = new NBTTagCompound();

    		entityIn.writeToNBT(tagCompound); 
    		        	
            Class<? extends Entity> entityClass = entityIn.getClass();

    		sourceWorldServer.removeEntity(entityIn);    		
            try 
            {
				Entity destinationEntity = entityClass.getConstructor(World.class).newInstance((World)destinationWorldServer);
				
            	TeleporterUtility.transferToLocation(destinationEntity, x, y, z, yaw, pitch);
            	
    	        destinationEntity.forceSpawn = true;
    			destinationWorldServer.spawnEntityInWorld(destinationEntity);
    	        destinationEntity.forceSpawn = false;
    	        
    	        ITeleporterEntity ite = destinationEntity.getCapability(CapabilityTeleporterEntity.INSTANCE, null);
    	        ite.setOnTeleporter(true);
    	        ite.setTeleported(true);
            	
    	        destinationWorldServer.updateEntityWithOptionalForce(destinationEntity, false);
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
