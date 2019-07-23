package net.dyeo.teleporter.renderer;

import net.dyeo.teleporter.client.model.ModelDiode;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileEntityTeleporterRenderer extends TileEntitySpecialRenderer<TileEntityTeleporter>
{
    private static final ResourceLocation[] TEXTURE_DIODE = new ResourceLocation[]
        {
            new ResourceLocation("textures/blocks/redstone_block.png"),
            new ResourceLocation("textures/blocks/glowstone.png")
        };
    private final ModelDiode modelDiode = new ModelDiode();

    @Override
    public void render(TileEntityTeleporter te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        GlStateManager.pushMatrix();

        float offset = !te.isPowered() ? 0.5f : 0.26f;
        GlStateManager.translate((float)x + 0.25f, (float)y + offset, (float)z + 0.25f);

        this.bindTexture(TEXTURE_DIODE[te.getBlockMetadata()]);

        GlStateManager.enableCull();
        modelDiode.render(0.0625f);

        GlStateManager.popMatrix();
    }
}
