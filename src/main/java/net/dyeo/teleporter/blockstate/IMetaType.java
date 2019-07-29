package net.dyeo.teleporter.blockstate;

import net.minecraft.util.IStringSerializable;

public interface IMetaType extends IStringSerializable
{
    String getName();
    int getMetadata();
    String getUnlocalizedName();
    String getRegistryName();
}
