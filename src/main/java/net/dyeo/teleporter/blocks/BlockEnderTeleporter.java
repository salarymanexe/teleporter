package net.dyeo.teleporter.blocks;

import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Standard Teleporter Block Class
// Has a 1-slot container for teleporter key
public class BlockEnderTeleporter extends BlockTeleporterBase
{
	
	// Constructor
	public BlockEnderTeleporter()
	{
		super(true);
	}
	
	@Override
	public String getBlockName()
	{
		return "enderTeleporterBlock";		
	}
	
	public static Vec3 getBounds()
	{
		return BlockTeleporterBase.getBounds();
	}

	// called when the block is placed or loaded client side to get the tile entity for the block
	// should return a new instance of the tile entity for the block
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		TileEntityTeleporter result = new TileEntityTeleporter();
		return result;
	}

	// called when the block is right clicked
	// in this case it's used to open the teleporter key gui when right-clicked by a player
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) 
	{
		return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollidedWithBlock(worldIn, pos, entityIn);
    }
		
	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbourBlock)
	{
		super.onNeighborBlockChange(world, pos, state, neighbourBlock);
	}

	// drop inventory contents when block is broken
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) 
	{
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
