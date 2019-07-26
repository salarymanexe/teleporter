package net.dyeo.teleporter.teleport;

import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;

/**
 * TeleporterNetwork is the singleton responsible for saving the teleporter data onto the world file, and is
 * responsible for retrieving destination and source nodes during teleportation.
 *
 */
public class TeleporterNetwork extends WorldSavedData
{
	private ArrayList<TeleporterNode> network = new ArrayList<TeleporterNode>();

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
		NBTTagList netNBT = nbt.getTagList("Network", NBT.TAG_COMPOUND);

		if (this.network.size() != 0) this.network.clear();

		for (int i = 0; i < netNBT.tagCount(); ++i)
		{
			NBTTagCompound nodeNBT = netNBT.getCompoundTagAt(i);
			TeleporterNode node = new TeleporterNode(nodeNBT);
			this.network.add(node);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList netNBT = new NBTTagList();

		for (int i = 0; i < this.network.size(); ++i)
		{
			TeleporterNode node = this.network.get(i);
			NBTTagCompound nodeNBT = node.writeToNBT(new NBTTagCompound());
			netNBT.appendTag(nodeNBT);
		}

		nbt.setTag("Network", netNBT);
		return nbt;
	}

	public void addNode(TeleporterNode node)
	{
		this.network.add(node);
		this.markDirty();
	}

	public boolean removeNode(BlockPos pos, int dimension)
	{
		for (int i = 0; i < this.network.size(); ++i)
		{
			TeleporterNode node = this.network.get(i);
			if (node.matches(pos, dimension))
			{
				this.network.remove(node);
				this.markDirty();
				return true;
			}
		}
		return false;
	}

	public TeleporterNode getNode(BlockPos pos, int dimension)
	{
		for (int i = 0; i < this.network.size(); ++i)
		{
			TeleporterNode node = this.network.get(i);
			if (node.matches(pos, dimension))
			{
				return node;
			}
		}
		return null;
	}

	/**
	 * gets the next node that can be teleported to from the target teleporter
	 *
	 */
	public TeleporterNode getNextNode(Entity entity, TeleporterNode srcNode)
	{
		if(entity == null)
		{
			return null;
		}

		TileEntityTeleporter srcTileEntity = (TileEntityTeleporter)entity.world.getTileEntity(srcNode.pos);

		TeleporterNode dstNode = null;

		// get the top-most entity (rider) for sending messages
		Entity potentialPlayerEntity = entity;
		while (!potentialPlayerEntity.getPassengers().isEmpty())
		{
			potentialPlayerEntity = potentialPlayerEntity.getControllingPassenger();
		}

		int index = this.network.indexOf(srcNode);
		for (int i = index + 1; i < this.network.size() + index; ++i)
		{
			TeleporterNode node = this.network.get(i % this.network.size());

			WorldServer dstWorld = entity.getServer().getWorld(node.dimension);

			// if this node matches the source node, continue
			if (node == srcNode)
			{
				continue;
			}

			// if a tile entity doesn't exist at the specified node location, continue
			TileEntityTeleporter dstTileEntity = (TileEntityTeleporter)dstWorld.getTileEntity(node.pos);
			if (dstTileEntity == null)
			{
				continue;
			}

			// if the teleporter types are different, continue
			if (srcNode.type != node.type)
			{
				continue;
			}

			// if the teleporter isn't inter-dimensional and the dimensions are different, continue
			if (srcNode.type == BlockTeleporter.EnumType.REGULAR && srcNode.dimension != node.dimension)
			{
				continue;
			}

			// if the teleporter's names or the key itemstacks are different, continue
			ItemStack srcKey = srcTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
			ItemStack dstKey = dstTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
			if (!srcTileEntity.getName().equals(dstTileEntity.getName()) || !doKeyStacksMatch(srcKey, dstKey))
			{
				continue;
			}

			// if the destination node is obstructed, continue
			if (isObstructed(dstWorld, node))
			{
				if (potentialPlayerEntity instanceof EntityPlayer)
				{
					EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
					entityPlayer.sendStatusMessage(TeleporterUtility.getMessage("teleporterBlocked"), true);
				}
				continue;
			}

			// if the destination node is powered, continue
			if (dstTileEntity.isPowered())
			{
				if (potentialPlayerEntity instanceof EntityPlayer)
				{
					EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
					entityPlayer.sendStatusMessage(TeleporterUtility.getMessage("teleporterDisabled"), true);
				}
				continue;
			}

			// if all above conditions are met, we've found a valid destination node.
			dstNode = node;
			break;
		}

		if (dstNode == null && potentialPlayerEntity instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
			entityPlayer.sendStatusMessage(TeleporterUtility.getMessage("teleporterNotFound"), true);
		}

		return dstNode;
	}

	private static boolean isObstructed(World world, TeleporterNode node)
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

	private static boolean doKeyStacksMatch(ItemStack srcKey, ItemStack dstKey)
	{
		// if both keys are null, they match (obviously!)
		if (srcKey.isEmpty() && dstKey.isEmpty())
		{
			return true;
		}
		// if one or both keys are not null...
		else
		{
			// if they're both not null...
			if (!(srcKey.isEmpty() || dstKey.isEmpty()))
			{
				// ensure that the items match
				if (srcKey.getItem() != dstKey.getItem()) return false;
				// ensure that the item metadata matches
				if (srcKey.getItemDamage() != dstKey.getItemDamage()) return false;

				// if the source key has an NBT tag
				if (srcKey.hasTagCompound())
				{
					// ensure that the destination key also has an NBT tag
					if (!dstKey.hasTagCompound()) return false;

					// if the key items are written books
					if (srcKey.getItem() == Items.WRITTEN_BOOK)
					{
						// ensure that the book authors and titles match
						String sourceBookNBT = srcKey.getTagCompound().getString("author") + ":" + srcKey.getTagCompound().getString("title");
						String destinationBookNBT = dstKey.getTagCompound().getString("author") + ":" + dstKey.getTagCompound().getString("title");
						if (!sourceBookNBT.equals(destinationBookNBT)) return false;
					}
					// if it's any other type of item
					else
					{
						// ensure that the nbt tags match
						if (!ItemStack.areItemStackTagsEqual(srcKey, dstKey)) return false;
					}
				}

				// if we're still here, everything matches
				return true;
			}

			// if one key is null and the other is not, they don't match
			else return false;
		}
	}
}
