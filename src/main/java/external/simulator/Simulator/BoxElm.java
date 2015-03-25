package external.simulator.Simulator;

import me.querol.andrew.ic.Gui.CircuitGUI;

import java.util.StringTokenizer;

import net.minecraft.client.gui.Gui;
import org.lwjgl.util.Color;

class BoxElm extends GraphicElm {

    public BoxElm(int xx, int yy) {
        super(xx, yy);
        x2 = xx + 16;
        y2 = yy + 16;
        setBbox(x, y, x2, y2);
    }

    public BoxElm(int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        x2 = xb;
        y2 = yb;
        if (st.hasMoreTokens())
            x = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens())
            y = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens())
            x2 = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens())
            y2 = Integer.parseInt(st.nextToken());
        setBbox(x, y, x2, y2);
    }

    int getDumpType() {
        return 'b';
    }

    void drag(int xx, int yy) {
        x = xx;
        y = yy;
    }

    void draw(CircuitGUI g, int mouseX, int mouseY, float partialTicks) {
        //g.setColor(needsHighlight() ? selectColor : lightGrayColor);
        Color color = (Color) (needsHighlight() ? selectColor : Color.GREY);
        setBbox(x, y, x2, y2);
        if (x < x2 && y < y2)
            Gui.drawRect(x, y, x2 - x, y2 - y, color.hashCode());
        else if (x > x2 && y < y2)
            Gui.drawRect(x2, y, x - x2, y2 - y, color.hashCode());
        else if (x < x2 && y > y2)
            Gui.drawRect(x, y2, x2 - x, y - y2, color.hashCode());
        else
            Gui.drawRect(x2, y2, x - x2, y - y2, color.hashCode());
    }

    public EditInfo getEditInfo(int n) {
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
    }

    public void getInfo(String arr[]) {
    }

    @Override
    int getShortcut() {
        return 0;
    }
}

