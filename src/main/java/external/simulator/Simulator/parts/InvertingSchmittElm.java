package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

// contributed by Edward Calver

public class InvertingSchmittElm extends CircuitElm {
	double slewRate; // V/ns
	double lowerTrigger;
	double upperTrigger;
	boolean state;
	private Polygon gatePoly;
	private Polygon symbolPoly;
	private Point pcircle;
	private double dlt;
	private double dut;

	public InvertingSchmittElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
		slewRate = .5;
		state = false;
		lowerTrigger = 1.66;
		upperTrigger = 3.33;
	}

	public InvertingSchmittElm(int xa, int ya, int xb, int yb, int f,
	                           StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		noDiagonal = true;
		try {
			slewRate = new Double(st.nextToken());
			lowerTrigger = new Double(st.nextToken());
			upperTrigger = new Double(st.nextToken());

		} catch (Exception e) {
			slewRate = .5;
			lowerTrigger = 1.66;
			upperTrigger = 3.33;
		}
	}

	protected String dump() {
		return super.dump() + " " + slewRate + " " + lowerTrigger + " " + upperTrigger;
	}

	public int getDumpType() {
		return 183;
	}//Trying to find unused type

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		drawPosts(g, lightGrayColor);
		draw2Leads(g, lightGrayColor);
		Color color = needsHighlight() ? selectColor : lightGrayColor;
		drawThickPolygon(g, gatePoly, color);
		drawThickPolygon(g, symbolPoly, color);
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
		Point symPoints[] = newPointArray(6);
		Point dummy = new Point(0, 0);
		interpPoint2(lead1, lead2, triPoints[0], triPoints[1], 0, hs);
		triPoints[2] = interpPoint(point1, point2, .5 + (ww - 5) / dn);

		interpPoint2(lead1, lead2, symPoints[5], symPoints[4], 0.2, hs / 4);// 0 5 1
		interpPoint2(lead1, lead2, symPoints[1], symPoints[2], 0.35, hs / 4);//  4 2 3
		interpPoint2(lead1, lead2, symPoints[0], dummy, 0.1, hs / 4);
		interpPoint2(lead1, lead2, dummy, symPoints[3], 0.45, hs / 4);

		gatePoly = createPolygon(triPoints);
		symbolPoly = createPolygon(symPoints);
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
		double out;
		if (state) {//Output is high
			if (volts[0] > upperTrigger)//Input voltage high enough to set output low
			{
				state = false;
				out = 0;
			} else {
				out = 5;
			}
		} else {//Output is low
			if (volts[0] < lowerTrigger)//Input voltage low enough to set output high
			{
				state = true;
				out = 5;
			} else {
				out = 0;
			}
		}

		double maxStep = slewRate * sim.getTimeStep() * 1e9;
		out = Math.max(Math.min(v0 + maxStep, out), v0 - maxStep);
		sim.updateVoltageSource(0, nodes[1], voltSource, out);
	}

	protected double getVoltageDiff() {
		return volts[0];
	}

	public void getInfo(String arr[]) {
		arr[0] = "InvertingSchmitt";
		arr[1] = "Vi = " + getVoltageText(volts[0]);
		arr[2] = "Vo = " + getVoltageText(volts[1]);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			dlt = lowerTrigger;
			return new EditInfo("Lower threshold (V)", lowerTrigger, 0.01, 5);
		}
		if (n == 1) {
			dut = upperTrigger;
			return new EditInfo("Upper threshold (V)", upperTrigger, 0.01, 5);
		}
		if (n == 2)
			return new EditInfo("Slew Rate (V/ns)", slewRate, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			dlt = ei.getValue();
		if (n == 1)
			dut = ei.getValue();
		if (n == 2)
			slewRate = ei.getValue();

		if (dlt > dut) {
			upperTrigger = dlt;
			lowerTrigger = dut;
		} else {
			upperTrigger = dut;
			lowerTrigger = dlt;
		}

	}

	// there is no current path through the InvertingSchmitt input, but there
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
}
