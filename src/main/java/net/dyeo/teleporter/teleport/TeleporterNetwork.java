package net.dyeo.teleporter.teleport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter.EnumType;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;


/**
 * TeleporterNetwork is the singleton responsible for saving the teleporter data onto the world file, and is
 * responsible for retrieving destination and source nodes during teleportation.
 */
public class TeleporterNetwork extends WorldSavedData
{

	/*
	 * These versions pertain solely to the teleporter network. As new changes are made to the network, old network versions will require every teleporter to re-register within the network, to prevent older worlds from breaking.
	 */
	/**
	 * Teleporter Network Versioning Major (x.0)
	 */
	private final int VERSION_MAJOR = 2;
	/**
	 * Teleporter Network Versioning Minor (0.x)
	 */
	private final int VERSION_MINOR = 0;


	private Map<String, List<TeleporterNode>> network = new HashMap<String, List<TeleporterNode>>();


	public TeleporterNetwork()
	{
		super(TeleporterMod.MODID);
	}

	public TeleporterNetwork(String identifier)
	{
		super(identifier);
	}


	public static TeleporterNetwork get(World world)
	{
		TeleporterNetwork instance = (TeleporterNetwork)world.getMapStorage().getOrLoadData(TeleporterNetwork.class, TeleporterMod.MODID);
		if (instance == null)
		{
			instance = new TeleporterNetwork();
			world.setData(TeleporterMod.MODID, instance);
			instance.markDirty();
		}
		return instance;
	}


	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagCompound networkTag = nbt.getCompoundTag("Network");

		// attempt to read the major and minor version numbers from the network (these will return zero if not present)
		int versionMajor = nbt.getInteger("NetworkVersionMajor");
		int versionMinor = nbt.getInteger("NetworkVersionMinor");

		// clear the map if it's not already empty
		if (!this.network.isEmpty()) this.network.clear();

		// if we're reading an old-style (single network) data structure...
		if (versionMajor < 2)
		{
			// iterate through the tags in the network list
			NBTTagList nbttaglist = nbt.getTagList("Network", NBT.TAG_COMPOUND);
			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				// create a teleport node from the current nbt tag
				TeleporterNode node = new TeleporterNode(nbttaglist.getCompoundTagAt(i));

				// get the key for the subnet by checking the stack stored in the tile tntity's item handler for this node
				WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.dimension);
				if (world != null)
				{
					TileEntityTeleporter tile = (TileEntityTeleporter)world.getTileEntity(node.pos);
					if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
					{
						IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
						node.key = getItemKey(handler.getStackInSlot(0));
					}
				}

				// if a subnet for the node key doesn't already exist in the network map, add it
				if (!network.containsKey(node.key))
				{
					network.put(node.key, new ArrayList<TeleporterNode>());
				}

				// add the node to the collection for the target subnet
				network.get(node.key).add(node);

			}
			this.markDirty();
		}

		// if we're reading a new-style (multiple network) data structure...
		else
		{
			// iterate over the subkey keys in the network
			for ( String key : networkTag.getKeySet() )
			{
				// create a list of nodes for this subnet key
				List<TeleporterNode> subnetList = new ArrayList<TeleporterNode>();

				// iterate through the tags in the subnet
				NBTTagList nbttaglist = networkTag.getTagList(key, NBT.TAG_COMPOUND);
				for (int i = 0; i < nbttaglist.tagCount(); ++i)
				{
					// create a teleport node from the current nbt tag
					TeleporterNode node = new TeleporterNode(nbttaglist.getCompoundTagAt(i));

					// add the node to the subnet list
					subnetList.add(node);
				}

				// add the subnet list to the network
				this.network.put(key, subnetList);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagCompound networkTag = new NBTTagCompound();

		for (Map.Entry<String, List<TeleporterNode>> entry : network.entrySet())
		{
			String itemKey = entry.getKey();
			List<TeleporterNode> list = entry.getValue();
			Iterator<TeleporterNode> it = list.iterator();

			if (!list.isEmpty())
			{
				NBTTagList listTag = new NBTTagList();

				while (it.hasNext())
				{
					NBTTagCompound nodeNBT = it.next().writeToNBT(new NBTTagCompound());
					listTag.appendTag(nodeNBT);
				}

				networkTag.setTag(itemKey, listTag);
			}
		}

		nbt.setInteger("NetworkVersionMajor", VERSION_MAJOR);
		nbt.setInteger("NetworkVersionMinor", VERSION_MINOR);
		nbt.setTag("Network", networkTag);
		return nbt;
	}


	public Set<String> getSubnets()
	{
		return network.keySet();
	}

	public int getSubnetSize(String subnetKey)
	{
		if (network.containsKey(subnetKey))
		{
			return network.get(subnetKey).size();
		}
		return 0;
	}


	/**
	 * Gets a node from the network
	 * @param pos The world position
	 * @param dimension The dimension id
	 * @return The node, or null if no node is found
	 */
	public TeleporterNode getNode(BlockPos pos, int dimension)
	{
		Iterator<List<TeleporterNode>> mit = network.values().iterator();
		while (mit.hasNext())
		{

			Iterator<TeleporterNode> lit = mit.next().iterator();
			while (lit.hasNext())
			{
				TeleporterNode node = lit.next();
				if (node.matches(pos, dimension))
				{
					return node;
				}
			}

		}

		return null;
	}

//	public TeleporterNode getNode(BlockPos pos, int dimension, ItemStack key)
//	{
//		String itemKey = getItemKey(key);
//		if (network.containsKey(itemKey))
//		{
//			Iterator<TeleporterNode> lit = network.get(itemKey).iterator();
//			while (lit.hasNext())
//			{
//				TeleporterNode node = lit.next();
//				if (node.matches(pos, dimension))
//				{
//					return node;
//				}
//			}
//
//		}
//
//		return null;
//	}

	/**
	 * Adds a node to the network
	 * @param node The node to be added
	 */
	public void addNode(TeleporterNode node)
	{
		if (!network.containsKey(node.key))
		{
			network.put(node.key, new ArrayList<TeleporterNode>());
		}
		network.get(node.key).add(node);
		this.markDirty();
	}


//	/**
//	 * Update's a node's item key, and reassigns it in the network if necessary
//	 * @param node The node to be updated
//	 */
//	public void updateNode(TeleporterNode node)
//	{
//		ItemStack stack = node.getTileEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
//		String newKey = getItemKey(stack);
//		if (!node.key.equals(newKey))
//		{
//			// remove node from network
//			removeNode(node);
//			// set new key
//			node.key = newKey;
//			// add node back to network
//			addNode(node);
//			// update nbt data
//			this.markDirty();
//		}
//	}


	public void updateNode(BlockPos pos, int dimension, EnumType type, ItemStack stack)
	{
		String newKey = getItemKey(stack);
		String oldKey = null;

		boolean found = false;

		// iterate the subnets in the network
		for ( String key : network.keySet() )
		{
			// iterate the nodes in the subnet
			for ( TeleporterNode node : network.get(key))
			{
				// if this node matches the one we're looking for
				if (node.matches(pos, dimension))
				{
					// if the key hasn't changed, do nothing
					if (node.key.equals(newKey)) return;

					// make a note of the old key and remove the node
					network.get(key).remove(node);
					oldKey = key;

					found = true;
					break;
				}

			}
			if (found) break;
		}

		// create a new subket for the new key if one doesn't already exist
		if (!network.containsKey(newKey)) network.put(newKey, new ArrayList<TeleporterNode>());

		// add a new node to the correct subnet for the new key
		network.get(newKey).add(new TeleporterNode(pos, dimension, type, newKey));

		// remove the old subnet if it's now empty
		if (network.get(oldKey).isEmpty()) network.remove(oldKey);


		this.markDirty();
	}



	/**
	 * Removes a node from the network
	 * @param pos The node's position
	 * @param dimension The node's dimension id
	 * @param key The node's key
	 * @return True if remove was successful, false otherwise
	 */
	public boolean removeNode(BlockPos pos, int dimension, ItemStack key)
	{
		String itemKey = getItemKey(key);
		if (network.containsKey(itemKey))
		{
			Iterator<TeleporterNode> lit = network.get(itemKey).iterator();
			while (lit.hasNext())
			{
				TeleporterNode node = lit.next();
				if (node.matches(pos, dimension))
				{
					lit.remove();
					if (network.get(node.key).isEmpty())
					{
						network.remove(node.key);
					}
					this.markDirty();
					return true;
				}
			}

		}

		return false;
	}

	public boolean removeNode(TeleporterNode node)
	{
		if (network.containsKey(node.key))
		{
			boolean res = network.get(node.key).remove(node);
			if (network.get(node.key).isEmpty())
			{
				network.remove(node.key);
			}
			return res;
		}
		return false;
	}

	/**
	 * Gets the next node that can be teleported to from the target teleporter
	 * @param entityIn The entity being teleported (for messaging purposes)
	 * @param sourceNode The beginning node
	 * @return A valid node if one exists, or null otherwise
	 */
	public TeleporterNode getNextNode(Entity entityIn, TeleporterNode sourceNode)
	{
		// get the top-most entity (rider) for sending messages
		Entity livingEntity = entityIn;
		while (!livingEntity.getPassengers().isEmpty())
		{
			livingEntity = livingEntity.getControllingPassenger();
		}

		List<TeleporterNode> subnet = this.network.get(sourceNode.key);

		System.out.println("Checking teleporter subnet " + sourceNode.key);

		TeleporterNode destinationNode = null;

		int sourceIndex = subnet.indexOf(sourceNode);
		for (int i = (sourceIndex + 1) % subnet.size(); i != sourceIndex; i = (i + 1) % subnet.size())
		{
			TeleporterNode currentNode = subnet.get(i);

			WorldServer destinationWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(currentNode.dimension);
			if (destinationWorld != null)
			{
				// if a tile entity doesn't exist at the specified node location, remove the node and continue
				TileEntityTeleporter tEntDest = getTileEntity(currentNode);
				if (tEntDest == null)
				{
					System.out.println("Invalid node found! Deleting...");
					removeNode(currentNode);
					continue;
				}

				// if the teleporter types are different, continue
				if (sourceNode.type.isEnder() != currentNode.type.isEnder())
				{
					continue;
				}

				// if the teleporter isn't inter-dimensional and the dimensions are different, continue
				if (!sourceNode.type.isEnder() && sourceNode.dimension != currentNode.dimension)
				{
					continue;
				}

				// if the destination node is obstructed, continue
				if (isObstructed(destinationWorld, currentNode))
				{
					if (livingEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer)livingEntity;
						entityPlayer.sendMessage(this.getMessage("teleporterBlocked"));
					}
					continue;
				}

				// if the destination node is powered, continue
				if (tEntDest.isPowered() == true)
				{
					if (livingEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer)livingEntity;
						entityPlayer.sendMessage(this.getMessage("teleporterDisabled"));
					}
					continue;
				}

				// if all above conditions are met, we've found a valid destination node.
				destinationNode = currentNode;
				break;
			}
		}

		if (destinationNode == null && livingEntity instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer)livingEntity;
			entityPlayer.sendMessage(this.getMessage("teleporterNotFound"));
		}

		return destinationNode;
	}

	/**
	 * Gets a chat message for the player, given a string id
	 * @param messageName The unlocalized message name
	 * @return The message to be sent to the player
	 */
	private TextComponentTranslation getMessage(String messageName)
	{
		return new TextComponentTranslation("message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + messageName);
	}

	/**
	 * Determines whether the teleporter block is being obstructed for purposes of teleporting
	 * @param world
	 * @param node
	 * @return True if the teleporter is obstructed, false otherwise
	 */
	public static boolean isObstructed(World world, TeleporterNode node)
	{
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

	private static TileEntityTeleporter getTileEntity(TeleporterNode node)
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
				key += ":" + stack.getItemDamage();

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



	private static class TeleporterMap extends HashMap<String, List<TeleporterNode>>
	{
		private static class TeleporterSubnet extends ArrayList<TeleporterNode>
		{
		}

	}

}
