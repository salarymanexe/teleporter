package net.dyeo.teleporter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;



public class TeleporterEntity implements IExtendedEntityProperties 
{

	public final static String EXT_PROP_NAME = "TeleporterEntity";
	
	private final Entity entity;
	
	boolean teleported, onTeleporter;
	int dimension;
	
	public TeleporterEntity(Entity entity)
	{
		this.entity = entity;
		
		teleported = false;
		onTeleporter = false;
	}
	
	//
	
	public static final void register(Entity entity)
	{
		entity.registerExtendedProperties(TeleporterEntity.EXT_PROP_NAME, new TeleporterEntity(entity));
	}
	
	public static final TeleporterEntity get(Entity entity)
	{
		return (TeleporterEntity) entity.getExtendedProperties(EXT_PROP_NAME);
	}
	
	//
	
	@Override
	public void saveNBTData(NBTTagCompound compound) 
	{
		// TODO Auto-generated method stub
		compound.setByte("dimension", (byte) dimension);
		compound.setBoolean("teleported", teleported);
		compound.setBoolean("onTeleporter", onTeleporter);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) 
	{
		// TODO Auto-generated method stub
		onTeleporter = compound.getBoolean("onTeleporter");
		teleported = compound.getBoolean("teleported");
		dimension = compound.getByte("dimension");
	}

	@Override
	public void init(Entity entity, World world) 
	{
		// TODO Auto-generated method stub
	}

	public void checkLocation() 
	{
		if(!entity.worldObj.isRemote)
		{
			if(dimension != entity.dimension)
			{
				this.onTeleporter = false;
				this.teleported = false;
			} 
			dimension = entity.dimension;
			
			TeleporterNetwork netWrapper = TeleporterNetwork.get(entity.worldObj, false);
		
			BlockPos ppos = new BlockPos(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY-(BlockTeleporter.getBounds().yCoord)), MathHelper.floor_double(entity.posZ));
		
			TeleporterNode node = netWrapper.getNode(new BlockPos(ppos), entity.worldObj.provider.getDimensionId(), false);
			
			if(node != null)
			{
				//System.out.println("[Teleporter] Player on block " + ppos.getX() + "," + ppos.getY() + "," + ppos.getZ());
				this.onTeleporter = true;
			}
			else
			{
				this.onTeleporter = false;
				this.teleported = false;
			}			
		}
	}
}
