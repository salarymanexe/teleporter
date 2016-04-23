package net.dyeo.teleporter.utilities;

import com.google.common.base.Throwables;

import net.dyeo.teleporter.entities.TeleporterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
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
        	if (sourceDimension == 1) 
        	{
        		playerIn.setLocationAndAngles(x, y, z, yaw, pitch);
            	destinationWorldServer.spawnEntityInWorld(playerIn);
            	destinationWorldServer.updateEntityWithOptionalForce(playerIn, false);
        	}
        	
        	// transfer the player to the location of the teleport, and preserve rotation
        	TeleporterUtility.transferToLocation(playerIn, x, y, z, yaw, pitch);
        
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
    	        
    	        TeleporterEntity entityProperties = TeleporterEntity.get(destinationEntity);
    	        entityProperties.setOnTeleporter(true);
    	        entityProperties.setTeleported(true);
            	
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
