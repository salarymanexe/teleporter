package net.dyeo.teleporter.recipes;

import com.google.gson.JsonObject;
import net.dyeo.teleporter.common.config.ModConfiguration;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraft.util.JsonUtils;

import java.util.function.BooleanSupplier;

public class SimpleRecipesConditionFactory implements IConditionFactory
{
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        boolean value = JsonUtils.getBoolean(json,"value", true);
        return () -> value == ModConfiguration.useDiamonds;
    }
}
