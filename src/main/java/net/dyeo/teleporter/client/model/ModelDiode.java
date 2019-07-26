package net.dyeo.teleporter.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDiode extends ModelBase
{
    private ModelRenderer diode = (new ModelRenderer(this)).addBox(0.0f, 0.0f, 0.0f, 16, 8, 16);

    /**
     * Renders the model.
     */
    public void render(float scale)
    {
        diode.render(scale);
    }
}
