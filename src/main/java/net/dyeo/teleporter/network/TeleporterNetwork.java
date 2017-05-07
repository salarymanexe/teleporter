package net.dyeo.teleporter.network;

import java.util.ArrayList;
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

	private ArrayList<TeleporterNode> network = new ArrayList<TeleporterNode>();

	public TeleporterNetwork()
	{
		super(TeleporterMod.MODID);
	}

	public TeleporterNetwork(String identifier)
	{
		super(identifier);
	}


	public static TeleporterNetwork instance(World world)
	{
		TeleporterNetwork data = (TeleporterNetwork)world.getMapStorage().getOrLoadData(TeleporterNetwork.class, TeleporterMod.MODID);
		if (data == null)
		{
			data = new TeleporterNetwork();
			world.setData(TeleporterMod.MODID, data);
		}

		data.markDirty();
		return data;
	}


	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList netNBT = nbt.getTagList("Network", NBT.TAG_COMPOUND);

		if (network.size() != 0) network.clear();

		for (int i = 0; i < netNBT.tagCount(); ++i)
		{
			NBTTagCompound nodeNBT = netNBT.getCompoundTagAt(i);
			TeleporterNode node = new TeleporterNode(nodeNBT);
			network.add(node);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList netNBT = new NBTTagList();

		for (int i = 0; i < network.size(); ++i)
		{
			TeleporterNode node = network.get(i);
			NBTTagCompound nodeNBT = node.writeToNBT(new NBTTagCompound());
			netNBT.appendTag(nodeNBT);
		}

		nbt.setTag("Network", netNBT);
		return nbt;
	}



	public TeleporterNode getNode(BlockPos pos, int dimension)
	{
		for (int i = 0; i < network.size(); ++i)
		{
			TeleporterNode node = network.get(i);
			if (node.matches(pos, dimension))
			{
				return node;
			}
		}
		return null;
	}


	public void addNode(TeleporterNode node)
	{
		network.add(node);
		markDirty();
	}

	public boolean removeNode(BlockPos pos, int dimension)
	{
		for (int i = 0; i < network.size(); ++i)
		{
			TeleporterNode node = network.get(i);
			if (node.matches(pos, dimension))
			{
				network.remove(node);
				return true;
			}
		}
		return false;
	}


	/**
	 * gets the next node that can be teleported to from the target teleporter
	 *
	 */
	public TeleporterNode getNextNode(Entity entityIn, TeleporterNode sourceNode)
	{

		TileEntityTeleporter tEntSource = (TileEntityTeleporter)entityIn.world.getTileEntity(sourceNode.pos);
		ItemStack sourceKey = tEntSource.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);

		TeleporterNode destinationNode = null;

		// get the top-most entity (rider) for sending messages
		Entity potentialPlayerEntity = entityIn;
		while (!potentialPlayerEntity.getPassengers().isEmpty())
		{
			potentialPlayerEntity = potentialPlayerEntity.getControllingPassenger();
		}

		int index = network.indexOf(sourceNode);
		for (int i = index + 1; i < network.size() + index; ++i)
		{

			TeleporterNode node = network.get(i % network.size());

			WorldServer destinationWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.dimension);
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
				TileEntityTeleporter tEntDest = (TileEntityTeleporter)destinationWorld.getTileEntity(node.pos);
				if (tEntDest == null)
				{
					continue;
				}

				// if the key itemstacks are different, continue
				ItemStack destinationKey = tEntDest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
				if (!doKeyStacksMatch(sourceKey, destinationKey))
				{
					continue;
				}

				// if the destination node is obstructed, continue
				if (isObstructed(destinationWorld, node))
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
						entityPlayer.sendMessage(getMessage("teleporterBlocked"));
					}
					continue;
				}

				// if the destination node is powered, continue
				if (tEntDest.isPowered() == true)
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
						entityPlayer.sendMessage(getMessage("teleporterDisabled"));
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
			EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
			entityPlayer.sendMessage(getMessage("teleporterNotFound"));
		}

		return destinationNode;
	}




	private TextComponentTranslation getMessage(String messageName)
	{
		return new TextComponentTranslation("message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + messageName);
	}

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

	private boolean doKeyStacksMatch(ItemStack sourceKey, ItemStack destinationKey)
	{
		// if keys are completely different
		if (sourceKey.isEmpty() && !destinationKey.isEmpty())
		{
			return false; // skip this destination
		}
		else if (!sourceKey.isEmpty() && destinationKey.isEmpty())
		{
			return false; // skip this destination
		}

		if (!sourceKey.isEmpty() && !destinationKey.isEmpty())
		{
			// check if keys are the same
			if (sourceKey.getItem().getUnlocalizedName().equals(destinationKey.getItem().getUnlocalizedName()) == false)
			{
				return false;
			}

			// both items are written books
			if (sourceKey.getItem() == Items.WRITTEN_BOOK && destinationKey.getItem() == Items.WRITTEN_BOOK)
			{

				// get author and title for A as "author:title"
				String author = sourceKey.getTagCompound().getString("author");
				author += ":" + sourceKey.getTagCompound().getString("title");

				// get author and title for B as "author:title"
				String nodeAuthor = destinationKey.getTagCompound().getString("author");
				nodeAuthor += ":" + destinationKey.getTagCompound().getString("title");
				if (author.equals(nodeAuthor) == false)
				{
					return false;
				}
			}
			else if (sourceKey.getItem() == Items.FILLED_MAP && destinationKey.getItem() == Items.FILLED_MAP)
			{
				// compare map value (stored in item damage)
				if (sourceKey.getItemDamage() != destinationKey.getItemDamage())
				{
					// skip this destination
					return false;
				}
			}
			else
			{
				// item naming
				String name = "", nodeName = "";
				// set item A name if first item has tag compound
				if (sourceKey.hasTagCompound())
				{
					NBTTagCompound display = (NBTTagCompound) sourceKey.getTagCompound().getTag("display");
					name = display.getString("Name");
				}
				// set item B name if second item has tag compound
				if ((destinationKey.hasTagCompound()))
				{
					NBTTagCompound display = (NBTTagCompound) destinationKey.getTagCompound().getTag("display");
					nodeName = display.getString("Name");
				}
				// compare resulting names to see if they are a unique pair
				if (name.equals(nodeName) == false)
				{
					return false;
				}
			}
		}
		return true;
	}

}
