package net.dyeo.teleporter.blocks;

import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.dyeo.teleporter.Reference;
import net.dyeo.teleporter.Teleporter;
import net.dyeo.teleporter.entities.TeleporterEntity;
import net.dyeo.teleporter.entities.TileEntityTeleporter;
import net.dyeo.teleporter.gui.GuiHandlerTeleporter;
import net.dyeo.teleporter.network.TeleporterNode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BlockTeleporterBase extends BlockContainer
{
	private boolean interdimensional = false;

	public BlockTeleporterBase(boolean isInterdimensional)
	{
		super(Material.rock);

		this.setCreativeTab(CreativeTabs.tabTransport);

		this.setResistance(30.0F);

		this.setHardness(3.0F);

		this.setLightLevel(0.5F);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, (float)getBounds().xCoord, (float)getBounds().yCoord, (float)getBounds().zCoord);

		this.setInterdimensional(isInterdimensional);
	}

	public ChatComponentTranslation GetMessage(String messageName)
	{
		return new ChatComponentTranslation("message." + "teleporter".toLowerCase() + '_' + this.getClass().getSimpleName() + '.' + messageName, new Object[0]);
	}

	public String getBlockName()
	{
		return null;
	}

	public void setInterdimensional(boolean val)
	{
		this.interdimensional = val;
	}

	public boolean getInterdimensional()
	{
		return this.interdimensional;
	}

	public static Vec3 getBounds()
	{
		return Vec3.createVectorHelper(1.0D, 0.9375D, 1.0D);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		TileEntityTeleporter result = new TileEntityTeleporter();
		return result;
	}

	@Override
	public boolean onBlockActivated(World worldIn, int posx, int posy, int posz, EntityPlayer playerIn, int side, float hitX, float hitY, float hitZ)
	{
		if ((!worldIn.isRemote) && (!playerIn.isSneaking()))
		{
			playerIn.openGui(Teleporter.instance, GuiHandlerTeleporter.getGuiID(), worldIn, posx, posy, posz);
		}
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, int posx, int posy, int posz, Entity entityIn)
	{
		TeleporterEntity tentity = null;
		tentity = TeleporterEntity.get(entityIn);
		if (tentity == null)
		{
			TeleporterEntity.register(entityIn);
			tentity = TeleporterEntity.get(entityIn);
			System.out.println("New Teleporter Entity");
		}
		if (!worldIn.isRemote)
		{
			TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(worldIn, posx, posy, posz);
			if ((teleporter != null) && (tentity != null))
			{
				if ((tentity.getOnTeleporter()) && (!tentity.getTeleported()))
				{
					boolean isHostile = ((entityIn instanceof EntityMob)) || (((entityIn instanceof EntityWolf)) && (((EntityWolf)entityIn).isAngry()));

					boolean isPassive = entityIn instanceof EntityAnimal;
					if (((!isHostile) || (isHostile == Reference.teleportHostileMobs)) && ((!isPassive) || (isPassive == Reference.teleportPassiveMobs)))
					{
						tentity.setTeleported(true);

						TeleporterNode destinationNode = teleporter.teleport(entityIn);
						if (destinationNode != null)
						{
							System.out.println("Teleported entity to " + destinationNode.posx + "," + destinationNode.posy + "," + destinationNode.posz + " : " + destinationNode.dimension);
						}
					}
				}
			}
		}
		else
		{
			double width = 0.25D;
			double height = 0.25D;

			Random rand = new Random();
			if (!tentity.getTeleported())
			{
				double mx = rand.nextGaussian() * 0.2D;
				double my = rand.nextGaussian() * 0.2D;
				double mz = rand.nextGaussian() * 0.2D;

				worldIn.spawnParticle("portal", posx + 0.5D + rand

						.nextFloat() * width * 2.0D - width, posy + 1.5D + rand.nextFloat() * height, posz + 0.5D + rand.nextFloat() * width * 2.0D - width, mx, my, mz);
			}
		}
		super.onEntityCollidedWithBlock(worldIn, posx, posy, posz, entityIn);
	}

	@Override
	public void onNeighborBlockChange(World world, int posx, int posy, int posz, Block neighbourBlock)
	{
		TileEntityTeleporter tileEntityTeleporter = TileEntityTeleporter.getTileEntityAt(world, posx, posy, posz);

		boolean oldPowered = tileEntityTeleporter.isPowered();
		if ((!world.isRemote) && (world.isBlockIndirectlyGettingPowered(posx, posy, posz)) && (tileEntityTeleporter != null))
		{
			tileEntityTeleporter.setPowered(true);
			if (!oldPowered)
			{
				EntityPlayer player = world.getClosestPlayer(posx, posy, posz, 16.0D);
				if (player != null)
				{
					player.addChatMessage(this.GetMessage("teleporterLocked"));
				}
			}
			tileEntityTeleporter.markDirty();
		}
		else if (tileEntityTeleporter != null)
		{
			tileEntityTeleporter.setPowered(false);
			if (oldPowered == true)
			{
				EntityPlayer player = world.getClosestPlayer(posx, posy, posz, 16.0D);
				if (player != null)
				{
					player.addChatMessage(this.GetMessage("teleporterUnlocked"));
				}
			}
			tileEntityTeleporter.markDirty();
		}
	}

	@Override
	public void breakBlock(World worldIn, int posx, int posy, int posz, Block block, int state)
	{
		IInventory inventory = (worldIn.getTileEntity(posx, posy, posz) instanceof IInventory) ? (IInventory)worldIn.getTileEntity(posx, posy, posz) : null;
		if (inventory != null)
		{
			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				if (inventory.getStackInSlot(i) != null)
				{
					EntityItem item = new EntityItem(worldIn, posx + 0.5D, posy + 0.5D, posz + 0.5D, inventory.getStackInSlot(i));

					float multiplier = 0.1F;
					float motionX = worldIn.rand.nextFloat() - 0.5F;
					float motionY = worldIn.rand.nextFloat() - 0.5F;
					float motionZ = worldIn.rand.nextFloat() - 0.5F;

					item.motionX = (motionX * multiplier);
					item.motionY = (motionY * multiplier);
					item.motionZ = (motionZ * multiplier);

					worldIn.spawnEntityInWorld(item);
				}
			}
			TileEntityTeleporter tileEntityTeleporter = TileEntityTeleporter.getTileEntityAt(worldIn, posx, posy, posz);
			if (tileEntityTeleporter != null)
			{
				tileEntityTeleporter.removeFromNetwork();
			}
			((TileEntityTeleporter)inventory).clear();
		}
		super.breakBlock(worldIn, posx, posy, posz, block, state);
	}

	@SideOnly(Side.CLIENT)
	public int getBlockLayer()
	{
		return 2;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean isFullCube()
	{
		return true;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return 0; //return 3;
	}
}
