package net.dyeo.teleporter;

import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BlockTeleporter extends BlockContainer
{
	@SideOnly(Side.CLIENT)
	private IIcon iconFront;
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public BlockTeleporter()
	{
		super(Material.rock);
		setCreativeTab(CreativeTabs.tabTransport);
		setResistance(30.0F);
		setHardness(3.0F);
		setLightLevel(0.5F);
		setBlockBounds(0.0F, 0.0F, 0.0F, (float)getBounds().xCoord, (float)getBounds().yCoord, (float)getBounds().zCoord);
	}

	public static Vec3 getBounds()
	{
		return Vec3.createVectorHelper(1.0D, 0.9375D, 1.0D);
	}

	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityTeleporter();
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon("teleporter".toLowerCase() + ":teleporterBlock");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata)
	{
		return this.blockIcon;
	}

	public boolean onBlockActivated(World worldIn, int posx, int posy, int posz, EntityPlayer playerIn, int side, float hitX, float hitY, float hitZ)
	{
		if ((!worldIn.isRemote) && (!playerIn.isSneaking()))
		{
			playerIn.openGui(Teleporter.instance, GuiHandlerTeleporter.getGuiID(), worldIn, posx, posy, posz);
		}
		return true;
	}

	public void onEntityCollidedWithBlock(World worldIn, int posx, int posy, int posz, Entity entityIn)
	{
		if ((entityIn instanceof EntityLivingBase))
		{
			TeleporterEntity tentity = null;
			tentity = TeleporterEntity.get(entityIn);
			if (tentity == null)
			{
				TeleporterEntity.register(entityIn);
				tentity = TeleporterEntity.get(entityIn);
				System.out.println("New Teleporter Entity: " + entityIn.getCommandSenderName());
			}
			if (!worldIn.isRemote)
			{
				TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(worldIn, posx, posy, posz);
				if ((teleporter != null) && (tentity != null))
				{
					if ((tentity.onTeleporter) && (!tentity.teleported))
					{
						boolean isHostile = ((entityIn instanceof EntityMob)) || (((entityIn instanceof EntityWolf)) && (((EntityWolf)entityIn).isAngry()));

						boolean isPassive = entityIn instanceof EntityAnimal;
						if (((!isHostile) || (isHostile == Reference.teleportHostileMobs)) && ((!isPassive) || (isPassive == Reference.teleportPassiveMobs)))
						{
							tentity.teleported = true;
							TeleporterNode dest = teleporter.teleport(entityIn);
							if (dest != null)
							{
								System.out.println("Teleported " + entityIn.getCommandSenderName() + " to " + dest.posx + "," + dest.posy + "," + dest.posz);
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
				if (!tentity.teleported)
				{
					double mx = rand.nextGaussian() * 0.2D;
					double my = rand.nextGaussian() * 0.2D;
					double mz = rand.nextGaussian() * 0.2D;

					worldIn.spawnParticle("portal", posx + 0.5D + rand.nextFloat() * width * 2.0D - width, posy + 1.5D + rand.nextFloat() * height, posz + 0.5D + rand.nextFloat() * width * 2.0D - width, mx, my, mz);
				}
			}
		}
		super.onEntityCollidedWithBlock(worldIn, posx, posy, posz, entityIn);
	}

	public void onNeighborBlockChange(World world, int posx, int posy, int posz, Block neighbourBlock)
	{
		TileEntityTeleporter tele = TileEntityTeleporter.getTileEntityAt(world, posx, posy, posz);

		boolean oldPowered = tele.isPowered;
		if ((!world.isRemote) && (world.isBlockIndirectlyGettingPowered(posx, posy, posz)) && (tele != null))
		{
			tele.isPowered = true;
			if (!oldPowered)
			{
				EntityPlayer player = world.getClosestPlayer(posx, posy, posz, 16.0D);
				if (player != null)
				{
					player.addChatMessage(new ChatComponentTranslation("Teleporter locked: can exit only.", new Object[0]));
				}
			}
			tele.markDirty();
		}
		else if (tele != null)
		{
			tele.isPowered = false;
			if (oldPowered == true)
			{
				EntityPlayer player = world.getClosestPlayer(posx, posy, posz, 16.0D);
				if (player != null)
				{
					player.addChatMessage(new ChatComponentTranslation("Teleporter unlocked: can enter and exit.", new Object[0]));
				}
			}
			tele.markDirty();
		}
	}

	public void breakBlock(World worldIn, int posx, int posy, int posz, Block blockIn, int something)
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
			TileEntityTeleporter teleporter = TileEntityTeleporter.getTileEntityAt(worldIn, posx, posy, posz);
			if (teleporter != null)
			{
				teleporter.removeFromNetwork();
			}
			((TileEntityTeleporter)inventory).clear();
		}
		super.breakBlock(worldIn, posx, posy, posz, blockIn, something);
	}

	@SideOnly(Side.CLIENT)
	public int getBlockLayer()
	{
		return 2;
	}

	public boolean isOpaqueCube()
	{
		return false;
	}

	public boolean isFullCube()
	{
		return true;
	}

	public boolean renderAsNormalBlock()
	{
		return false;
	}

	public int getRenderType()
	{
		return -1;
	}
}
