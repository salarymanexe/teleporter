package net.dyeo.teleporter.utilities;

import net.dyeo.teleporter.entities.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

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
	static boolean _transferPlayerToDimension(EntityPlayerMP playerIn, int destinationDimension, double x, double y, double z, float yaw, float pitch) 
	{
		int sourceDimension = playerIn.worldObj.provider.getDimensionId();
		
		// get player's world server
        WorldServer destinationWorldServer = MinecraftServer.getServer().worldServerForDimension(destinationDimension);
        
        if(destinationWorldServer != null)
        {
        	// force the player entity to update
        	playerIn.addExperienceLevel(0);
        
        	// teleport the player to the new dimension
        	MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(playerIn, destinationDimension);
        
        	// handle returning from end, such that it behaves like a regular dimensional teleport
        	if (sourceDimension == 1 ) 
        	{
        		playerIn.setPositionAndUpdate(x, y, z);
            	destinationWorldServer.spawnEntityInWorld(playerIn);
            	destinationWorldServer.updateEntityWithOptionalForce(playerIn, false);
        	}
        	
        	// get TeleporterEntity, or register one if it doesn't already exist
        	TeleporterEntity destinationPlayerProperties = TeleporterEntity.get(playerIn);
            
        	// set so the player doesn't teleport again when the dimension loads
        	destinationPlayerProperties.setOnTeleporter(true);
        	destinationPlayerProperties.setTeleported(true);
        	
        	// transfer the player to the location of the teleport, and preserve rotation
        	TeleporterUtility.transferToLocation(playerIn, x, y, z, playerIn.rotationYaw, playerIn.rotationPitch);
        
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
	
	// transfer entity to dimension. do not transfer player using this method! use _transferPlayerToDimension
	static boolean _transferEntityToDimension(Entity entityIn, int destinationDimension, double x, double y, double z, float yaw, float pitch)
	{		
		int sourceDimension = entityIn.worldObj.provider.getDimensionId();
		
		MinecraftServer minecraftServer = MinecraftServer.getServer();
		
        WorldServer sourceWorldServer = minecraftServer.worldServerForDimension(sourceDimension);
        WorldServer destinationWorldServer = minecraftServer.worldServerForDimension(destinationDimension);

        if(destinationWorldServer != null)
        {      
            Entity destinationEntity;
    		NBTTagCompound tagCompound = new NBTTagCompound();
    		
    		// if the entity is not successfully written to nbt (on a mount):
        	if(!entityIn.writeToNBTOptional(tagCompound))
        	{
        		// teleport unsuccessful
        		System.out.println("Entity could not be written to NBT.");
        		return false;
        	}
        
        	// remove the original entity from the source world
    		sourceWorldServer.removeEntity(entityIn);
    		entityIn.isDead = false;
    		
    		// transfer the original entity data to the new world
    		minecraftServer.getConfigurationManager().transferEntityToWorld(entityIn, sourceDimension, sourceWorldServer, destinationWorldServer);
    		
    		// spawn the new entity in the destination world
    		destinationEntity = EntityList.createEntityFromNBT(tagCompound, destinationWorldServer);

    		// if the entity was successfully created:
    		if(destinationEntity != null)
    		{
    			// copy the old entity data into the new entity
    			destinationEntity.copyDataFromOld(entityIn);
    			
            	// get TeleporterEntity, or register one if it doesn't already exist
            	TeleporterEntity destinationEntityProperties = TeleporterEntity.get(destinationEntity);
                
            	// set so the entity doesn't teleport again when the dimension loads
            	destinationEntityProperties.setOnTeleporter(true);
            	destinationEntityProperties.setTeleported(true);
    			
    			// set the new entity's position/rotation to the teleport position/rotation
    			TeleporterUtility.transferToLocation(destinationEntity, x, y, z, yaw, pitch);
    			
    			// spawn the entity in the world
    			destinationWorldServer.spawnEntityInWorld(destinationEntity);
    		}
    		else
    		{
            	// teleport unsuccessful
            	System.out.println("Entity could not be created.");
            	return false;
    		}
    		
    		// kill the original entity
    		entityIn.isDead = true;
        	
        	// reset the update ticks so the entity will be included in the next update loop properly
        	sourceWorldServer.resetUpdateEntityTick();
            destinationWorldServer.resetUpdateEntityTick();
        	
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
