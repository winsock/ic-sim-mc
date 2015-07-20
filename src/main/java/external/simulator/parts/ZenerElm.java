package external.simulator.parts;

import external.simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

// Zener code contributed by J. Mike Rollins
// http://www.camotruck.net/rollins/simulator.html
public class ZenerElm extends DiodeElm {
	private final int hs = 8;
	private final double default_zvoltage = 5.6;
	Polygon poly;
	private Point[] cathode;
	private Point[] wing;

	public ZenerElm(int xx, int yy) {
		super(xx, yy);
		zvoltage = default_zvoltage;
		setup();
	}

	public ZenerElm(int xa, int ya, int xb, int yb, int f,
	                StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		zvoltage = new Double(st.nextToken());
		setup();
	}

	protected void setup() {
		diode.leakage = 5e-6; // 1N4004 is 5.0 uAmp
		super.setup();
	}

	public int getDumpType() {
		return 'z';
	}

	protected String dump() {
		return super.dump() + " " + zvoltage;
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads(16);
		cathode = newPointArray(2);
		wing = newPointArray(2);
		Point pa[] = newPointArray(2);
		interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
		interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
		interpPoint(cathode[0], cathode[1], wing[0], -0.2, -hs);
		interpPoint(cathode[1], cathode[0], wing[1], -0.2, -hs);
		poly = createPolygon(pa[0], pa[1], lead2);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		setBbox(point1, point2, hs);

		double v1 = volts[0];
		double v2 = volts[1];

		draw2Leads(g, getVoltageColor(v1));

		// draw arrow thingy
		drawPolygon(g, poly, getVoltageColor(v1));

		// draw thing arrow is pointing to
		drawThickLine(g, cathode[0], cathode[1], getVoltageColor(v2));

		// draw wings on cathode
		drawThickLine(g, wing[0], cathode[0], getVoltageColor(v2));
		drawThickLine(g, wing[1], cathode[1], getVoltageColor(v2));

		doDots(g);
		drawPosts(g, Color.lightGray);
	}

	public void getInfo(String arr[]) {
		super.getInfo(arr);
		arr[0] = "Zener diode";
		arr[5] = "Vz = " + getVoltageText(zvoltage);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Fwd Voltage @ 1A", fwdrop, 10, 1000);
		if (n == 1)
			return new EditInfo("Zener Voltage @ 5mA", zvoltage, 1, 25);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			fwdrop = ei.getValue();
		if (n == 1)
			zvoltage = ei.getValue();
		setup();
	}

	public int getShortcut() {
		return 0;
	}
}
