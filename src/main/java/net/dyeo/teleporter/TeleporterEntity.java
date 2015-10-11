package net.dyeo.teleporter;

import java.util.BitSet;

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
	
	
	public void copy(TeleporterEntity old) 
	{
		dimension = old.dimension;
		teleported = old.teleported;
		onTeleporter = old.onTeleporter;
	}
	
	//
	
	@Override
	public void saveNBTData(NBTTagCompound compound) 
	{
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) 
	{
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
