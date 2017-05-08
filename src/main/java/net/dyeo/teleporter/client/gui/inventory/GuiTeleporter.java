package net.dyeo.teleporter.client.gui.inventory;

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

	private static final ResourceLocation TELEPORTER_GUI_TEXTURE = new ResourceLocation(TeleporterMod.MODID, "textures/gui/container/teleporter.png");

	private final InventoryPlayer playerInventory;
	private final TileEntityTeleporter tileEntity;

	public GuiTeleporter(InventoryPlayer playerInventory, TileEntityTeleporter tileEntity)
	{
		super(new ContainerTeleporter(playerInventory, tileEntity));
		this.playerInventory = playerInventory;
		this.tileEntity = tileEntity;
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.fontRendererObj.drawString(this.tileEntity.getDisplayName().getUnformattedText(), 8, 6, 0x404040);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(TELEPORTER_GUI_TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

}
