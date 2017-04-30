package net.dyeo.teleporter.client.gui.inventory;

import java.awt.Color;
import net.dyeo.teleporter.TeleporterMod;
import net.dyeo.teleporter.inventory.ContainerTeleporter;
import net.dyeo.teleporter.tileentity.TileEntityTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class GuiTeleporter extends GuiContainer
{

	private static final ResourceLocation TELEPORTER_GUI_TEXTURE = new ResourceLocation(TeleporterMod.MODID, "textures/gui/container/teleporterBlock.png");

	private TileEntityTeleporter tileEntityTeleporter;

	public GuiTeleporter(InventoryPlayer invPlayer, TileEntityTeleporter tile)
	{
		super(new ContainerTeleporter(invPlayer, tile));
		tileEntityTeleporter = tile;
		xSize = 176;
		ySize = 166;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		final int LABEL_XPOS = 5;
		final int LABEL_YPOS = 5;
		fontRendererObj.drawString(tileEntityTeleporter.getDisplayName().getUnformattedText(), LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(TELEPORTER_GUI_TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

}
