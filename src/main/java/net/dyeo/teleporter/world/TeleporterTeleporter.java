package net.dyeo.teleporter.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class TeleporterTeleporter implements ITeleporter
{
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public TeleporterTeleporter(double x, double y, double z, float yaw, float pitch)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw)
    {
        world.getBlockState(new BlockPos(x,y,z));
        entity.setPosition(x,y,z);
        entity.rotationYaw = this.yaw;
        entity.rotationPitch = this.pitch;
        entity.motionX = 0.0f;
        entity.motionY = 0.0f;
        entity.motionZ = 0.0f;
    }

    @Override
    public boolean isVanilla()
    {
        return false;
    }
}
