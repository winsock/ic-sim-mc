package external.simulator.Simulator;


import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class TextElm extends GraphicElm {
    private final int FLAG_CENTER = 1;
    private final int FLAG_BAR = 2;
    private String text;
    private Vector<String> lines;
    private int size;

    public TextElm(int xx, int yy) {
        super(xx, yy);
        text = "hello";
        lines = new Vector<String>();
        lines.add(text);
        size = 24;
    }

    public TextElm(int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        size = new Integer(st.nextToken());
        text = st.nextToken();
        while (st.hasMoreTokens())
            text += ' ' + st.nextToken();
        split();
    }

    @Override
    protected boolean isWire() {
        return false;
    }

    private void split() {
        int i;
        lines = new Vector<String>();
        StringBuilder sb = new StringBuilder(text);
        for (i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == '\\') {
                sb.deleteCharAt(i);
                c = sb.charAt(i);
                if (c == 'n') {
                    lines.add(sb.substring(0, i));
                    sb.delete(0, i + 1);
                    i = -1;
                }
            }
        }
        lines.add(sb.toString());
    }

    protected String dump() {
        return super.dump() + " " + size + " " + text;
    }

    public int getDumpType() {
        return 'x';
    }

    protected void drag(int xx, int yy) {
        x = xx;
        y = yy;
        x2 = xx + 16;
        y2 = yy;
    }

    public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
        //Graphics2D g2 = (Graphics2D)g;
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        //	RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = needsHighlight() ? selectColor : lightGrayColor;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int i;
        int maxW = -1;
        for (i = 0; i != lines.size(); i++) {
            int w = fontRenderer.getStringWidth(lines.elementAt(i));
            if (w > maxW)
                maxW = w;
        }
        int curY = y;
        setBbox(x, y, x, y);
        for (i = 0; i != lines.size(); i++) {
            String s = lines.elementAt(i);
            g.drawString(fontRenderer, s, x, curY, color.getRGB());
            if ((flags & FLAG_BAR) != 0) {
                int by = curY - fontRenderer.FONT_HEIGHT;
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
	            GlStateManager.enableBlend();
	            GlStateManager.disableTexture2D();
	            GlStateManager.disableCull();
	            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
	            GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	            worldRenderer.startDrawing(GL11.GL_LINE);
                worldRenderer.addVertex(g.getGuiLeft() + x, g.getGuiTop() + by, g.getZLevel());
                worldRenderer.addVertex(g.getGuiLeft() + (x + fontRenderer.getStringWidth(s) - 1), g.getGuiTop() + by, g.getZLevel());
	            tessellator.draw();
            }
            adjustBbox(x, curY - fontRenderer.FONT_HEIGHT / 2,
                    x + fontRenderer.getStringWidth(s), curY + fontRenderer.FONT_HEIGHT / 2);
            curY += fontRenderer.FONT_HEIGHT;
        }
        x2 = boundingBox.getX() + boundingBox.getWidth();
        y2 = boundingBox.getY() + boundingBox.getHeight();
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("Text", 0, -1, -1);
            ei.setText(text);
            return ei;
        }
        if (n == 1)
            return new EditInfo("Size", size, 5, 100);
        if (n == 2) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Toggleable("Center", (flags & FLAG_CENTER) != 0));
            return ei;
        }
        if (n == 3) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Toggleable("Draw Bar On Top", (flags & FLAG_BAR) != 0));
            return ei;
        }
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
/*        if (n == 0) {
            text = ei.textf.getText();
            split();
        }*/
        if (n == 1)
            size = (int) ei.getValue();
        if (n == 3) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_BAR;
            else
                flags &= ~FLAG_BAR;
        }
        if (n == 2) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_CENTER;
            else
                flags &= ~FLAG_CENTER;
        }
    }

    protected boolean isCenteredText() {
        return (flags & FLAG_CENTER) != 0;
    }

    public void getInfo(String arr[]) {
        arr[0] = text;
    }

    @Override
    public int getShortcut() {
        return 't';
    }
}

