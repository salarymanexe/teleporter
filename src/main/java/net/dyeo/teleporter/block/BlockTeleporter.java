package net.dyeo.teleporter.block;

import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.dyeo.teleporter.common.network.GuiHandler;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty;
import net.dyeo.teleporter.entityproperties.TeleportEntityProperty.EnumTeleportStatus;
import net.dyeo.teleporter.inventory.InventoryHelper;
import net.dyeo.teleporter.teleport.TeleporterNode;
import net.dyeo.teleporter.teleport.TeleporterUtility;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.dyeo.teleporter.util.Vec3i;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class BlockTeleporter extends BlockContainer
{
	public static final AxisAlignedBB TELEPORTER_AABB = AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);
	private boolean interdimensional = false;

	public BlockTeleporter()
	{
		super(Material.rock);
		this.setCreativeTab(CreativeTabs.tabTransport);
		this.setResistance(30.0F);
		this.setHardness(3.0F);
		this.setLightLevel(0.5F);
		this.setBlockBounds(0.0f, 0.0f, 0.0f, (float)TELEPORTER_AABB.maxX, (float)TELEPORTER_AABB.maxY, (float)TELEPORTER_AABB.maxZ);
	}

	@Override
	public boolean onBlockActivated(World world, int posX, int posY, int posZ, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ((!world.isRemote) && (!player.isSneaking()))
		{
			player.openGui(TeleporterMod.instance, GuiHandler.GUI_ID_TELEPORTER, world, posX, posY, posZ);
		}
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int posX, int posY, int posZ, Entity entity)
	{
		if (entity instanceof EntityLivingBase)
		{
			TeleportEntityProperty handler = TeleportEntityProperty.get(entity);
			if (TeleportEntityProperty.get(entity) != null)
			{
				if (!world.isRemote)
				{
					if (handler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
					{
						boolean onTeleporter = new Vec3i(entity).distanceSq(posX, posY, posZ) == 0;
						int dimension = entity.dimension;

						handler.setOnTeleporter(onTeleporter);
						handler.setDimension(dimension);

						if (handler.getOnTeleporter())
						{
							boolean isHostile = ((entity instanceof EntityMob)) || (((entity instanceof EntityWolf)) && (((EntityWolf)entity).isAngry()));
							boolean isPassive = entity instanceof EntityAnimal;

							if ((isHostile == false || isHostile == ModConfiguration.teleportHostileMobs) && (isPassive == false || isPassive == ModConfiguration.teleportPassiveMobs))
							{
								TeleporterNode destinationNode = TeleporterUtility.teleport((EntityLivingBase)entity, posX, posY, posZ);
							}
						}
					}
				}

				if (handler.getTeleportStatus() == EnumTeleportStatus.INACTIVE)
				{
					double width = 0.25D;
					double height = 0.25D;

					double mx = world.rand.nextGaussian() * 0.2d;
					double my = world.rand.nextGaussian() * 0.2d;
					double mz = world.rand.nextGaussian() * 0.2d;

					world.spawnParticle("portal",
						posX + 0.5D + world.rand.nextFloat() * width * 2.0D - width,
						posY + 1.5D + world.rand.nextFloat() * height,
						posZ + 0.5D + world.rand.nextFloat() * width * 2.0D - width, mx, my, mz
					);
				}
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int posX, int posY, int posZ, Block neighbourBlock)
	{
		if (!world.isRemote)
		{
			TileEntityTeleporter tileEntity = (TileEntityTeleporter)world.getTileEntity(posX, posY, posZ);
			if (tileEntity != null)
			{
				boolean isNowPowered = world.isBlockIndirectlyGettingPowered(posX, posY, posZ);
				boolean isAlreadyPowered = tileEntity.isPowered();

				tileEntity.setPowered(isNowPowered);

				if (isNowPowered != isAlreadyPowered)
				{
					// there is no way in forge to determine who activated/deactivated the teleporter, so we simply get the closest player
					// works for _most_ cases
					EntityPlayer player = world.getClosestPlayer(posX, posY, posZ, 16.0D);
					if (player != null)
					{
						String translationKey = "message." + TeleporterMod.MODID + '_' + this.getClass().getSimpleName() + '.' + (isNowPowered ? "teleporterLocked" : "teleporterUnlocked");
						ChatComponentTranslation message = new ChatComponentTranslation(translationKey);
						player.addChatMessage(message);
					}
					tileEntity.markDirty();
				}

			}
		}
	}

	@Override
	public void breakBlock(World world, int posX, int posY, int posZ, Block block, int state)
	{
		TileEntityTeleporter tileEntity = (TileEntityTeleporter)world.getTileEntity(posX, posY, posZ);
		if (tileEntity != null)
		{
			tileEntity.removeFromNetwork();

			for (int i = 0; i < tileEntity.getSizeInventory(); i++)
			{
				ItemStack stack = tileEntity.getStackInSlot(0);
				if (stack != null)
				{
					stack = stack.copy();
					InventoryHelper.spawnItemStack(world, posX, posY, posZ, stack);
				}
			}
		}

		// super _must_ be called last because it removes the tile entity
		super.breakBlock(world, posX, posY, posZ, block, state);
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		TileEntityTeleporter result = new TileEntityTeleporter();
		return result;
	}

	@Override
	public int damageDropped(int meta)
	{
		return meta;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for ( EnumType type : EnumType.values() )
		{
			list.add(new ItemStack(item, 1, type.getMetadata()));
		}
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
		return -1;
	}



	public enum EnumType
	{
		REGULAR(0, "teleporterBlock"),
		ENDER(1, "enderTeleporterBlock");

		private final int meta;
		private final String unlocalizedName;

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];

		public int getMetadata()
		{
			return this.meta;
		}

		public String getUnlocalizedName()
		{
			return this.unlocalizedName;
		}

		public static EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		private EnumType(int meta, String unlocalizedName)
		{
			this.meta = meta;
			this.unlocalizedName = unlocalizedName;
		}

		static
		{
			for (EnumType value : values())
			{
				META_LOOKUP[value.getMetadata()] = value;
			}
		}

	}

}
