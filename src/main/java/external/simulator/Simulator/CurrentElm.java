package external.simulator.Simulator;


import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;
import org.lwjgl.util.Color;

import java.awt.*;
import java.util.StringTokenizer;

class CurrentElm extends CircuitElm {
    private double currentValue;
    Polygon arrow;
    private Point ashaft1;
    private Point ashaft2;
    private Point center;

    public CurrentElm(int xx, int yy) {
        super(xx, yy);
        currentValue = .01;
    }

    public CurrentElm(int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        try {
            currentValue = new Double(st.nextToken());
        } catch (Exception e) {
            currentValue = .01;
        }
    }

    String dump() {
        return super.dump() + " " + currentValue;
    }

    int getDumpType() {
        return 'i';
    }

    void setPoints() {
        super.setPoints();
        calcLeads(26);
        ashaft1 = interpPoint(lead1, lead2, .25);
        ashaft2 = interpPoint(lead1, lead2, .6);
        center = interpPoint(lead1, lead2, .5);
        Point p2 = interpPoint(lead1, lead2, .75);
        arrow = calcArrow(center, p2, 4, 4);
    }


    void draw(CircuitGUI g, int mouseX, int mouseY, float partialTicks) {
        int cr = 12;
        draw2Leads(g, (Color) CircuitElm.lightGrayColor);
        getVoltageColor((volts[0] + volts[1]) / 2);

        drawThickCircle(g, center.getX(), center.getY(), cr, (Color) Color.YELLOW);
        drawThickLine(g, ashaft1, ashaft2, (Color) CircuitElm.whiteColor);

//        g.fillPolygon(arrow);
        setBbox(point1, point2, cr);
        doDots(g);
/*        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(currentValue, "A");
            if (dx == 0 || dy == 0)
                drawValues(g, s, cr);
        }*/
        drawPosts(g, (Color) Color.LTGREY);
    }


    void stamp() {
        current = currentValue;
        sim.stampCurrentSource(nodes[0], nodes[1], current);
    }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Current (A)", currentValue, 0, .1);
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        currentValue = ei.value;
    }

    @Override
    boolean isWire() {
        return false;
    }

    public void getInfo(String arr[]) {
        arr[0] = "current source";
        getBasicInfo(arr);
    }

    double getVoltageDiff() {
        return volts[1] - volts[0];
    }
}
