package net.dyeo.teleporter;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Standard Teleporter Block Class
// Has a 1-slot container for teleporter key
public class BlockTeleporter extends BlockContainer
{
	
	// Constructor
	public BlockTeleporter()
	{
		// Set material to rock
		super(Material.rock);
		// Set transport tab for creative
		this.setCreativeTab(CreativeTabs.tabTransport);
		// resistance: 30
		this.setResistance(30);
		// hardness: 3.0
		this.setHardness(3.0f);
		// light level (emission): 0.5f
		this.setLightLevel(0.5f);
		this.setBlockBounds(0.0f, 0.0f, 0.0f, (float)getBounds().xCoord, (float)getBounds().yCoord, (float)getBounds().zCoord);
	}
	
	public static Vec3 getBounds()
	{
		return new Vec3(1.0f, 0.9375f, 1.0f);
	}

	// called when the block is placed or loaded client side to get the tile entity for the block
	// should return a new instance of the tile entity for the block
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileEntityTeleporter();
	}

	// called when the block is right clicked
	// in this case it's used to open the teleporter key gui when right-clicked by a player
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		// Uses the gui handler registered to open the gui for the given gui id
		// open on the server side only
		if (!worldIn.isRemote && !playerIn.isSneaking()) 
		{
			playerIn.openGui(Teleporter.instance, GuiHandlerTeleporter.getGuiID(), worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return true;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn)
	{
		TeleporterEntity tentity = null;
		tentity = TeleporterEntity.get(entityIn);
		if(tentity == null)
		{
			TeleporterEntity.register(entityIn);
			tentity = TeleporterEntity.get(entityIn);
			System.out.println("New Teleporter Entity: " + entityIn.getName());
		}
		
		if(!worldIn.isRemote)
		{
			TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(worldIn, pos);
		
			if(teleporter != null && tentity != null)
			{				
				if (tentity.onTeleporter && !tentity.teleported)
				{
					boolean isHostile = (entityIn instanceof EntityMob) || (entityIn instanceof EntityWolf && ((EntityWolf) entityIn).isAngry());
					
					boolean isPassive = (entityIn instanceof EntityAnimal);
					
					if((isHostile == false || isHostile == Reference.teleportHostileMobs) && (isPassive == false || isPassive == Reference.teleportPassiveMobs))
					{					
						tentity.teleported = true;
						TeleporterNode dest = teleporter.teleport(entityIn);
						if(dest != null)
						{
							System.out.println("Teleported " + entityIn.getName() + " to " + dest.pos.getX() + "," + dest.pos.getY()  + "," +  dest.pos.getZ() );
						}
					}
				}
			}
		}
		else
		{
			double width = 0.25;
			double height = 0.25;
			
			Random rand = new Random();
			
			if (!tentity.teleported)
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
		// get the tile entity at this block's world pos
		TileEntityTeleporter tileEntityTeleporter = TileEntityTeleporter.getTileEntityAt(world, pos);
		
		// check if teleporter was powered before
		boolean oldPowered = tileEntityTeleporter.isPowered;
		
		if (!world.isRemote && world.isBlockIndirectlyGettingPowered(pos) > 0 && tileEntityTeleporter != null)
		{
			tileEntityTeleporter.isPowered = true;
			// if block was not powered before but is now powered
			if(oldPowered == false)
			{
				// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
				// works for _most_ cases
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16);
				if(player != null)
				{
					player.addChatMessage(new ChatComponentTranslation("Teleporter locked: can exit only."));
				}
			}
			tileEntityTeleporter.markDirty();
		}
		else if(tileEntityTeleporter != null)
		{
			tileEntityTeleporter.isPowered = false;
			// if block was powered before but is now not powered
			if(oldPowered == true)
			{
				// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
				// works for _most_ cases
				EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16);
				if(player != null)
				{
					player.addChatMessage(new ChatComponentTranslation("Teleporter unlocked: can enter and exit."));
				}
			}
			tileEntityTeleporter.markDirty();
		}
	}

	// drop inventory contents when block is broken
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) 
	{

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

			TileEntityTeleporter tileEntityTeleporter = TileEntityTeleporter.getTileEntityAt(worldIn, pos);
			
			if(tileEntityTeleporter != null)
			{
				tileEntityTeleporter.removeFromNetwork();
			}
			
			// clear the inventory so nothing else (such as another mod) can do anything with the items
			inventory.clear();
		}

		// super _must_ be called last because it removes the tile entity
		super.breakBlock(worldIn, pos, state);
	}

	//---------------------------------------------------------

	// the block will render in the CUTOUT layer (allows transparency of glass and such)
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}

	// used by the renderer to control lighting and visibility of other blocks, also by
	// (eg) wall or fence to control whether the fence joins itself to this block
	@Override
	public boolean isFullCube() 
	{
		return true;
	}

	// render using a BakedModel
	// not strictly required because the default (super method) is 3.
	@Override
	public int getRenderType() 
	{
		return 3;
	}

}
