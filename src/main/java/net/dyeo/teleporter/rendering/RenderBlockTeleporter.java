package net.dyeo.teleporter.rendering;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class RenderBlockTeleporter extends TileEntitySpecialRenderer
{
	ResourceLocation texture;
	ResourceLocation objModelLocation;
	public IModelCustom model;

	public RenderBlockTeleporter(String texloc, String modloc)
	{
		this.texture = new ResourceLocation("teleporter".toLowerCase(), texloc);
		this.objModelLocation = new ResourceLocation("teleporter".toLowerCase(), modloc);
		this.model = AdvancedModelLoader.loadModel(this.objModelLocation);
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double posX, double posY, double posZ, float timeSinceLastTick)
	{
		this.bindTexture(this.texture);

		GL11.glPushMatrix();
		GL11.glTranslated(posX + 0.5D, posY + 0.5D, posZ + 0.5D);

		GL11.glPushMatrix();
		this.model.renderAll();
		GL11.glPopMatrix();

		GL11.glPopMatrix();
	}
}
