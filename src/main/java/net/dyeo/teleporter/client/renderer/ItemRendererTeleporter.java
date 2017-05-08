package net.dyeo.teleporter.client.renderer;

import org.lwjgl.opengl.GL11;
import net.dyeo.teleporter.TeleporterMod;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class ItemRendererTeleporter implements IItemRenderer
{

	private static final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(TeleporterMod.MODID, "models/block/teleporterBlock.obj"));

	private static final ResourceLocation[] texture = new ResourceLocation[]
	{
		new ResourceLocation(TeleporterMod.MODID, "textures/blocks/teleporterBlock.png"),
		new ResourceLocation(TeleporterMod.MODID, "textures/blocks/enderTeleporterBlock.png")
	};

	private final Minecraft mc = Minecraft.getMinecraft();

	public ItemRendererTeleporter()
	{
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
		GL11.glEnable(GL11.GL_CULL_FACE); // GL11.glEnable(2884);

		this.mc.renderEngine.bindTexture(ItemRendererTeleporter.texture[item.getItemDamage()]);

		if ((type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON) || (type == IItemRenderer.ItemRenderType.EQUIPPED))
		{
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
		ItemRendererTeleporter.model.renderAll();

		GL11.glDisable(GL11.GL_CULL_FACE); // GL11.glDisable(2884);
		GL11.glPopMatrix();
	}
}
