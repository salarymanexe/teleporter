package net.dyeo.teleporter.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDiode extends ModelBase
{
    private ModelRenderer diode =
            (new ModelRenderer(this))
                    .setTextureOffset(4,4)
                    .setTextureSize(16,16)
                    .addBox(0.0f, 0.0f, 0.0f, 8, 8, 8);

    /**
     * Renders the model.
     */
    public void render(float scale)
    {
        diode.render(scale);
    }
}
