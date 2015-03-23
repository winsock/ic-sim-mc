package me.querol.andrew.ic.Gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CircuitGUI extends GuiScreen {
    /** The location of the inventory background texture */
    protected static final ResourceLocation blankBackground = new ResourceLocation("textures/gui/demo_background.png");
    /** The X size of the inventory window in pixels. */
    protected int xSize = 500;
    /** The Y size of the inventory window in pixels. */
    protected int ySize = 380;
    /** Starting X position for the Gui. Inconsistent use for Gui backgrounds. */
    protected int guiLeft;
    /** Starting Y position for the Gui. Inconsistent use for Gui backgrounds. */
    protected int guiTop;

    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    /**
     * Draws either a gradient over the background screen
     */
    public void drawDefaultBackground() {
        super.drawDefaultBackground();
        this.mc.getTextureManager().bindTexture(blankBackground);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }
}
