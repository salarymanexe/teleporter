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


	public static TeleporterNetwork get(World world)
	{
		TeleporterNetwork instance = (TeleporterNetwork)world.getMapStorage().getOrLoadData(TeleporterNetwork.class, TeleporterMod.MODID);
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

		for (int i = 0; i < nbtNetwork.tagCount(); ++i)
		{
			NBTTagCompound nbtNode = nbtNetwork.getCompoundTagAt(i);
			TeleporterNode node = new TeleporterNode(nbtNode);
			this.network.add(node);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagList nbtNetwork = new NBTTagList();

		for (int i = 0; i < this.network.size(); ++i)
		{
			TeleporterNode node = this.network.get(i);
			NBTTagCompound nbtNode = node.writeToNBT(new NBTTagCompound());
			nbtNetwork.appendTag(nbtNode);
		}

		compound.setTag("Network", nbtNetwork);
		return compound;
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


	/**
	 * gets the next node that can be teleported to from the target teleporter
	 *
	 */
	public TeleporterNode getNextNode(EntityLivingBase entity, TeleporterNode sourceNode)
	{

		TileEntityTeleporter tEntSource = (TileEntityTeleporter)entity.worldObj.getTileEntity(sourceNode.pos);
		ItemStack sourceKey = tEntSource.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);

		TeleporterNode destinationNode = null;

		// get the top-most entity (rider) for sending messages
		Entity potentialPlayerEntity = entity;
		while (!potentialPlayerEntity.getPassengers().isEmpty())
		{
			potentialPlayerEntity = potentialPlayerEntity.getControllingPassenger();
		}

		int index = this.network.indexOf(sourceNode);
		for (int i = index + 1; i < this.network.size() + index; ++i)
		{

			TeleporterNode node = this.network.get(i % this.network.size());

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
						entityPlayer.addChatMessage(this.getMessage("teleporterBlocked"));
					}
					continue;
				}

				// if the destination node is powered, continue
				if (tEntDest.isPowered() == true)
				{
					if (potentialPlayerEntity instanceof EntityPlayer)
					{
						EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
						entityPlayer.addChatMessage(this.getMessage("teleporterDisabled"));
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
			entityPlayer.addChatMessage(this.getMessage("teleporterNotFound"));
		}

		return destinationNode;
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
			if (sourceKey.getItem() == Items.WRITTEN_BOOK && destinationKey.getItem() == Items.WRITTEN_BOOK)
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
			else if (sourceKey.getItem() == Items.FILLED_MAP && destinationKey.getItem() == Items.FILLED_MAP)
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

	private TextComponentTranslation getMessage(String messageName)
	{
		return new TextComponentTranslation("message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + messageName);
	}

}
