package external.simulator.Simulator;


import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

import java.util.StringTokenizer;
import java.util.Vector;

class TextElm extends GraphicElm {
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
    boolean isWire() {
        return false;
    }

    void split() {
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

    String dump() {
        return super.dump() + " " + size + " " + text;
    }

    int getDumpType() {
        return 'x';
    }

    void drag(int xx, int yy) {
        x = xx;
        y = yy;
        x2 = xx + 16;
        y2 = yy;
    }

    void draw(CircuitGUI g, int mouseX, int mouseY, float partialTicks) {
        //Graphics2D g2 = (Graphics2D)g;
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        //	RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = (Color) (needsHighlight() ? selectColor : lightGrayColor);
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
            g.drawString(fontRenderer, s, x, curY, color.hashCode());
            if ((flags & FLAG_BAR) != 0) {
                int by = curY - fontRenderer.FONT_HEIGHT;
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                worldRenderer.startDrawing(GL11.GL_LINE);
                worldRenderer.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                worldRenderer.addVertex(x, by, 0);
                worldRenderer.addVertex(x + fontRenderer.getStringWidth(s) - 1, by, 0);
                worldRenderer.finishDrawing();
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
            ei.text = text;
            return ei;
        }
        if (n == 1)
            return new EditInfo("Size", size, 5, 100);
        if (n == 2) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox =
                    new Toggleable("Center", (flags & FLAG_CENTER) != 0);
            return ei;
        }
        if (n == 3) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox =
                    new Toggleable("Draw Bar On Top", (flags & FLAG_BAR) != 0);
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
            size = (int) ei.value;
        if (n == 3) {
            if (ei.checkbox.getState())
                flags |= FLAG_BAR;
            else
                flags &= ~FLAG_BAR;
        }
        if (n == 2) {
            if (ei.checkbox.getState())
                flags |= FLAG_CENTER;
            else
                flags &= ~FLAG_CENTER;
        }
    }

    boolean isCenteredText() {
        return (flags & FLAG_CENTER) != 0;
    }

    public void getInfo(String arr[]) {
        arr[0] = text;
    }

    @Override
    int getShortcut() {
        return 't';
    }
}

