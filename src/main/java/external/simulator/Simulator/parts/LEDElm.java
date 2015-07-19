package external.simulator.Simulator.parts;


import external.simulator.Simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class LEDElm extends DiodeElm {
    private double colorR;
    private double colorG;
    private double colorB;
    private Point ledLead1;
    private Point ledLead2;
    private Point ledCenter;

    public LEDElm(int xx, int yy) {
        super(xx, yy);
        fwdrop = 2.1024259;
        setup();
        colorR = 1;
        colorG = colorB = 0;
    }

    public LEDElm(int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        if ((f & FLAG_FWDROP) == 0)
            fwdrop = 2.1024259;
        setup();
        colorR = new Double(st.nextToken());
        colorG = new Double(st.nextToken());
        colorB = new Double(st.nextToken());
    }

    public int getDumpType() {
        return 162;
    }

    protected String dump() {
        return super.dump() + " " + colorR + " " + colorG + " " + colorB;
    }

    protected void setPoints() {
        super.setPoints();
        int cr = 12;
        ledLead1 = interpPoint(point1, point2, .5 - cr / dn);
        ledLead2 = interpPoint(point1, point2, .5 + cr / dn);
        ledCenter = interpPoint(point1, point2, .5);
    }

    public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
        if (needsHighlight() || this == sim.getDragElm()) {
            super.draw(g, mouseX, mouseY, partialTicks);
            return;
        }
        ;
        drawThickLine(g, point1, ledLead1, getVoltageColor(volts[0]));
        drawThickLine(g, ledLead2, point2, getVoltageColor(volts[1]));

        int cr = 12;
        drawThickCircle(g, ledCenter.getX(), ledCenter.getY(), cr, Color.gray);
        cr -= 4;
        double w = 255 * current / .01;
        if (w > 255)
            w = 255;
        Color cc = new Color((int) (colorR * w), (int) (colorG * w), (int) (colorB * w));
        drawOval(g, ledCenter.getX() - cr, ledCenter.getY() - cr, cr * 2, cr * 2, cc);
        setBbox(point1, point2, cr);
        updateDotCount();
        drawDots(g, point1, ledLead1, curcount);
        drawDots(g, point2, ledLead2, -curcount);
        drawPosts(g, lightGrayColor);
    }

    public void getInfo(String arr[]) {
        super.getInfo(arr);
        arr[0] = "LED";
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return super.getEditInfo(n);
        if (n == 1)
            return new EditInfo("Red Value (0-1)", colorR, 0, 1).
                    setDimensionless();
        if (n == 2)
            return new EditInfo("Green Value (0-1)", colorG, 0, 1).
                    setDimensionless();
        if (n == 3)
            return new EditInfo("Blue Value (0-1)", colorB, 0, 1).
                    setDimensionless();
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            super.setEditValue(0, ei);
        if (n == 1)
            colorR = ei.getValue();
        if (n == 2)
            colorG = ei.getValue();
        if (n == 3)
            colorB = ei.getValue();
    }

    public int getShortcut() {
        return 'l';
    }
}
