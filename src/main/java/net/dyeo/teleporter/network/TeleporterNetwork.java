package net.dyeo.teleporter.network;

import java.util.ArrayList;
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
import net.minecraftforge.fml.common.FMLCommonHandler;

/*
 * TeleporterNetwork is the singleton responsible for saving the teleporter data onto the world file, and is
 * responsible for retrieving destination and source nodes during teleportation.
 */
public class TeleporterNetwork extends WorldSavedData
{

	private ArrayList<TeleporterNode> network = new ArrayList<TeleporterNode>();

	private static final String IDENTIFIER = TeleporterMod.MODID;

	public TeleporterNetwork()
	{
		super(IDENTIFIER);
	}

	public TeleporterNetwork(String identifier)
	{
		super(identifier);
	}

	// gets a human readable message to be used in the teleporter network
	public TextComponentTranslation GetMessage(String messageName)
	{
		return new TextComponentTranslation("message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + messageName);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		final int NBT_TYPE = 10; // compound
		NBTTagList netNBT = nbt.getTagList("Network", NBT_TYPE);

		if (network.size() != 0)
		{
			network.clear();
		}

		for (int i = 0; i < netNBT.tagCount(); ++i)
		{
			TeleporterNode tempNode = new TeleporterNode();
			NBTTagCompound nodeNBT = netNBT.getCompoundTagAt(i);
			tempNode.readFromNBT(nodeNBT);
			network.add(tempNode);

			System.out.println("Read worldData node " + i);
		}

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList netNBT = new NBTTagList();

		for (int i = 0; i < network.size(); ++i)
		{
			TeleporterNode tempNode = network.get(i);
			NBTTagCompound nodeNBT = new NBTTagCompound();
			tempNode.writeToNBT(nodeNBT);
			netNBT.appendTag(nodeNBT);

			System.out.println("Saved worldData node " + i);
		}

		nbt.setTag("Network", netNBT);
		return nbt;
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

		/*
		 * if (( bl1 == Blocks.wall_sign || bl1 == Blocks.standing_sign || bl1 == Blocks.lever || bl1 == Blocks.vine || bl1 == Blocks.torch || bl1 == Blocks.air
		 * || bl1 == Blocks.redstone_torch || bl1 == Blocks.ladder bl1.isPassable(world, bl1p)) && ( bl2 == Blocks.wall_sign || bl2 == Blocks.standing_sign ||
		 * bl2 == Blocks.lever || bl2 == Blocks.vine || bl2 == Blocks.torch || bl2 == Blocks.air || bl2 == Blocks.redstone_torch || bl2 == Blocks.ladder
		 * bl2.isPassable(world, bl2p))) { return false; }
		 */
	}

	// gets the next node that can be teleported to from the target teleporter
	public TeleporterNode getNextNode(Entity entityIn, ItemStack stack, TeleporterNode source)
	{

		TileEntityTeleporter tEntSource = TileEntityTeleporter.getTileEntityAt(entityIn.worldObj, source.pos);

		// a teleporter with matching key, no obstructions, and no lock
		TeleporterNode destinationNode = null;

		// get the top-most entity (rider) for sending messages
		Entity potentialPlayerEntity = entityIn;
		while (!potentialPlayerEntity.getPassengers().isEmpty())
		{
			potentialPlayerEntity = potentialPlayerEntity.getControllingPassenger();
		}

		int index = network.indexOf(source);
		for (int i = index + 1; i < network.size() + index; ++i)
		{

			TeleporterNode node = network.get(i % network.size());

			WorldServer destinationWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(node.dimension);

			if (destinationWorld != null)
			{
				// get destination tile entity
				TileEntityTeleporter tEntDest = TileEntityTeleporter.getTileEntityAt(destinationWorld, node.pos);

				if (tEntDest == null)
				{
					continue;
				}

				// if teleporter types are different, skip the loop
				if (tEntSource.getTypeProperty().getID() != tEntDest.getTypeProperty().getID())
				{
					continue; // skip this destination
				}

				// if block doesn't travel different dimensions, and if
				// dimensions are different
				if ((tEntSource.getInterdimensional() == false) && (source.dimension != node.dimension))
				{
					continue; // skip this destination
				}

				// if teleporter is trying to teleport to itself
				if (node == source)
				{
					continue; // skip this destination
				}

				// if keys are completely different
				if (stack == null && tEntDest.itemStacks[0] != null)
				{
					continue; // skip this destination
				}
				else if (stack != null && tEntDest.itemStacks[0] == null)
				{
					continue; // skip this destination
				}

				if (stack != null && tEntDest.itemStacks[0] != null)
				{
					// check if keys are the same
					if (stack.getItem().getUnlocalizedName()
							.equals(tEntDest.itemStacks[0].getItem().getUnlocalizedName()) == false)
					{
						continue;
					}

					// both items are written books
					if (stack.getItem() == Items.WRITTEN_BOOK && tEntDest.itemStacks[0].getItem() == Items.WRITTEN_BOOK)
					{

						// get author and title for A as "author:title"
						String author = stack.getTagCompound().getString("author");
						author += ":" + stack.getTagCompound().getString("title");

						// get author and title for B as "author:title"
						String nodeAuthor = tEntDest.itemStacks[0].getTagCompound().getString("author");
						nodeAuthor += ":" + tEntDest.itemStacks[0].getTagCompound().getString("title");
						if (author.equals(nodeAuthor) == false)
						{
							continue;
						}
					}
					else if (stack.getItem() == Items.FILLED_MAP
							&& tEntDest.itemStacks[0].getItem() == Items.FILLED_MAP)
					{
						// compare map value (stored in item damage)
						if (stack.getItemDamage() != tEntDest.itemStacks[0].getItemDamage())
						{
							// skip this destination
							continue;
						}
					}
					else
					{
						// item naming
						String name = "", nodeName = "";
						// set item A name if first item has tag compound
						if (stack.hasTagCompound())
						{
							NBTTagCompound display = (NBTTagCompound) stack.getTagCompound().getTag("display");
							name = display.getString("Name");
						}
						// set item B name if second item has tag compound
						if ((tEntDest.itemStacks[0].hasTagCompound()))
						{
							NBTTagCompound display = (NBTTagCompound) tEntDest.itemStacks[0].getTagCompound()
									.getTag("display");
							nodeName = display.getString("Name");
						}
						// compare resulting names to see if they are a unique
						// pair
						if (name.equals(nodeName) == false)
						{
							continue;
						}
					}
				}

				// check if the destination block is obstructed
				boolean obstructed = isObstructed(destinationWorld, node);

				if (obstructed == true)
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
						entityPlayer.addChatMessage(GetMessage("teleporterBlocked"));
					}
					continue;
				}
				else if (tEntDest.isPowered() == true)
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
						entityPlayer.addChatMessage(GetMessage("teleporterDisabled"));
					}
					continue;
				}
				else
				{
					destinationNode = node;
					break;
				}
			}
		}

		if (destinationNode == null && potentialPlayerEntity instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer) potentialPlayerEntity;
			entityPlayer.addChatMessage(GetMessage("teleporterNotFound"));
			System.out.println("[Teleporter] Destination not found");
		}
		return destinationNode;

	}

	public TeleporterNode getNode(BlockPos pos, int dimension, boolean debug)
	{
		for (int i = 0; i < network.size(); ++i)
		{
			TeleporterNode node = network.get(i);
			if (pos.getX() == node.pos.getX() && pos.getY() == node.pos.getY() && pos.getZ() == node.pos.getZ()
					&& dimension == node.dimension)
			{
				if (debug) System.out.println(
						"Getting node at " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " from network");
				return node;
			}
		}

		if (debug)
			System.out.println("No node at " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " found in network");
		return null;
	}

	// add node to network
	public void addNode(TeleporterNode node)
	{
		int index = network.size();
		network.add(node);
		markDirty();
		System.out.println("Appending node at " + node.pos.getX() + "," + node.pos.getY() + "," + node.pos.getZ()
				+ " to network " + "[" + index + "]");
	}

	// remove node from network
	public boolean removeNode(BlockPos pos, int dimension)
	{
		for (int i = 0; i < network.size(); ++i)
		{
			TeleporterNode node = network.get(i);
			if (pos.getX() == node.pos.getX() && pos.getY() == node.pos.getY() && pos.getZ() == node.pos.getZ()
					&& dimension == node.dimension)
			{
				network.remove(node);
				System.out.println("Removing node at " + pos.getX() + "," + pos.getY() + "," + pos.getZ()
						+ " from network " + '[' + i + ']');
				return true;
			}
		}

		System.out
				.println("ERROR: No node at " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " found in network");
		return false;
	}

	// get the network instance
	public static TeleporterNetwork get(World world, boolean debug)
	{
		TeleporterNetwork data = (TeleporterNetwork) world.getMapStorage().getOrLoadData(TeleporterNetwork.class, IDENTIFIER);

		if (data == null)
		{
			if (debug) System.out.println("New network created!");
			data = new TeleporterNetwork();
			world.setItemData(IDENTIFIER, data);
		}
		else
		{
			if (debug) System.out.println("Network loaded!");
		}

		data.markDirty();
		return data;
	}

}
