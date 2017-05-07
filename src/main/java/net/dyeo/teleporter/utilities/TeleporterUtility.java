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
	public static boolean transferToDimensionLocation(Entity sourceEntity, int dimensionDestination, double x, double y, double z, float yaw, float pitch)
	{
		if (sourceEntity != null)
		{
			if (sourceEntity.dimension == dimensionDestination)
			{
				return transferToLocation(sourceEntity, x, y, z, yaw, pitch);
			}
			if ((sourceEntity instanceof EntityPlayerMP))
			{
				System.out.println("EntityPlayerMp");
				return _transferPlayerToDimension((EntityPlayerMP)sourceEntity, dimensionDestination, x, y, z, yaw, pitch);
			}
			if ((sourceEntity instanceof EntityLivingBase))
			{
				System.out.println("EntityLivingBase");
				return _transferEntityToDimension(sourceEntity, dimensionDestination, x, y, z, yaw, pitch);
			}
		}
		System.out.println("Non Entity");
		return false;
	}

	public static boolean transferToLocation(Entity entityIn, double x, double y, double z, float yaw, float pitch)
	{
		try
		{
			entityIn.setPosition(x, y, z);
			entityIn.rotationYaw = yaw;
			entityIn.rotationPitch = pitch;
		}
		catch (Exception e)
		{
			Throwables.propagate(e);
			return false;
		}
		return true;
	}

	static boolean _transferPlayerToDimension(EntityPlayerMP playerIn, int destinationDimension, double x, double y, double z, float yaw, float pitch)
	{
		int sourceDimension = playerIn.worldObj.provider.dimensionId;

		WorldServer destinationWorldServer = MinecraftServer.getServer().worldServerForDimension(destinationDimension);
		if (destinationWorldServer != null)
		{
			playerIn.addExperienceLevel(0);

			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(playerIn, destinationDimension);
			if (sourceDimension == 1)
			{
				playerIn.setLocationAndAngles(x, y, z, yaw, pitch);
				destinationWorldServer.spawnEntityInWorld(playerIn);
				destinationWorldServer.updateEntityWithOptionalForce(playerIn, false);
			}
			transferToLocation(playerIn, x, y, z, yaw, pitch);

			return true;
		}
		System.out.println("Destination world server does not exist.");
		return false;
	}

	static boolean _transferEntityToDimension(Entity entityIn, int destinationDimension, double x, double y, double z, float yaw, float pitch)
	{
		int sourceDimension = entityIn.worldObj.provider.dimensionId;

		MinecraftServer minecraftServer = MinecraftServer.getServer();

		WorldServer sourceWorldServer = minecraftServer.worldServerForDimension(sourceDimension);
		WorldServer destinationWorldServer = minecraftServer.worldServerForDimension(destinationDimension);
		if (destinationWorldServer != null)
		{
			NBTTagCompound tagCompound = new NBTTagCompound();

			entityIn.writeToNBT(tagCompound);

			Class<? extends Entity> entityClass = entityIn.getClass();

			sourceWorldServer.removeEntity(entityIn);
			try
			{
				Entity destinationEntity = (Entity)entityClass.getConstructor(new Class[] { World.class }).newInstance(new Object[] { destinationWorldServer });

				transferToLocation(destinationEntity, x, y, z, yaw, pitch);

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
				Throwables.propagate(e);
				return false;
			}
			return true;
		}
		System.out.println("Destination world server does not exist.");
		return false;
	}
}
