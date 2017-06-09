package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.capabilities.CapabilityTeleportHandler;
import net.dyeo.teleporter.capabilities.EnumTeleportStatus;
import net.dyeo.teleporter.capabilities.ITeleportHandler;
import net.dyeo.teleporter.event.TeleportEvent;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Contains static utility methods used throughout the teleporter network and nodes, as well as the functionality for teleporting players and entities.
 *
 */
public class TeleporterUtility
{

	/**
	 * Teleports the entity to the next available node in the network.
	 * @param entity The entity to be teleported.
	 * @param pos The position of the entity, used to find the teleporter they're standing on.
	 * @return The TeleporterNode they teleported to, or null if the teleport failed.
	 */
	public static TeleporterNode teleport(EntityLivingBase entity, BlockPos pos)
	{
		boolean teleportSuccess = false;

		TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.world);
		TeleporterNode sourceNode = netWrapper.getNode(pos, entity.world.provider.getDimension());
		TeleporterNode destinationNode = netWrapper.getNextNode(entity, sourceNode);
		
		if (destinationNode != null)
		{
			AxisAlignedBB DESTINATION_AABB = destinationNode.type.isRecall() ? BlockTeleporter.RECALL_TELEPORTER_AABB : BlockTeleporter.TELEPORTER_AABB;
			
			double x = destinationNode.pos.getX() + (DESTINATION_AABB.maxX * 0.5D);
			double y = destinationNode.pos.getY() + (DESTINATION_AABB.maxY);
			double z = destinationNode.pos.getZ() + (DESTINATION_AABB.maxZ * 0.5D);
			float yaw = entity.rotationYaw;
			float pitch = entity.rotationPitch;

			if (!sourceNode.type.isEnder() || entity.dimension == destinationNode.dimension) // if (sourceNode.type == BlockTeleporter.EnumType.REGULAR || entity.dimension == destinationNode.dimension)
			{
				if(entity instanceof EntityPlayerMP)
				{
					teleportSuccess = setPlayerPosition((EntityPlayerMP)entity, x, y, z, yaw, pitch);
				}
				else
				{
					teleportSuccess = setEntityPosition(entity, x, y, z, yaw, pitch);
				}
			}
			else if (!(sourceNode.dimension != destinationNode.dimension && !entity.getPassengers().isEmpty()))
			{
				if (entity instanceof EntityPlayerMP)
				{
					teleportSuccess = transferPlayerToDimension((EntityPlayerMP)entity, x, y, z, yaw, pitch, destinationNode.dimension);
				}
				else
				{
					teleportSuccess = transferEntityToDimension(entity, x, y, z, yaw, pitch, destinationNode.dimension);
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
			
			ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
			handler.setTeleportStatus(EnumTeleportStatus.FAILED);
		}

		MinecraftForge.EVENT_BUS.post(new TeleportEvent.EntityTeleportedEvent(entity));
		return destinationNode;
	}

	/**
	 * Transfers a player to a different dimension and location, as if they were being teleported by a dimension portal
	 */
	private static boolean transferPlayerToDimension(EntityPlayerMP player, double posX, double posY, double posZ, float yaw, float pitch, int dimension)
	{
		MinecraftServer minecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer srcWorld = minecraftServer.worldServerForDimension(player.dimension);
		WorldServer dstWorld = minecraftServer.worldServerForDimension(dimension);

		if(dstWorld != null)
		{
		
			// fire player change dimension event and check that action is valid before continuing
			if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(player, dimension)) return false;
		
			// (hard) set the player's dimension to the destination dimension
			player.dimension = dimension;

			// send a player respawn packet to the destination dimension so the player respawns there
			player.connection.sendPacket(
				new SPacketRespawn(
					player.dimension,
					player.world.getDifficulty(),
					player.world.getWorldInfo().getTerrainType(),
					player.interactionManager.getGameType()
				)
			);

			srcWorld.removeEntity(player); // remove the original player entity
			player.isDead = false; // make sure the player isn't dead (removeEntity sets player as dead)
		
			PlayerList playerList = player.mcServer.getPlayerList();

			// set player's location (net server handler)
			setPlayerPosition(player, posX, posY, posZ, yaw, pitch);		
			// spawn the player in the new world
			dstWorld.spawnEntity(player); 
			// update the entity (do not force)
			dstWorld.updateEntityWithOptionalForce(player, false);
			// set the player's world to the new world
			player.setWorld(dstWorld); 
			// add the player into the appropriate player list
			playerList.preparePlayer(player, srcWorld);
			// set item in world manager's world to the same as the player
			player.interactionManager.setWorld(dstWorld); 
			// update time and weather for the player so that it's the same as the world
			playerList.updateTimeAndWeatherForPlayer(player, dstWorld); 
			// sync the player's inventory
			playerList.syncPlayerInventory(player); 
			// add no experience (syncs experience)
			player.addExperience(0); 
			// update player's health
			player.setPlayerHealthUpdated();


			// fire the dimension changed event so that minecraft swithces dimensions properly
			FMLCommonHandler.instance().firePlayerChangedDimensionEvent(
				player,
				srcWorld.provider.getDimension(),
				dstWorld.provider.getDimension()
			);
		
			return true;
		
		}
		else
		{
			return false;
		}
	}


	/**
	 * Transfers an entity to a different dimension and location, as if it was being teleported by a dimension portal
	 */
	private static boolean transferEntityToDimension(Entity entity, double x, double y, double z, float yaw, float pitch, int dimension)
	{
		MinecraftServer minecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer srcWorld = minecraftServer.worldServerForDimension(entity.dimension);
		WorldServer dstWorld = minecraftServer.worldServerForDimension(dimension);

		if (dstWorld != null)
		{
			try
			{
				NBTTagCompound nbttagcompound = entity.writeToNBT(new NBTTagCompound());
		    	nbttagcompound.removeTag("Dimension");
				srcWorld.removeEntity(entity);
				
				Entity newEntity = (Entity)(entity.getClass().getConstructor(World.class).newInstance((World) dstWorld));
				
				newEntity.readFromNBT(nbttagcompound);
		    	
				setEntityPosition(newEntity, x, y, z, yaw, pitch);
				
				dstWorld.spawnEntity(newEntity);
				
				dstWorld.updateEntityWithOptionalForce(newEntity, false);
				
				return true;
			}
			catch(Exception e)
			{ 
				TeleporterMod.LOGGER.catching(e); 
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Sets the player's position in a minecraft server-friendly way
	 */
	public static boolean setPlayerPosition(EntityPlayerMP player, double x, double y, double z, float yaw, float pitch)
	{
		ITeleportHandler handler = player.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
		handler.setTeleportStatus(EnumTeleportStatus.IN_PROGRESS);
		
		player.connection.setPlayerLocation(x, y, z, yaw, pitch);
		player.setRotationYawHead(yaw);
		return true;
	}
	
	/**
	 * Sets the entity's position in a minecraft server-friendly way
	 */
	public static boolean setEntityPosition(Entity entity, double x, double y, double z, float yaw, float pitch)
	{
		ITeleportHandler handler = entity.getCapability(CapabilityTeleportHandler.TELEPORT_CAPABILITY, null);
		handler.setTeleportStatus(EnumTeleportStatus.IN_PROGRESS);
		
		entity.setPositionAndRotation(x, y, z, yaw, pitch);//;(x, y, z, yaw, pitch);
		entity.setRotationYawHead(yaw);
		return true;
	}
	
	/**
	 * Retrieves the WorldServer for a given node.
	 * @param node
	 * @return
	 */
	public static WorldServer getNodeWorld(TeleporterNode node)
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.dimension);
	}

	/**
	 * Generates a unique item key pertaining to an item stack. Takes into account all unique values except for stack size. The return of this function is guaranteed to produce the same key for two identical items, and takes into account NBT tags, damage, and the unlocalized name. Mods which implement two different blocks/items with the same unlocalized name will be treated as the same.
	 * @param stack The item stack to generate a key from
	 * @return The unique key
	 */
	public static String getItemKey(ItemStack stack)
	{
		if (stack != null)
		{
			String key = stack.getUnlocalizedName();
	
			if (stack.stackSize != 0)
			{
				if (stack.getItemDamage() > 0)
				{
					key += ":" + stack.getItemDamage();
				}
	
				if (stack.hasTagCompound())
				{
					if (stack.getItem() == Items.WRITTEN_BOOK)
					{
						key += ":" + stack.getTagCompound().getString("author");
						key += ":" + stack.getTagCompound().getString("title");
					}
					else
					{
						key += ":" + stack.getTagCompound().toString();
					}
				}
			}
			return key;
		}
		return Blocks.AIR.getUnlocalizedName();
	}

	/**
	 * Determines whether the teleporter block is being obstructed for purposes of teleporting.
	 * @param world The world the node is contained in
	 * @param node
	 * @return True if the teleporter is obstructed, false otherwise
	 */
	public static boolean isObstructed(TeleporterNode node)
	{
		WorldServer world = TeleporterUtility.getNodeWorld(node);
		BlockPos blockPos1 = new BlockPos(node.pos.getX(), node.pos.getY() + 1, node.pos.getZ());
		BlockPos blockPos2 = new BlockPos(node.pos.getX(), node.pos.getY() + 2, node.pos.getZ());
		Block block1 = world.getBlockState(blockPos1).getBlock();
		Block block2 = world.getBlockState(blockPos2).getBlock();
	
		if (block1.isPassable(world, blockPos1) && block2.isPassable(world, blockPos2))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Retrieves the TileEntityTeleporter for a given teleporter node.
	 * @param node The teleporter node
	 * @return The tile entity, or null if no tile entity was found (or the tile entity is not a TileEntityTeleporter)
	 */
	public static TileEntityTeleporter getTileEntity(TeleporterNode node)
	{
		try
		{
			WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.dimension);
			return (TileEntityTeleporter)world.getTileEntity(node.pos);
		}
		catch(Exception ex)
		{
			TeleporterMod.LOGGER.catching(ex);
			return null;
		}
	}

	/**
	 * Gets a chat message for the player, given a string id.
	 * @param messageName The unlocalized message name
	 * @return The message to be sent to the player
	 */
	public static TextComponentTranslation getMessage(String messageName, Object source)
	{
		return new TextComponentTranslation("message." + TeleporterMod.MODID + '_' + source.getClass().getSimpleName() + '.' + messageName);
	}
}
