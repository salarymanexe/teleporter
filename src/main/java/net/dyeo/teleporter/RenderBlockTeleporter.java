package net.dyeo.teleporter;

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

	public RenderBlockTeleporter()
	{
		this.texture = new ResourceLocation("teleporter", "textures/models/teleporterBlock.png");
		this.objModelLocation = new ResourceLocation("teleporter", "models/teleporterBlock.obj");
		this.model = AdvancedModelLoader.loadModel(this.objModelLocation);
	}

	public void renderTileEntityAt(TileEntity te, double posX, double posY, double posZ, float timeSinceLastTick)
	{
		TileEntityTeleporter te2 = (TileEntityTeleporter)te;
		bindTexture(this.texture);

		GL11.glPushMatrix();
		GL11.glTranslated(posX + 0.5D, posY + 0.5D, posZ + 0.5D);

		GL11.glPushMatrix();
		this.model.renderAll();
		GL11.glPopMatrix();

		GL11.glPopMatrix();
	}
}
