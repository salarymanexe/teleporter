package net.dyeo.teleporter.entities;

import net.dyeo.teleporter.blocks.BlockTeleporter;
import net.dyeo.teleporter.network.TeleporterNetwork;
import net.dyeo.teleporter.network.TeleporterNode;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;


// entity extension for objects that can use the teleporter
// applies to every living entity through the EventTeleporter class
public class TeleporterEntity implements IExtendedEntityProperties 
{
	
	//
	public final static String EXT_PROP_NAME = "TeleporterEntity";
	
	private final Entity entity;
	
	private boolean teleported;

	private boolean onTeleporter;
	int dimension;
	
	public boolean getTeleported() {
		return teleported;
	}

	public void setTeleported(boolean teleported) {
		this.teleported = teleported;
	}

	public boolean getOnTeleporter() {
		return onTeleporter;
	}

	public void setOnTeleporter(boolean onTeleporter) {
		this.onTeleporter = onTeleporter;
	}

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
		setTeleported(old.getTeleported());
		setOnTeleporter(old.getOnTeleporter());
	}
	
	//
	
	@Override
	public void saveNBTData(NBTTagCompound compound) 
	{
		compound.setInteger("dimension", dimension);
		compound.setBoolean("teleported", getTeleported());
		compound.setBoolean("onTeleporter", getOnTeleporter());
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) 
	{
		dimension    = compound.getInteger("dimension");
		setTeleported(compound.getBoolean("teleported"));
		setOnTeleporter(compound.getBoolean("onTeleporter"));
	}

	@Override
	public void init(Entity entity, World world) 
	{	
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
				this.setOnTeleporter(true);
			}
			else
			{
				this.setOnTeleporter(false);
				this.setTeleported(false);
			}
		}
	}
	
}
