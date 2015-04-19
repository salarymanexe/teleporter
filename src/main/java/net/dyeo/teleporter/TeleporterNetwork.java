package net.dyeo.teleporter;

import java.util.ArrayList;

import com.sun.org.apache.bcel.internal.Constants;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/*
 * TeleporterNetwork is the singleton responsible for saving the teleporter data onto the world file, and is 
 * responsible for retrieving destination and source nodes during teleportation.
 */
public class TeleporterNetwork extends WorldSavedData
{
	
	//
	
	private ArrayList<TeleporterNode> network = new ArrayList<TeleporterNode>();
	
	//
	
	private static final String IDENTIFIER = "teleporter";

	public TeleporterNetwork() 
	{
		super(IDENTIFIER);
	}
	
	public TeleporterNetwork(String identifier)
	{
		super(identifier);
	}
	
	//

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		final int NBT_TYPE = 10; // compound
		NBTTagList netNBT = nbt.getTagList("Network", NBT_TYPE);
		
		if(network.size() != 0)
		{
			network.clear();
		}
		
		for(int i = 0; i < netNBT.tagCount(); ++i)
		{
			TeleporterNode tempNode = new TeleporterNode();
			NBTTagCompound nodeNBT = netNBT.getCompoundTagAt(i);
			tempNode.readFromNBT(nodeNBT);
			network.add(tempNode);
			
			System.out.println("[Teleporter][readFromNBT] Read worldData node " + i);
		}
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		NBTTagList netNBT = new NBTTagList();
		
		for(int i = 0; i < network.size(); ++i)
		{
			TeleporterNode tempNode = network.get(i);
			NBTTagCompound nodeNBT = new NBTTagCompound();
			tempNode.writeToNBT(nodeNBT);
			netNBT.appendTag(nodeNBT);
			
			System.out.println("[Teleporter][writeToNBT] Saved worldData node " + i);
		}
		
		nbt.setTag("Network", netNBT);		
	}
	
	private boolean isObstructed(World world, TeleporterNode node)
	{
		BlockPos bl1p = new BlockPos(node.pos.getX(),node.pos.getY()+1,node.pos.getZ());
		BlockPos bl2p = new BlockPos(node.pos.getX(),node.pos.getY()+2,node.pos.getZ());
		Block bl1 = world.getBlockState(bl1p).getBlock();
		Block bl2 = world.getBlockState(bl2p).getBlock();
		
		if ((/*bl1 == Blocks.wall_sign || bl1 == Blocks.standing_sign || bl1 == Blocks.lever || bl1 == Blocks.vine || bl1 == Blocks.torch || bl1 == Blocks.air || bl1 == Blocks.redstone_torch || bl1 == Blocks.ladder || */bl1.isPassable(world, bl1p)) 
				&& 
			(/*bl2 == Blocks.wall_sign || bl2 == Blocks.standing_sign || bl2 == Blocks.lever || bl2 == Blocks.vine || bl2 == Blocks.torch || bl2 == Blocks.air || bl2 == Blocks.redstone_torch || bl2 == Blocks.ladder || */bl2.isPassable(world, bl2p)))
		{
			return false;
		}
		
		return true;
	}
	
	public TeleporterNode getNextNode(Entity entityIn, World world, ItemStack stack, TeleporterNode source)
	{
		TileEntityTeleporter sourceEnt = TileEntityTeleporter.getTileEntityAt(world, source.pos);
		
		boolean playerEnt = false;
		if(entityIn instanceof EntityPlayer)
		{
			playerEnt = true;
		}
		int index = network.indexOf(source);
		for(int i = index+1; i < network.size()+index; ++i)
		{
			TeleporterNode node =  network.get(i % network.size());
			TileEntityTeleporter nodeEnt = TileEntityTeleporter.getTileEntityAt(world, node.pos);
			
			boolean isUnique = false;
			boolean matches = false;
			boolean diffDimension = false;
			
			if(stack != null) if(stack.getItem() == Items.written_book || stack.getItem() == Items.filled_map)
			{
				isUnique = true;
			}
			
			// check if dimensions are different
			if(node == source)
			{
				matches = false;
			}
			if(source.dimension != node.dimension)
			{
				matches = false;
				diffDimension = true;
			}
			// check if keys are completely different
			else if(stack == null && nodeEnt.itemStacks[0] != null)
			{
				matches = false;
			}
			else if(stack != null && nodeEnt.itemStacks[0] == null)
			{
				matches = false;
			}
			// check if keys are both empty
			else if(stack == null && nodeEnt.itemStacks[0] == null)
			{
				matches = true;			
			}
			// check if keys are the same 
			else if(stack.getItem().getUnlocalizedName().equals(nodeEnt.itemStacks[0].getItem().getUnlocalizedName()))	
			{
				matches = true;
			}
			
			if (isUnique == true && matches == true) 
			{
				if (stack.getItem() == Items.written_book && nodeEnt.itemStacks[0].getItem() == Items.written_book) 
				{
					String author = stack.getTagCompound().getString("author");
					author += ":" + stack.getTagCompound().getString("title");
					
					String nodeAuthor = nodeEnt.itemStacks[0].getTagCompound().getString("author");
					nodeAuthor += ":" + nodeEnt.itemStacks[0].getTagCompound().getString("title");
					if (author.equals(nodeAuthor)) 
					{
						matches = true;
					} 
					else 
					{
						matches = false;
					}
				} 
				else if (stack.getItem() == Items.filled_map && nodeEnt.itemStacks[0].getItem() == Items.filled_map) 
				{
					if (stack.getItemDamage() == nodeEnt.itemStacks[0].getItemDamage()) 
					{
						matches = true;
					} 
					else 
					{
						matches = false;
					}
				}
			}
			
			boolean obstructed = isObstructed(world,node);
			
			if(matches == true && obstructed == false && diffDimension == false && nodeEnt.isPowered == false)
			{
				return node;
			}
			else if(playerEnt == true && matches == true && obstructed == true && diffDimension == false)
			{
				EntityPlayer entp = (EntityPlayer)entityIn;
				entp.addChatMessage(new ChatComponentTranslation("Teleporter is blocked; skipping..."));
			}
			else if(playerEnt == true && matches == true && obstructed == false && diffDimension == false && nodeEnt.isPowered == true)
			{
				EntityPlayer entp = (EntityPlayer)entityIn;
				entp.addChatMessage(new ChatComponentTranslation("Teleporter is disabled; skipping..."));
			}
		}

		if(playerEnt == true)
		{
			EntityPlayer entp = (EntityPlayer)entityIn;
			entp.addChatMessage(new ChatComponentTranslation("No teleporters found that match your key."));
		}
		
		System.out.println("[Teleporter] Destination not found");
		return null;
	}
	
	public TeleporterNode getNode(BlockPos pos, int dimension, boolean debug)
	{
		for(int i = 0; i < network.size(); ++i)
		{
			TeleporterNode node = network.get(i);
			if(pos.getX() == node.pos.getX() && pos.getY() == node.pos.getY() && pos.getZ() == node.pos.getZ() && dimension == node.dimension)
			{
				if(debug) System.out.println("[Teleporter][getNode] Getting node " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " from network");
				return node;
			}
		}
		
		if(debug) System.out.println("[Teleporter][getNode] No node " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " found in network");
		return null;
	}
	
	public void addNode(TeleporterNode node)
	{
		int index = network.size();
		network.add(node);
		markDirty();
		System.out.println("[Teleporter][addNode] Appending node " + node.pos.getX() + "," + node.pos.getY() + "," + node.pos.getZ() + " to network " + "[" + index + "]");
	}
	
	public boolean removeNode(BlockPos pos, int dimension)
	{
		for(int i = 0; i < network.size(); ++i)
		{
			TeleporterNode node = network.get(i);
			if(pos.getX() == node.pos.getX() && pos.getY() == node.pos.getY() && pos.getZ() == node.pos.getZ() && dimension == node.dimension)
			{
				network.remove(node);
				System.out.println("[Teleporter][removeNode] Removing node " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " from network");
				return true;
			}
		}
		
		System.out.println("[Teleporter][removeNode] No node " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " found in network");
		return false;
	}
	
	public static TeleporterNetwork get(World world, boolean debug)
	{
		TeleporterNetwork data = (TeleporterNetwork)world.loadItemData(TeleporterNetwork.class, IDENTIFIER);
		
		if(data == null)
		{
			if(debug) System.out.println("[Teleporter][get] New network created!");
			data = new TeleporterNetwork();
			world.setItemData(IDENTIFIER, data);
		}
		else
		{
			if(debug) System.out.println("[Teleporter][get] Network loaded!");
		}		
		
		data.markDirty();		
		return data;		
	}

}
