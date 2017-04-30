package net.dyeo.teleporter;

import java.awt.Color;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiTeleporter extends GuiContainer
{
	private static final ResourceLocation texture = new ResourceLocation("teleporter".toLowerCase(), "textures/gui/teleporterBlock.png");
	private TileEntityTeleporter tileEntityTeleporter;

	public GuiTeleporter(InventoryPlayer invPlayer, TileEntityTeleporter tile)
	{
		super(new ContainerTeleporter(invPlayer, tile));
		this.tileEntityTeleporter = tile;

		this.xSize = 176;
		this.ySize = 166;
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int LABEL_XPOS = 5;
		int LABEL_YPOS = 5;
		this.fontRendererObj.drawString("Teleporter", 5, 5, Color.darkGray.getRGB());
	}
}
