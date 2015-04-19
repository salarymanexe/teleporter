package net.dyeo.teleporter;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * BlockInventoryBasic is a simple inventory capable of storing 9 item stacks. The block itself doesn't do much more
 * then any regular block except create a tile entity when placed, open a gui when right clicked and drop tne
 * inventory's contents when harvested. The actual storage is handled by the tile entity.
 */
public class BlockTeleporter extends BlockContainer
{
		
	public BlockTeleporter()
	{
		super(Material.rock);
		this.setCreativeTab(CreativeTabs.tabTransport);     // the block will appear on the Blocks tab.
		this.setResistance(30);
		this.setHardness(3.0f);
		this.setLightLevel(0.5f);
	}

	// Called when the block is placed or loaded client side to get the tile entity for the block
	// Should return a new instance of the tile entity for the block
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityTeleporter();
	}

	// Called when the block is right clicked
	// In this block it is used to open the blocks gui when right clicked by a player
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		// Uses the gui handler registered to your mod to open the gui for the given gui id
		// open on the server side only  (not sure why you shouldn't open client side too... vanilla doesn't, so we better not either)
		if (worldIn.isRemote) return true;

		if(!playerIn.isSneaking()) playerIn.openGui(Teleporter.instance, GuiHandlerTeleporter.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn)
	{
		TeleporterPlayer tplayer = null;
		if(entityIn instanceof EntityPlayer)
		{
			tplayer = TeleporterPlayer.get((EntityPlayer) entityIn);
		}
		
		if(!worldIn.isRemote)
		{
			TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(worldIn, pos);
		
			if(teleporter != null && tplayer != null)
			{				
				if (!tplayer.teleported)
				{
					
					tplayer.teleported = true;
					tplayer.justTeleported = true;
					TeleporterNode dest = teleporter.teleport(entityIn);
					if(dest != null)
					{
							System.out.println("Teleported " + entityIn.getName() + " to " + dest.pos.getX() + "," + dest.pos.getY()  + "," +  dest.pos.getZ() );
					}
				}
			}
		}
		else
		{
			double width = 0.25;
			double height = 0.25;
			
			Random rand = new Random();
			
			if (!tplayer.teleported)
			{
				double mx = rand.nextGaussian() * 0.2d;
				double my = rand.nextGaussian() * 0.2d;
				double mz = rand.nextGaussian() * 0.2d;
				
				worldIn.spawnParticle(
						EnumParticleTypes.PORTAL,           
						pos.getX() + 0.5 + rand.nextFloat() * width * 2.0F - width, 
						pos.getY() + 1.5 + rand.nextFloat() * height, 
						pos.getZ() + 0.5 + rand.nextFloat() * width * 2.0F - width, 
						mx, my, mz);
			}
		}
		super.onEntityCollidedWithBlock(worldIn, pos, entityIn);
    }
		
	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbourBlock)
	{
		TileEntityTeleporter tele = TileEntityTeleporter.getTileEntityAt(world, pos);
		
		// check if teleporter was powered before
		boolean oldPowered = tele.isPowered;
		
		if (!world.isRemote && world.isBlockIndirectlyGettingPowered(pos) > 0 && tele != null)
		{
			tele.isPowered = true;
			// if block was not powered before but is now powered
			if(oldPowered == false)
			{
				// tell player who powered block that teleporter is exit-only
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16);
				if(player != null)
				{
					player.addChatMessage(new ChatComponentTranslation("Teleporter locked: can exit only."));
				}
			}
			tele.markDirty();
		}
		else if(tele != null)
		{
			tele.isPowered = false;
			// if block was powered before but is now not powered
			if(oldPowered == true)
			{
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16);
				if(player != null)
				{
					player.addChatMessage(new ChatComponentTranslation("Teleporter unlocked: can enter and exit."));
				}
			}
			tele.markDirty();
		}		
		
	}

	// This is where you can do something when the block is broken. In this case drop the inventory's contents
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		IInventory inventory = worldIn.getTileEntity(pos) instanceof IInventory ? (IInventory)worldIn.getTileEntity(pos) : null;

		if (inventory != null){
			// For each slot in the inventory
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				// If the slot is not empty
				if (inventory.getStackInSlot(i) != null)
				{
					// Create a new entity item with the item stack in the slot
					EntityItem item = new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, inventory.getStackInSlot(i));

					// Apply some random motion to the item
					float multiplier = 0.1f;
					float motionX = worldIn.rand.nextFloat() - 0.5f;
					float motionY = worldIn.rand.nextFloat() - 0.5f;
					float motionZ = worldIn.rand.nextFloat() - 0.5f;

					item.motionX = motionX * multiplier;
					item.motionY = motionY * multiplier;
					item.motionZ = motionZ * multiplier;

					// Spawn the item in the world
					worldIn.spawnEntityInWorld(item);
				}
			}

			TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(worldIn, pos);
			
			if(teleporter != null)
			{
				teleporter.removeFromNetwork();
			}
			
			// Clear the inventory so nothing else (such as another mod) can do anything with the items
			inventory.clear();
		}

		// Super MUST be called last because it removes the tile entity
		super.breakBlock(worldIn, pos, state);
	}

	//---------------------------------------------------------

	// the block will render in the SOLID layer.  See http://greyminecraftcoder.blogspot.co.at/2014/12/block-rendering-18.html for more information.
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	// set to false because this block doesn't fill the entire 1x1x1 space
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	// used by the renderer to control lighting and visibility of other blocks, also by
	// (eg) wall or fence to control whether the fence joins itself to this block
	// set to false because this block doesn't fill the entire 1x1x1 space
	@Override
	public boolean isFullCube() {
		return true;
	}

	// render using a BakedModel
	// not strictly required because the default (super method) is 3.
	@Override
	public int getRenderType() {
		return 3;
	}

}
