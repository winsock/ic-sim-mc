package external.simulator.Simulator.parts;


import me.querol.andrew.ic.Gui.CircuitGUI;

import java.awt.*;
import java.util.StringTokenizer;

public class RailElm extends VoltageElm {
    final int FLAG_CLOCK = 1;

    public RailElm(int xx, int yy) {
        super(xx, yy, WF_DC);
    }

	public RailElm(int xx, int yy, int wf) {
        super(xx, yy, wf);
    }

    public RailElm(int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    public int getDumpType() {
        return 'R';
    }

    protected int getPostCount() {
        return 1;
    }

    protected void setPoints() {
        super.setPoints();
        lead1 = interpPoint(point1, point2, 1 - circleSize / dn);
    }

    public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
        setBbox(point1, point2, circleSize);
        drawThickLine(g, point1, lead1, getVoltageColor(volts[0]));
        boolean clock = waveform == WF_SQUARE && (flags & FLAG_CLOCK) != 0;
        if (waveform == WF_DC || waveform == WF_VAR || clock) {
            Color color = (needsHighlight() ? selectColor : whiteColor);
            double v = getVoltage();
            String s = getShortUnitText(v, "V");
            if (Math.abs(v) < 1)
                s = showFormat.format(v) + "V";
            if (getVoltage() > 0)
                s = "+" + s;
            if (this instanceof AntennaElm)
                s = "Ant";
            if (clock)
                s = "CLK";
            drawCenteredText(g, s, x2, y2, true, color);
        } else {
            drawWaveform(g, point2);
        }
        drawPosts(g, lightGrayColor);
        curcount = updateDotCount(-current, curcount);
        if (sim.getDragElm() != this)
            drawDots(g, point1, lead1, curcount);
    }

    protected double getVoltageDiff() {
        return volts[0];
    }

    public void stamp() {
        if (waveform == WF_DC)
            sim.stampVoltageSource(0, nodes[0], voltSource, getVoltage());
        else
            sim.stampVoltageSource(0, nodes[0], voltSource);
    }

    public void doStep() {
        if (waveform != WF_DC)
            sim.updateVoltageSource(0, nodes[0], voltSource, getVoltage());
    }

    protected boolean hasGroundConnection(int n1) {
        return true;
    }

    public int getShortcut() {
        return 'V';
    }
}
