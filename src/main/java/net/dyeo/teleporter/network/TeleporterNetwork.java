package net.dyeo.teleporter.network;

import java.util.ArrayList;
import net.dyeo.teleporter.blocks.BlockTeleporterBase;
import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
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

public class TeleporterNetwork extends WorldSavedData
{
	private ArrayList<TeleporterNode> network = new ArrayList();
	private static final String IDENTIFIER = "teleporter".toLowerCase();

	public TeleporterNetwork()
	{
		super(IDENTIFIER);
	}

	public TeleporterNetwork(String identifier)
	{
		super(identifier);
	}

	public ChatComponentTranslation GetMessage(String messageName)
	{
		return new ChatComponentTranslation("message." + "teleporter".toLowerCase() + '_' + this.getClass().getSimpleName() + '.' + messageName, new Object[0]);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		int NBT_TYPE = 10;
		NBTTagList netNBT = nbt.getTagList("Network", 10);
		if (this.network.size() != 0)
		{
			this.network.clear();
		}
		for (int i = 0; i < netNBT.tagCount(); i++)
		{
			TeleporterNode tempNode = new TeleporterNode();
			NBTTagCompound nodeNBT = netNBT.getCompoundTagAt(i);
			tempNode.readFromNBT(nodeNBT);
			this.network.add(tempNode);

			System.out.println("Read worldData node " + i);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList netNBT = new NBTTagList();
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode tempNode = (TeleporterNode)this.network.get(i);
			NBTTagCompound nodeNBT = new NBTTagCompound();
			tempNode.writeToNBT(nodeNBT);
			netNBT.appendTag(nodeNBT);

			System.out.println("Saved worldData node " + i);
		}
		nbt.setTag("Network", netNBT);
	}

	private boolean isObstructed(World world, TeleporterNode node)
	{
		int posx = node.posx;
		int posy1 = node.posy + 1;
		int posy2 = node.posy + 2;
		int posz = node.posz;
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

	public TeleporterNode getNextNode(Entity entityIn, ItemStack stack, TeleporterNode source)
	{
		TileEntityTeleporter tileEntityTeleporterSource = TileEntityTeleporter.getTileEntityAt(entityIn.worldObj, source.posx, source.posy, source.posz);

		TeleporterNode destinationNode = null;

		Entity potentialPlayerEntity = entityIn;
		while (potentialPlayerEntity.riddenByEntity != null)
		{
			potentialPlayerEntity = potentialPlayerEntity.riddenByEntity;
		}
		int index = this.network.indexOf(source);
		for (int i = index + 1; i < this.network.size() + index; i++)
		{
			TeleporterNode node = (TeleporterNode)this.network.get(i % this.network.size());

			WorldServer destinationWorld = MinecraftServer.getServer().worldServerForDimension(node.dimension);
			if (destinationWorld != null)
			{
				TileEntityTeleporter tileEntityTeleporterDestination = TileEntityTeleporter.getTileEntityAt(destinationWorld, node.posx, node.posy, node.posz);

				BlockTeleporterBase blockSource = (BlockTeleporterBase)tileEntityTeleporterSource.getBlockType();
				BlockTeleporterBase blockDestination = (BlockTeleporterBase)tileEntityTeleporterDestination.getBlockType();
				if ((blockSource != null) && (blockDestination != null))
				{
					if (blockSource.getClass() == blockDestination.getClass())
					{
						if ((blockSource.getInterdimensional()) || (source.dimension == node.dimension))
						{
							if (node != source)
							{
								if ((stack != null) || (tileEntityTeleporterDestination.itemStacks[0] == null))
								{
									if ((stack == null) || (tileEntityTeleporterDestination.itemStacks[0] != null))
									{
										if ((stack != null) && (tileEntityTeleporterDestination.itemStacks[0] != null))
										{
											if (!stack.getItem().getUnlocalizedName().equals(tileEntityTeleporterDestination.itemStacks[0].getItem().getUnlocalizedName()))
											{
												continue;
											}
											if ((stack.getItem() == Items.written_book) && (tileEntityTeleporterDestination.itemStacks[0].getItem() == Items.written_book))
											{
												String author = stack.getTagCompound().getString("author");
												author = author + ":" + stack.getTagCompound().getString("title");

												String nodeAuthor = tileEntityTeleporterDestination.itemStacks[0].getTagCompound().getString("author");
												nodeAuthor = nodeAuthor + ":" + tileEntityTeleporterDestination.itemStacks[0].getTagCompound().getString("title");
												if (!author.equals(nodeAuthor))
												{
													continue;
												}
											}
											else if ((stack.getItem() == Items.filled_map) && (tileEntityTeleporterDestination.itemStacks[0].getItem() == Items.filled_map))
											{
												if (stack.getItemDamage() != tileEntityTeleporterDestination.itemStacks[0].getItemDamage())
												{
													continue;
												}
											}
											else
											{
												String name = "";
												String nodeName = "";
												if (stack.hasTagCompound())
												{
													NBTTagCompound display = (NBTTagCompound)stack.getTagCompound().getTag("display");
													name = display.getString("Name");
												}
												if (tileEntityTeleporterDestination.itemStacks[0].hasTagCompound())
												{
													NBTTagCompound display = (NBTTagCompound)tileEntityTeleporterDestination.itemStacks[0].getTagCompound().getTag("display");
													nodeName = display.getString("Name");
												}
												if (!name.equals(nodeName))
												{
													continue;
												}
											}
										}
										boolean obstructed = this.isObstructed(destinationWorld, node);
										if (obstructed == true)
										{
											if ((potentialPlayerEntity instanceof EntityPlayer))
											{
												EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
												entityPlayer.addChatMessage(this.GetMessage("teleporterBlocked"));
											}
										}
										else if (tileEntityTeleporterDestination.isPowered() == true)
										{
											if ((potentialPlayerEntity instanceof EntityPlayer))
											{
												EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
												entityPlayer.addChatMessage(this.GetMessage("teleporterDisabled"));
											}
										}
										else
										{
											destinationNode = node;
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if ((destinationNode == null) && ((potentialPlayerEntity instanceof EntityPlayer)))
		{
			EntityPlayer entityPlayer = (EntityPlayer)potentialPlayerEntity;
			entityPlayer.addChatMessage(this.GetMessage("teleporterNotFound"));
			System.out.println("[Teleporter] Destination not found");
		}
		return destinationNode;
	}

	public TeleporterNode getNode(int posx, int posy, int posz, int dimension, boolean debug)
	{
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = (TeleporterNode)this.network.get(i);
			if ((posx == node.posx) && (posy == node.posy) && (posz == node.posz) && (dimension == node.dimension))
			{
				if (debug)
				{
					System.out.println("Getting node at " + posx + "," + posy + "," + posz + " from network");
				}
				return node;
			}
		}
		if (debug)
		{
			System.out.println("No node at " + posx + "," + posy + "," + posz + " found in network");
		}
		return null;
	}

	public void addNode(TeleporterNode node)
	{
		int index = this.network.size();
		this.network.add(node);
		this.markDirty();
		System.out.println("Appending node at " + node.posx + "," + node.posy + "," + node.posz + " to network " + "[" + index + "]");
	}

	public boolean removeNode(int posx, int posy, int posz, int dimension)
	{
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = (TeleporterNode)this.network.get(i);
			if ((posx == node.posx) && (posy == node.posy) && (posz == node.posz) && (dimension == node.dimension))
			{
				this.network.remove(node);
				System.out.println("Removing node at " + posx + "," + posy + "," + posz + " from network " + '[' + i + ']');
				return true;
			}
		}
		System.out.println("ERROR: No node at " + posx + "," + posy + "," + posz + " found in network");
		return false;
	}

	public static TeleporterNetwork get(World world, boolean debug)
	{
		TeleporterNetwork data = (TeleporterNetwork)world.loadItemData(TeleporterNetwork.class, IDENTIFIER);
		if (data == null)
		{
			if (debug)
			{
				System.out.println("New network created!");
			}
			data = new TeleporterNetwork();
			world.setItemData(IDENTIFIER, data);
		}
		else if (debug)
		{
			System.out.println("Network loaded!");
		}
		data.markDirty();
		return data;
	}
}
