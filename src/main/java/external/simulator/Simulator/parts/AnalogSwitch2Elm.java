package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.util.StringTokenizer;

public class AnalogSwitch2Elm extends AnalogSwitchElm {
    private final int openhs = 16;
    private Point[] swposts;
    private Point[] swpoles;
    private Point ctlPoint;

    public AnalogSwitch2Elm(int xx, int yy) {
        super(xx, yy);
    }

    public AnalogSwitch2Elm(int xa, int ya, int xb, int yb, int f,
                            StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    protected void setPoints() {
        super.setPoints();
        calcLeads(32);
        swposts = newPointArray(2);
        swpoles = newPointArray(2);
        interpPoint2(lead1, lead2, swpoles[0], swpoles[1], 1, openhs);
        interpPoint2(point1, point2, swposts[0], swposts[1], 1, openhs);
        ctlPoint = interpPoint(point1, point2, .5, openhs);
    }

    protected int getPostCount() {
        return 4;
    }

    void draw(CircuitGUI.ClientCircuitGui g) {
        setBbox(point1, point2, openhs);

        // draw first lead
        CircuitElm.drawThickLine(g, point1, lead1, getVoltageColor(volts[0]));

        // draw second lead
        CircuitElm.drawThickLine(g, swpoles[0], swposts[0], getVoltageColor(volts[1]));

        // draw third lead
        CircuitElm.drawThickLine(g, swpoles[1], swposts[1], getVoltageColor(volts[2]));

        // draw switch
        int position = (open) ? 1 : 0;
        CircuitElm.drawThickLine(g, lead1, swpoles[position], CircuitElm.lightGrayColor);

        updateDotCount();
        drawDots(g, point1, lead1, curcount);
        drawDots(g, swpoles[position], swposts[position], curcount);
        drawPosts(g, CircuitElm.lightGrayColor);
    }

    protected Point getPost(int n) {
        return (n == 0) ? point1 : (n == 3) ? ctlPoint : swposts[n - 1];
    }

    public int getDumpType() {
        return 160;
    }

    protected void calculateCurrent() {
        if (open)
            current = (volts[0] - volts[2]) / r_on;
        else
            current = (volts[0] - volts[1]) / r_on;
    }

    public void stamp() {
        CircuitElm.sim.stampNonLinear(nodes[0]);
        CircuitElm.sim.stampNonLinear(nodes[1]);
        CircuitElm.sim.stampNonLinear(nodes[2]);
    }

    public void doStep() {
        open = (volts[3] < 2.5);
        if ((flags & FLAG_INVERT) != 0)
            open = !open;
        if (open) {
            CircuitElm.sim.stampResistor(nodes[0], nodes[2], r_on);
            CircuitElm.sim.stampResistor(nodes[0], nodes[1], r_off);
        } else {
            CircuitElm.sim.stampResistor(nodes[0], nodes[1], r_on);
            CircuitElm.sim.stampResistor(nodes[0], nodes[2], r_off);
        }
    }

    protected boolean getConnection(int n1, int n2) {
        return !(n1 == 3 || n2 == 3);
    }

    public void getInfo(String arr[]) {
        arr[0] = "analog switch (SPDT)";
        arr[1] = "I = " + CircuitElm.getCurrentDText(getCurrent());
    }
}

