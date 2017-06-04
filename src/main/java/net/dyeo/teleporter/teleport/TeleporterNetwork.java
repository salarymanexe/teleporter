package net.dyeo.teleporter.teleport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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

/**
 * TeleporterNetwork is the singleton responsible for saving the teleporter data onto the world file, and is
 * responsible for retrieving destination and source nodes during teleportation.
 *
 */
public class TeleporterNetwork extends WorldSavedData
{
	public boolean runtimeRebuild = false;

	private final int VERSION_MAJOR = 2;
	private final int VERSION_MINOR = 0;

	private HashMap<String, ArrayList<TeleporterNode>> network = new HashMap<String, ArrayList<TeleporterNode>>();

	public Set<String> getSubnets()
	{
		return network.keySet();
	}
	
	public int getSubnetSize(String subnetKey)
	{
		if(network.containsKey(subnetKey))
		{
			return network.get(subnetKey).size();
		}
		return 0;
	}
	
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

		int versionMajor = networkTag.getInteger("VersionMajor");
		int versionMinor = networkTag.getInteger("VersionMinor");

		if (versionMajor < VERSION_MAJOR || (versionMajor == VERSION_MAJOR && versionMinor < VERSION_MINOR))
		{
			this.runtimeRebuild = true;
		}
		else
		{
			this.runtimeRebuild = false;
		}

		if (!this.network.isEmpty()) this.network.clear();

		Iterator<String> st = networkTag.getKeySet().iterator();
		while (st.hasNext())
		{
			String key = st.next();

			ArrayList<TeleporterNode> subnetList = new ArrayList<TeleporterNode>();
			this.network.put(key, subnetList);

			NBTTagList listTag = networkTag.getTagList(key, NBT.TAG_COMPOUND);

			for (int i = 0; i < listTag.tagCount(); ++i)
			{
				NBTTagCompound nodeNBT = listTag.getCompoundTagAt(i);
				TeleporterNode node = new TeleporterNode(nodeNBT);
				subnetList.add(node);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagCompound networkTag = new NBTTagCompound();

		for (HashMap.Entry<String, ArrayList<TeleporterNode>> entry : network.entrySet())
		{
			String itemKey = entry.getKey();
			ArrayList<TeleporterNode> list = entry.getValue();
			Iterator<TeleporterNode> it = list.iterator();

			NBTTagList listTag = new NBTTagList();

			while (it.hasNext())
			{
				NBTTagCompound nodeNBT = it.next().writeToNBT(new NBTTagCompound());
				listTag.appendTag(nodeNBT);
			}

			networkTag.setTag(itemKey, listTag);
		}

		networkTag.setInteger("VersionMajor", VERSION_MAJOR);
		networkTag.setInteger("VersionMinor", VERSION_MINOR);
		nbt.setTag("Network", networkTag);
		return nbt;
	}

	/**
	 * Gets a node from the network
	 * @param pos The world position
	 * @param dimension The dimension id
	 * @return The node, or null if no node is found
	 */
	public TeleporterNode getNode(BlockPos pos, int dimension)
	{
		Iterator<ArrayList<TeleporterNode>> mit = network.values().iterator();
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

	public TeleporterNode getNode(BlockPos pos, int dimension, ItemStack key)
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
					return node;
				}
			}

		}

		return null;
	}

	/**
	 * Adds a node to the network
	 * @param node The node to be added
	 */
	public void addNode(TeleporterNode node)
	{
		if (network.containsKey(node.key))
		{
			network.get(node.key).add(node);
		}
		else
		{
			ArrayList<TeleporterNode> nodeList = new ArrayList<TeleporterNode>();
			nodeList.add(node);
			network.put(node.key, nodeList);
		}
		this.markDirty();
	}

	/**
	 * Update's a node's item key, and reassigns it in the network if necessary
	 * @param node The node to be updated
	 */
	public void updateNode(TeleporterNode node)
	{
		ItemStack stack = node.getTileEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
		String newKey = getItemKey(stack);
		if (!node.key.equals(newKey))
		{
			// remove node from network
			removeNode(node);
			// set new key
			node.key = newKey;
			// add node back to network
			addNode(node);
			// update nbt data
			this.markDirty();
		}
	}

	/**
	 * Removes a node from the network
	 * @param pos The node's position
	 * @param dimension The node's dimension id
	 * @return True if remove was successful, false otherwise
	 */
	public boolean removeNode(BlockPos pos, int dimension)
	{
		Iterator<ArrayList<TeleporterNode>> mit = network.values().iterator();
		while (mit.hasNext())
		{

			Iterator<TeleporterNode> lit = mit.next().iterator();
			while (lit.hasNext())
			{
				TeleporterNode node = lit.next();
				if (node.matches(pos, dimension))
				{
					lit.remove();
					this.markDirty();
					return true;
				}
			}

		}

		return false;
	}

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
			return network.get(node.key).remove(node);
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

		TileEntityTeleporter tEntSource = (TileEntityTeleporter)entityIn.world.getTileEntity(sourceNode.pos);

		TeleporterNode destinationNode = null;

		// get the top-most entity (rider) for sending messages
		Entity livingEntity = entityIn;
		while (!livingEntity.getPassengers().isEmpty())
		{
			livingEntity = livingEntity.getControllingPassenger();
		}

		ArrayList<TeleporterNode> subnet = this.network.get(sourceNode.key);

		System.out.println("Checking teleporter subnet " + sourceNode.key);

		int index = subnet.indexOf(sourceNode);
		for (int i = index + 1; i < subnet.size() + index; ++i)
		{
			TeleporterNode node = subnet.get(i % subnet.size());

			WorldServer destinationWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.dimension);
			if (destinationWorld != null)
			{
				// if this node matches the source node, continue
				if (node == sourceNode)
				{
					continue;
				}

				// if the teleporter types are different, continue
				if (sourceNode.type.isEnder() != node.type.isEnder())
				{
					continue;
				}

				// if the teleporter isn't inter-dimensional and the dimensions are different, continue
				if (!sourceNode.type.isEnder() && sourceNode.dimension != node.dimension)
				{
					continue;
				}

				// if a tile entity doesn't exist at the specified node location, continue
				TileEntityTeleporter tEntDest = (TileEntityTeleporter)destinationWorld.getTileEntity(node.pos);
				if (tEntDest == null)
				{
					continue;
				}

				ItemStack sourceKey = tEntSource.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
				ItemStack destinationKey = tEntDest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);

				// if the key itemstacks are different, continue
				if (!getItemKey(sourceKey).equals(getItemKey(destinationKey)))
				{
					System.out.print("FAILED\n");
					continue;
				}

				System.out.print("SUCCESS\n");

				// if the destination node is obstructed, continue
				if (this.isObstructed(destinationWorld, node))
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
				destinationNode = node;
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
	private boolean isObstructed(World world, TeleporterNode node)
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

	/**
	 * Generates a unique item key pertaining to an item stack. Takes into account all unique values except for stack size. The return of this function is guaranteed to produce the same key for two identical items, and takes into account NBT tags, damage, and the unlocalized name. Mods which implement two different blocks/items with the same unlocalized name will be treated as the same.
	 * @param stack The item stack to generate a key from
	 * @return The unique key
	 */
	private String getItemKey(ItemStack stack)
	{
		String key = stack.getUnlocalizedName();

		if (stack != null)
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

}
