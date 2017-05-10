package net.dyeo.teleporter.teleport;

import java.util.ArrayList;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
		if (this.network.size() != 0) this.network.clear();

		for (int i = 0; i < nbtNetwork.tagCount(); i++)
		{
			NBTTagCompound nbtNode = nbtNetwork.getCompoundTagAt(i);
			TeleporterNode node = new TeleporterNode(nbtNode);
			this.network.add(node);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		NBTTagList nbtNetwork = new NBTTagList();

		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = this.network.get(i);
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
		return null;
	}

	public void addNode(TeleporterNode node)
	{
		this.network.add(node);
		this.markDirty();
	}

	public boolean removeNode(int x, int y, int z, int dimension)
	{
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = this.network.get(i);
			if (node.matches(x, y, z, dimension))
			{
				this.network.remove(node);
				this.markDirty();
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

		int index = this.network.indexOf(sourceNode);
		for (int i = index + 1; i < this.network.size() + index; i++)
		{

			TeleporterNode node = this.network.get(i % this.network.size());

			WorldServer destinationWorld = MinecraftServer.getServer().worldServerForDimension(node.dimension);
			if (destinationWorld != null)
			{
				// if this node matches the source node, continue
				if (node == sourceNode)
				{
					continue;
				}

				// if the teleporter types are different, continue
				if (sourceNode.type != node.type)
				{
					continue;
				}

				// if the teleporter isn't inter-dimensional and the dimensions are different, continue
				if (sourceNode.type == BlockTeleporter.EnumType.REGULAR && sourceNode.dimension != node.dimension)
				{
					continue;
				}

				// if a tile entity doesn't exist at the specified node location, continue
				TileEntityTeleporter destinationTileEntity = (TileEntityTeleporter)destinationWorld.getTileEntity(node.x, node.y, node.z);
				if (destinationTileEntity == null)
				{
					continue;
				}

				// if the key itemstacks are different, continue
				ItemStack destinationKey = destinationTileEntity.getStackInSlot(0);
				if (!this.doKeyStacksMatch(sourceKey, destinationKey))
				{
					continue;
				}

				// if the destination node is obstructed, continue
				if (this.isObstructed(destinationWorld, node))
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
						entityPlayer.addChatMessage(this.GetMessage("teleporterBlocked"));
					}
					continue;
				}

				// if the destination node is powered, continue
				if (destinationTileEntity.isPowered() == true)
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
						entityPlayer.addChatMessage(this.GetMessage("teleporterDisabled"));
					}
					continue;
				}

				// if all above conditions are met, we've found a valid destination node.
				destinationNode = node;
				break;
			}
		}

		if (destinationNode == null && potentialPlayerEntity instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
			entityPlayer.addChatMessage(this.GetMessage("teleporterNotFound"));
		}

		return destinationNode;
	}

	private boolean isObstructed(World world, TeleporterNode node)
	{
		Block block1 = world.getBlock(node.x, node.y + 1, node.z);
		Block block2 = world.getBlock(node.x, node.y + 2, node.z);

		if (!block1.getMaterial().blocksMovement() && !block2.getMaterial().blocksMovement())
		{
			return false;
		}
		else
		{
			return true;
		}
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
