package net.dyeo.teleporter.client.renderer.tileentity;

import org.lwjgl.opengl.GL11;
import net.dyeo.teleporter.TeleporterMod;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class RenderTeleporter extends TileEntitySpecialRenderer
{
	private static final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(TeleporterMod.MODID, "models/block/teleporterBlock.obj"));

	private static final ResourceLocation[] texture = new ResourceLocation[]
	{
		new ResourceLocation(TeleporterMod.MODID, "textures/blocks/teleporterBlock.png"),
		new ResourceLocation(TeleporterMod.MODID, "textures/blocks/enderTeleporterBlock.png")
	};


	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double posX, double posY, double posZ, float timeSinceLastTick)
	{
		int meta = tileEntity.getWorldObj().getBlockMetadata(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
		this.bindTexture(RenderTeleporter.texture[meta]);

		GL11.glPushMatrix();
		GL11.glTranslated(posX + 0.5D, posY + 0.5D, posZ + 0.5D);
		GL11.glPushMatrix();

		RenderTeleporter.model.renderAll();

		GL11.glPopMatrix();
		GL11.glPopMatrix();
	}
}
