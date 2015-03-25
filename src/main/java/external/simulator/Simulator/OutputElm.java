package external.simulator.Simulator;

import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.util.Color;
import org.lwjgl.util.Point;

import java.util.StringTokenizer;

class OutputElm extends CircuitElm {
    private final int FLAG_VALUE = 1;

    public OutputElm(int xx, int yy) {
        super(xx, yy);
    }

    public OutputElm(int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        super(xa, ya, xb, yb, f);
    }

    int getDumpType() {
        return 'O';
    }

    int getPostCount() {
        return 1;
    }

    void setPoints() {
        super.setPoints();
        lead1 = new Point();
    }

    void draw(CircuitGUI g, int mouseX, int mouseY, float partialTicks) {
        boolean selected = (needsHighlight() || sim.plotYElm == this);
        Color color = (Color) (selected ? selectColor : whiteColor);
        String s = (flags & FLAG_VALUE) != 0 ? getVoltageText(volts[0]) : "out";
        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        if (this == sim.plotXElm)
            s = "X";
        if (this == sim.plotYElm)
            s = "Y";
        interpPoint(point1, point2, lead1, 1 - (renderer.getStringWidth(s) / 2 + 8) / dn);
        setBbox(point1, lead1, 0);
        drawCenteredText(g, s, x2, y2, true, color);
/*        if (selected)
            g.setColor(selectColor);*/
        drawThickLine(g, point1, lead1, (Color) getVoltageColor(volts[0]));
        drawPosts(g, (Color) lightGrayColor);
    }

    double getVoltageDiff() {
        return volts[0];
    }

    public void getInfo(String arr[]) {
        arr[0] = "output";
        arr[1] = "V = " + getVoltageText(volts[0]);
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Toggleable("Show Voltage",
                    (flags & FLAG_VALUE) != 0);
            return ei;
        }
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            flags = (ei.checkbox.getState()) ?
                    (flags | FLAG_VALUE) :
                    (flags & ~FLAG_VALUE);
    }

    @Override
    boolean isWire() {
        return false;
    }
}
