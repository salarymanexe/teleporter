package net.dyeo.teleporter.rendering;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class RenderItemTeleporter implements IItemRenderer
{
	ResourceLocation texture;
	ResourceLocation objModelLocation;
	public IModelCustom model;
	private final Minecraft mc;

	public RenderItemTeleporter()
	{
		this.texture = new ResourceLocation("teleporter".toLowerCase(), "textures/models/teleporterBlock.png");
		this.objModelLocation = new ResourceLocation("teleporter".toLowerCase(), "models/teleporterBlock.obj");
		this.model = AdvancedModelLoader.loadModel(this.objModelLocation);
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPushMatrix();

		GL11.glEnable(2884);

		this.mc.renderEngine.bindTexture(this.texture);
		if ((type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) || (type == IItemRenderer.ItemRenderType.EQUIPPED))
		{
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
		this.model.renderAll();
		GL11.glDisable(2884);
		GL11.glPopMatrix();
	}
}
