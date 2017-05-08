package net.dyeo.teleporter.network;

import java.util.ArrayList;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.dyeo.teleporter.util.Vec3i;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * TeleporterNetwork is the singleton responsible for saving the teleporter data onto the world file, and is
 * responsible for retrieving destination and source nodes during teleportation.
 *
 */
public class TeleporterNetwork extends WorldSavedData
{

	private ArrayList<TeleporterNode> network = new ArrayList();

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
		TeleporterNetwork instance = (TeleporterNetwork)world.loadItemData(TeleporterNetwork.class, TeleporterMod.MODID);
		if (instance == null)
		{
			instance = new TeleporterNetwork();
			world.setItemData(TeleporterMod.MODID, instance);
			instance.markDirty();
		}
		return instance;
	}


	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		NBTTagList nbtNetwork = compound.getTagList("Network", NBT.TAG_COMPOUND);
		if (network.size() != 0) network.clear();

		for (int i = 0; i < nbtNetwork.tagCount(); i++)
		{
			NBTTagCompound nbtNode = nbtNetwork.getCompoundTagAt(i);
			TeleporterNode node = new TeleporterNode(nbtNode);
			network.add(node);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		NBTTagList nbtNetwork = new NBTTagList();

		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = network.get(i);
			NBTTagCompound nbtNode = node.writeToNBT(new NBTTagCompound());
			nbtNetwork.appendTag(nbtNode);
		}

		compound.setTag("Network", nbtNetwork);
	}


	public TeleporterNode getNode(int x, int y, int z, int dimension)
	{
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = (TeleporterNode)this.network.get(i);
			if (node.matches(x, y, z, dimension))
			{
				return node;
			}
		}

		System.out.println("TeleportNetwork.getNode :: no node found at " + new Vec3i(x, y, z).toString());
		return null;
	}

	public void addNode(TeleporterNode node)
	{
		this.network.add(node);
		System.out.println("TeleportNetwork.addNode :: added node " + node.toString());
		this.markDirty();
	}

	public boolean removeNode(int x, int y, int z, int dimension)
	{
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = network.get(i);
			if (node.matches(x, y, z, dimension))
			{
				this.network.remove(node);
				this.markDirty();
				System.out.println("TeleportNetwork.removeNode :: removed node " + node.toString());
				return true;
			}
		}
		return false;
	}


	/**
	 * gets the next node that can be teleported to from the target teleporter
	 *
	 */
	public TeleporterNode getNextNode(EntityLivingBase entity, TeleporterNode sourceNode)
	{
		TileEntityTeleporter sourceTileEntity = (TileEntityTeleporter)entity.worldObj.getTileEntity(sourceNode.x, sourceNode.y, sourceNode.z);
		ItemStack sourceKey = sourceTileEntity.getStackInSlot(0);

		TeleporterNode destinationNode = null;

		// get the top-most entity (rider) for sending messages
		Entity potentialPlayerEntity = entity;
		while (potentialPlayerEntity.riddenByEntity != null)
		{
			potentialPlayerEntity = potentialPlayerEntity.riddenByEntity;
		}


		System.out.println("getNextNode :: network.size = " + this.network.size());

		int index = this.network.indexOf(sourceNode);

		System.out.println("getNextNode :: index = " + index);

		for (int i = index + 1; i < this.network.size() + index; i++)
		{
			TeleporterNode node = this.network.get(i % this.network.size());

			System.out.println("getNextNode :: node[" + i % this.network.size() + "] = " + (node == null ? "null" : node.toString()));


			WorldServer destinationWorld = MinecraftServer.getServer().worldServerForDimension(node.dimension);
			if (destinationWorld != null)
			{
				// if this node matches the source node, continue
				if (node == sourceNode)
				{
					System.out.println("getNextNode :: node[" + i % this.network.size() + "] matches source node");
					continue;
				}

				// if the teleporter types are different, continue
				if (sourceNode.type != node.type)
				{
					System.out.println("getNextNode :: node[" + i % this.network.size() + "] type different from source node");
					continue;
				}

				// if the teleporter isn't inter-dimensional and the dimensions are different, continue
				if (sourceNode.type == BlockTeleporter.EnumType.REGULAR && sourceNode.dimension != node.dimension)
				{
					System.out.println("getNextNode :: node[" + i % this.network.size() + "] is for a different dimension");
					continue;
				}

				// if a tile entity doesn't exist at the specified node location, continue
				TileEntityTeleporter destinationTileEntity = (TileEntityTeleporter)destinationWorld.getTileEntity(node.x, node.y, node.z);
				if (destinationTileEntity == null)
				{
					System.out.println("getNextNode :: node[" + i % this.network.size() + "] does not have a tile entity");
					continue;
				}

				// if the key itemstacks are different, continue
				ItemStack destinationKey = destinationTileEntity.getStackInSlot(0);
				if (!doKeyStacksMatch(sourceKey, destinationKey))
				{
					System.out.println("getNextNode :: node[" + i % this.network.size() + "] keys differ");
					continue;
				}

				// if the destination node is obstructed, continue
				if (isObstructed(destinationWorld, node))
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
						entityPlayer.addChatMessage(GetMessage("teleporterBlocked"));
					}
					continue;
				}

				// if the destination node is powered, continue
				if (destinationTileEntity.isPowered() == true)
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
						entityPlayer.addChatMessage(GetMessage("teleporterDisabled"));
					}
					continue;
				}

				// if all above conditions are met, we've found a valid destination node.
				System.out.println("getNextNode :: destinationNode == node[" + i % this.network.size() + "]");
				destinationNode = node;
				break;

			}
			else
			{
				System.out.println("getNextNode :: destinationWorld is null");
			}

		}

		if (destinationNode == null && potentialPlayerEntity instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
			entityPlayer.addChatMessage(this.GetMessage("teleporterNotFound"));
		}

		System.out.println("getNextNode :: destinationNode = " + destinationNode == null ? "null" : destinationNode.toString());

		return destinationNode;
	}

	private boolean isObstructed(World world, TeleporterNode node)
	{
		int posx = node.x;
		int posy1 = node.y + 1;
		int posy2 = node.y + 2;
		int posz = node.z;
		Block bl1 = world.getBlock(posx, posy1, posz);
		Block bl2 = world.getBlock(posx, posy2, posz);
		if ((bl1 == null) && (bl2 == null))
		{
			return false;
		}
		if (((bl1 == Blocks.wall_sign) || (bl1 == Blocks.standing_sign) || (bl1 == Blocks.lever) || (bl1 == Blocks.vine) || (bl1 == Blocks.torch) || (bl1 == Blocks.air) || (bl1 == Blocks.redstone_torch) || (bl1 == Blocks.ladder)) && ((bl2 == Blocks.wall_sign) || (bl2 == Blocks.standing_sign) || (bl2 == Blocks.lever) || (bl2 == Blocks.vine) || (bl2 == Blocks.torch) || (bl2 == Blocks.air) || (bl2 == Blocks.redstone_torch) || (bl2 == Blocks.ladder)))
		{
			return false;
		}
		return true;
	}

	private boolean doKeyStacksMatch(ItemStack sourceKey, ItemStack destinationKey)
	{
		// if keys are completely different
		if (sourceKey == null && destinationKey != null)
		{
			return false; // skip this destination
		}
		else if (sourceKey != null && destinationKey == null)
		{
			return false; // skip this destination
		}

		if (sourceKey != null && destinationKey != null)
		{
			// check if keys are the same
			if (sourceKey.getItem().getUnlocalizedName().equals(destinationKey.getItem().getUnlocalizedName()) == false)
			{
				return false;
			}

			// if both items are written books
			if (sourceKey.getItem() == Items.written_book && destinationKey.getItem() == Items.written_book)
			{
				// check to see if the authors and titles match
				String sourceAuthor = sourceKey.getTagCompound().getString("author") + ":" + sourceKey.getTagCompound().getString("title");
				String destinationAuthor = destinationKey.getTagCompound().getString("author") + ":" + destinationKey.getTagCompound().getString("title");

				if (!sourceAuthor.equals(destinationAuthor))
				{
					return false;
				}
			}
			// if both items are filled maps
			else if (sourceKey.getItem() == Items.filled_map && destinationKey.getItem() == Items.filled_map)
			{
				// check to see if the share a damage value (i.e. are duplicates of each other)
				if (sourceKey.getItemDamage() != destinationKey.getItemDamage())
				{
					return false;
				}
			}
			else
			{
				// item naming
				String sourceName = "", destinationName = "";
				// set item A name if first item has tag compound
				if (sourceKey.hasTagCompound())
				{
					NBTTagCompound display = (NBTTagCompound)sourceKey.getTagCompound().getTag("display");
					sourceName = display.getString("Name");
				}
				// set item B name if second item has tag compound
				if ((destinationKey.hasTagCompound()))
				{
					NBTTagCompound display = (NBTTagCompound)destinationKey.getTagCompound().getTag("display");
					destinationName = display.getString("Name");
				}
				// compare resulting names to see if they are a unique pair
				if (!sourceName.equals(destinationName))
				{
					return false;
				}
			}
		}
		return true;
	}

	public ChatComponentTranslation GetMessage(String messageName)
	{
		return new ChatComponentTranslation("message." + "teleporter".toLowerCase() + '_' + this.getClass().getSimpleName() + '.' + messageName, new Object[0]);
	}

}
