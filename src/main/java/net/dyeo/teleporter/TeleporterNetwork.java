package net.dyeo.teleporter;

import java.util.ArrayList;
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

			System.out.println("[Teleporter][readFromNBT] Read worldData node " + i);
		}
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList netNBT = new NBTTagList();
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode tempNode = (TeleporterNode)this.network.get(i);
			NBTTagCompound nodeNBT = new NBTTagCompound();
			tempNode.writeToNBT(nodeNBT);
			netNBT.appendTag(nodeNBT);

			System.out.println("[Teleporter][writeToNBT] Saved worldData node " + i);
		}
		nbt.setTag("Network", netNBT);
	}

	private boolean isObstructed(World world, TeleporterNode node)
	{
		Block bl1 = world.getBlock(node.posx, node.posy + 1, node.posz);
		Block bl2 = world.getBlock(node.posx, node.posy + 2, node.posz);
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

	public TeleporterNode getNextNode(Entity entityIn, World world, ItemStack stack, TeleporterNode source)
	{
		TileEntityTeleporter sourceEnt = TileEntityTeleporter.getTileEntityAt(world, source.posx, source.posy, source.posz);

		boolean playerEnt = false;
		if ((entityIn instanceof EntityPlayer))
		{
			playerEnt = true;
		}
		int index = this.network.indexOf(source);
		for (int i = index + 1; i < this.network.size() + index; i++)
		{
			boolean isUnique = false;
			boolean matches = false;
			boolean diffDimension = false;

			TeleporterNode node = (TeleporterNode)this.network.get(i % this.network.size());

			WorldServer worldDst = MinecraftServer.getServer().worldServerForDimension(node.dimension);
			if (worldDst != null)
			{
				TileEntityTeleporter nodeEnt = TileEntityTeleporter.getTileEntityAt(worldDst, node.posx, node.posy, node.posz);
				if (node == source)
				{
					matches = false;
				}
				if (source.dimension != node.dimension)
				{
					matches = false;
					diffDimension = true;
				}
				if ((stack == null) && (nodeEnt.itemStacks[0] != null))
				{
					matches = false;
				}
				else if ((stack != null) && (nodeEnt.itemStacks[0] == null))
				{
					matches = false;
				}
				else if ((stack == null) && (nodeEnt.itemStacks[0] == null))
				{
					matches = true;
				}
				else if (stack.getItem().getUnlocalizedName().equals(nodeEnt.itemStacks[0].getItem().getUnlocalizedName()))
				{
					matches = true;
				}
				if (matches == true)
				{
					if ((stack != null) && (nodeEnt.itemStacks[0] != null))
					{
						if ((stack.getItem() == Items.written_book) && (nodeEnt.itemStacks[0].getItem() == Items.written_book))
						{
							String author = stack.getTagCompound().getString("author");
							author = author + ":" + stack.getTagCompound().getString("title");

							String nodeAuthor = nodeEnt.itemStacks[0].getTagCompound().getString("author");
							nodeAuthor = nodeAuthor + ":" + nodeEnt.itemStacks[0].getTagCompound().getString("title");
							if (author.equals(nodeAuthor))
							{
								matches = true;
							}
							else
							{
								matches = false;
							}
						}
						else if ((stack.getItem() == Items.filled_map) && (nodeEnt.itemStacks[0].getItem() == Items.filled_map))
						{
							if (stack.getMetadata() == nodeEnt.itemStacks[0].getMetadata())
							{
								matches = true;
							}
							else
							{
								matches = false;
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
							if (nodeEnt.itemStacks[0].hasTagCompound())
							{
								NBTTagCompound display = (NBTTagCompound)nodeEnt.itemStacks[0].getTagCompound().getTag("display");
								nodeName = display.getString("Name");
							}
							if (name.equals(nodeName))
							{
								matches = true;
							}
							else
							{
								matches = false;
							}
						}
					}
				}
				boolean obstructed = isObstructed(world, node);
				if ((matches == true) && (!obstructed) && (!diffDimension) && (!nodeEnt.isPowered))
				{
					return node;
				}
				if ((matches == true) && (obstructed == true) && (!diffDimension))
				{
					EntityPlayer entp = (EntityPlayer)entityIn;
					if (playerEnt == true)
					{
						entp.addChatMessage(new ChatComponentTranslation("Teleporter is blocked; skipping...", new Object[0]));
					}
				}
				else if ((matches == true) && (!obstructed) && (!diffDimension) && (nodeEnt.isPowered == true))
				{
					EntityPlayer entp = (EntityPlayer)entityIn;
					if (playerEnt == true)
					{
						entp.addChatMessage(new ChatComponentTranslation("Teleporter is disabled; skipping...", new Object[0]));
					}
				}
			}
		}
		if (playerEnt == true)
		{
			EntityPlayer entp = (EntityPlayer)entityIn;
			entp.addChatMessage(new ChatComponentTranslation("No teleporters found that match your key.", new Object[0]));
		}
		System.out.println("[Teleporter] Destination not found");
		return null;
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
					System.out.println("[Teleporter][getNode] Getting node " + posx + "," + posy + "," + posz + " from network");
				}
				return node;
			}
		}
		if (debug)
		{
			System.out.println("[Teleporter][getNode] No node " + posx + "," + posy + "," + posz + " found in network");
		}
		return null;
	}

	public void addNode(TeleporterNode node)
	{
		int index = this.network.size();
		this.network.add(node);
		markDirty();
		System.out.println("[Teleporter][addNode] Appending node " + node.posx + "," + node.posy + "," + node.posz + " to network " + "[" + index + "]");
	}

	public boolean removeNode(int posx, int posy, int posz, int dimension)
	{
		for (int i = 0; i < this.network.size(); i++)
		{
			TeleporterNode node = (TeleporterNode)this.network.get(i);
			if ((posx == node.posx) && (posy == node.posy) && (posz == node.posz) && (dimension == node.dimension))
			{
				this.network.remove(node);
				System.out.println("[Teleporter][removeNode] Removing node " + posx + "," + posy + "," + posz + " from network");
				return true;
			}
		}
		System.out.println("[Teleporter][removeNode] No node " + posx + "," + posy + "," + posz + " found in network");
		return false;
	}

	public static TeleporterNetwork get(World world, boolean debug)
	{
		TeleporterNetwork data = (TeleporterNetwork)world.loadItemData(TeleporterNetwork.class, IDENTIFIER);
		if (data == null)
		{
			if (debug)
			{
				System.out.println("[Teleporter][get] New network created!");
			}
			data = new TeleporterNetwork();
			world.setItemData(IDENTIFIER, data);
		}
		else if (debug)
		{
			System.out.println("[Teleporter][get] Network loaded!");
		}
		data.markDirty();
		return data;
	}
}
