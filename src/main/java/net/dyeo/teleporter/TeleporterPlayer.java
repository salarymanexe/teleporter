package net.dyeo.teleporter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class TeleporterPlayer implements IExtendedEntityProperties 
{

	public final static String EXT_PROP_NAME = "TeleporterPlayer";
	
	private final EntityPlayer player;
	
	boolean teleported, onTeleporter, justTeleported;
	
	public TeleporterPlayer(EntityPlayer player)
	{
		this.player = player;
		
		teleported = false;
		onTeleporter = false;
		justTeleported = false;
	}
	
	//
	
	public static final void register(EntityPlayer player)
	{
		player.registerExtendedProperties(TeleporterPlayer.EXT_PROP_NAME, new TeleporterPlayer(player));
	}
	
	public static final TeleporterPlayer get(EntityPlayer player)
	{
		return (TeleporterPlayer) player.getExtendedProperties(EXT_PROP_NAME);
	}
	
	//
	
	@Override
	public void saveNBTData(NBTTagCompound compound) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void init(Entity entity, World world) 
	{
		// TODO Auto-generated method stub
	}

	public void checkLocation() 
	{
		if(!player.worldObj.isRemote)
		{
			TeleporterNetwork netWrapper = TeleporterNetwork.get(player.worldObj, false);
		
			BlockPos ppos = new BlockPos(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY-1), MathHelper.floor_double(player.posZ));
		
			TeleporterNode node = netWrapper.getNode(new BlockPos(ppos), player.worldObj.provider.getDimensionId(), false);
					
			//System.out.println("[Teleporter] Player on block " + ppos.getX() + "," + ppos.getY() + "," + ppos.getZ());
			
			if(node != null)
			{
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
