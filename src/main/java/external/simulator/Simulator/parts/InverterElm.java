package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class InverterElm extends CircuitElm {
	private double slewRate; // V/ns
	private Polygon gatePoly;
	private Point pcircle;

	public InverterElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
		slewRate = .5;
	}

	public InverterElm(int xa, int ya, int xb, int yb, int f,
	                   StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		noDiagonal = true;
		try {
			slewRate = new Double(st.nextToken());
		} catch (Exception e) {
			slewRate = .5;
		}
	}

	protected String dump() {
		return super.dump() + " " + slewRate;
	}

	public int getDumpType() {
		return 'I';
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		drawPosts(g, Color.lightGray);
		draw2Leads(g, Color.lightGray);
		Color color = (needsHighlight() ? selectColor : lightGrayColor);
		drawThickPolygon(g, gatePoly, color);
		drawThickCircle(g, pcircle.getX(), pcircle.getY(), 3, color);
		curcount = updateDotCount(current, curcount);
		drawDots(g, lead2, point2, curcount);
	}

	protected void setPoints() {
		super.setPoints();
		int hs = 16;
		int ww = 16;
		if (ww > dn / 2)
			ww = (int) (dn / 2);
		lead1 = interpPoint(point1, point2, .5 - ww / dn);
		lead2 = interpPoint(point1, point2, .5 + (ww + 2) / dn);
		pcircle = interpPoint(point1, point2, .5 + (ww - 2) / dn);
		Point triPoints[] = newPointArray(3);
		interpPoint2(lead1, lead2, triPoints[0], triPoints[1], 0, hs);
		triPoints[2] = interpPoint(point1, point2, .5 + (ww - 5) / dn);
		gatePoly = createPolygon(triPoints);
		setBbox(point1, point2, hs);
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	public void stamp() {
		sim.stampVoltageSource(0, nodes[1], voltSource);
	}

	public void doStep() {
		double v0 = volts[1];
		double out = volts[0] > 2.5 ? 0 : 5;
		double maxStep = slewRate * sim.getTimeStep() * 1e9;
		out = Math.max(Math.min(v0 + maxStep, out), v0 - maxStep);
		sim.updateVoltageSource(0, nodes[1], voltSource, out);
	}

	protected double getVoltageDiff() {
		return volts[0];
	}

	public void getInfo(String arr[]) {
		arr[0] = "inverter";
		arr[1] = "Vi = " + getVoltageText(volts[0]);
		arr[2] = "Vo = " + getVoltageText(volts[1]);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Slew Rate (V/ns)", slewRate, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		slewRate = ei.getValue();
	}

	// there is no current path through the inverter input, but there
	// is an indirect path through the output to ground.
	protected boolean getConnection(int n1, int n2) {
		return false;
	}

	protected boolean hasGroundConnection(int n1) {
		return (n1 == 1);
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public int getShortcut() {
		return '1';
	}
}
