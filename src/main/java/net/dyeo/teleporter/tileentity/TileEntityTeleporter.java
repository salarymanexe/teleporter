package net.dyeo.teleporter.tileentity;

import net.dyeo.teleporter.block.BlockTeleporter;
import net.dyeo.teleporter.block.BlockTeleporter.EnumType;
import net.dyeo.teleporter.init.ModSounds;
import net.dyeo.teleporter.network.TeleporterNetwork;
import net.dyeo.teleporter.network.TeleporterNode;
import net.dyeo.teleporter.utilities.TeleporterUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityTeleporter extends TileEntity implements ITickable
{

	private String customName = null;
	private boolean firstUpdate = true;
	private boolean isPowered = false;

	private ItemStackHandler handler = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			TileEntityTeleporter.this.markDirty();
		}
	};



	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T)this.handler;
		return super.getCapability(capability, facing);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound = super.writeToNBT(compound);
		compound.setBoolean("powered", isPowered());
		compound.setTag("Inventory", handler.serializeNBT());
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		setPowered(compound.getBoolean("powered"));
		this.handler.deserializeNBT(compound.getCompoundTag("Inventory"));
	}



	public boolean isPowered()
	{
		return isPowered;
	}

	public void setPowered(boolean isPowered)
	{
		this.isPowered = isPowered;
	}

	public BlockTeleporter.EnumType getTypeProperty()
	{
		IBlockState state = getWorld().getBlockState(getPos());
		return state.getValue(BlockTeleporter.TYPE);
	}

	public boolean getInterdimensional()
	{
		return getTypeProperty() == EnumType.ENDER;
	}

	public String getName()
	{
		return this.hasCustomName() ? this.customName : "container.teleporter";
	}

	public boolean hasCustomName()
	{
		return this.customName != null && !this.customName.isEmpty();
	}

	public void setCustomName(String customNameIn)
	{
		this.customName = customNameIn;
	}

	public ITextComponent getDisplayName()
	{
		return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
	}



	public boolean canInteractWith(EntityPlayer player)
	{
		if (this.worldObj.getTileEntity(this.pos) != this) return false;
		final double X_CENTRE_OFFSET = 0.5;
		final double Y_CENTRE_OFFSET = 0.5;
		final double Z_CENTRE_OFFSET = 0.5;
		final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
		return player.getDistanceSq(pos.getX() + X_CENTRE_OFFSET, pos.getY() + Y_CENTRE_OFFSET,
				pos.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
	}

	@Override
	public BlockPos getPos()
	{
		return pos;
	}

	public static TileEntityTeleporter getTileEntityAt(World world, BlockPos pos)
	{
		TileEntityTeleporter teleTileEnt = null;
		TileEntity teleEnt = world.getTileEntity(pos);

		if (teleEnt != null)
		{
			if (teleEnt instanceof TileEntityTeleporter)
			{
				teleTileEnt = (TileEntityTeleporter) teleEnt;
			}
		}

		return teleTileEnt;
	}

	// call this when you want an entity to teleport to the next teleporter node
	public TeleporterNode teleport(Entity entityIn)
	{
		Vec3d bounds = BlockTeleporter.getBounds();

		TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, false);
		TeleporterNode source = netWrapper.getNode(pos, worldObj.provider.getDimension(), false);
		TeleporterNode destination = netWrapper.getNextNode(entityIn, this.handler.getStackInSlot(0), source);

		// teleport success variable
		boolean teleportSuccess = false;

		// if the destination and entity exist
		if (destination != null && entityIn != null)
		{
			// don't allow cross-dimensional teleportation if the entity is a
			// mount and the destination is another dimension
			if (source.dimension != destination.dimension && !entityIn.getPassengers().isEmpty())
			{
				return null;
			}

			// if the block type for this block is an instance of the basic
			// teleporter block
			if (getBlockType() instanceof BlockTeleporter)
			{
				double x = destination.pos.getX() + (bounds.xCoord * 0.5f);
				double y = destination.pos.getY() + (float)bounds.yCoord;
				double z = destination.pos.getZ() + (bounds.zCoord * 0.5f);

				float yaw = entityIn.rotationYaw;
				float pitch = entityIn.rotationPitch;

				if (getInterdimensional())
				{
					teleportSuccess = TeleporterUtility.transferToDimensionLocation(entityIn, destination.dimension, x, y, z, yaw, pitch);
					// don't allow if the entity is a mount
				}
				else
				{
					teleportSuccess = TeleporterUtility.transferToLocation(entityIn, x, y, z, yaw, pitch);
				}
			}
		}

		if (teleportSuccess)
		{
			worldObj.playSound(null, source.pos.getX(), source.pos.getY(), source.pos.getZ(), ModSounds.PORTAL_ENTER, SoundCategory.BLOCKS, 0.9f, 1.0f);
			worldObj.playSound(null, destination.pos.getX(), destination.pos.getY(), destination.pos.getZ(), ModSounds.PORTAL_EXIT, SoundCategory.BLOCKS, 0.9f, 1.0f);
			System.out.println("Teleport successful.");
			return destination;
		}
		else
		{
			worldObj.playSound(null, source.pos.getX(), source.pos.getY(), source.pos.getZ(), ModSounds.PORTAL_ERROR, SoundCategory.BLOCKS, 0.9f, 1.0f);
			System.out.println("Teleport unsuccessful.");
			return source;
		}

	}

	public void removeFromNetwork()
	{
		TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);
		netWrapper.removeNode(pos, worldObj.provider.getDimension());
	}

	@Override
	public void update()
	{

		if (firstUpdate)
		{
			if (!worldObj.isRemote)
			{
				boolean isNewNode = false;

				TeleporterNetwork netWrapper = TeleporterNetwork.get(worldObj, true);

				int tileDim = worldObj.provider.getDimension();
				TeleporterNode thisNode = netWrapper.getNode(this.pos, tileDim, true);
				if (thisNode == null)
				{
					thisNode = new TeleporterNode();
					isNewNode = true;
				}

				thisNode.pos = this.pos;
				thisNode.dimension = tileDim;

				if (isNewNode == true)
				{
					netWrapper.addNode(thisNode);
				}

				System.out.println("Node updated! (" + thisNode.pos.getX() + "," + thisNode.pos.getY() + ","
						+ thisNode.pos.getZ() + " @ dim " + thisNode.dimension + ")");
				markDirty();
			}
			firstUpdate = false;
		}

	}

}
