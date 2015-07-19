package external.simulator.Simulator.parts;

import external.simulator.Simulator.CirSim;
import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class MemristorElm extends CircuitElm {
	private double r_on;
	private double r_off;
	private double dopeWidth;
	private double totalWidth;
	private double mobility;
	private double resistance;
	private Point ps3;
	private Point ps4;

	public MemristorElm(int xx, int yy) {
		super(xx, yy);
		r_on = 100;
		r_off = 160 * r_on;
		dopeWidth = 0;
		totalWidth = 10e-9; // meters
		mobility = 1e-10;   // m^2/sV
		resistance = 100;
	}

	public MemristorElm(int xa, int ya, int xb, int yb, int f,
	                    StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		r_on = new Double(st.nextToken());
		r_off = new Double(st.nextToken());
		dopeWidth = new Double(st.nextToken());
		totalWidth = new Double(st.nextToken());
		mobility = new Double(st.nextToken());
		resistance = 100;
	}

	public int getDumpType() {
		return 'm';
	}

	protected String dump() {
		return super.dump() + " " + r_on + " " + r_off + " " + dopeWidth + " " +
			totalWidth + " " + mobility;
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads(32);
		ps3 = new Point();
		ps4 = new Point();
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int segments = 6;
		int i;
		int ox = 0;
		double v1 = volts[0];
		double v2 = volts[1];
		int hs = 2 + (int) (8 * (1 - dopeWidth / totalWidth));
		setBbox(point1, point2, hs);
		draw2Leads(g, lightGrayColor);
		double segf = 1. / segments;

		// draw zigzag
		for (i = 0; i <= segments; i++) {
			int nx = (i & 1) == 0 ? 1 : -1;
			if (i == segments)
				nx = 0;
			double v = v1 + (v2 - v1) * i / segments;
			Color color = getVoltageColor(v);
			interpPoint(lead1, lead2, ps1, i * segf, hs * ox);
			interpPoint(lead1, lead2, ps2, i * segf, hs * nx);
			drawThickLine(g, ps1, ps2, color);
			if (i == segments)
				break;
			interpPoint(lead1, lead2, ps1, (i + 1) * segf, hs * nx);
			drawThickLine(g, ps1, ps2, color);
			ox = nx;
		}

		doDots(g);
		drawPosts(g, lightGrayColor);
	}

	protected boolean nonLinear() {
		return true;
	}

	protected void calculateCurrent() {
		current = (volts[0] - volts[1]) / resistance;
	}

	public void reset() {
		dopeWidth = 0;
	}

	protected void startIteration() {
		double wd = dopeWidth / totalWidth;
		dopeWidth += sim.getTimeStep() * mobility * r_on * current / totalWidth;
		if (dopeWidth < 0)
			dopeWidth = 0;
		if (dopeWidth > totalWidth)
			dopeWidth = totalWidth;
		resistance = r_on * wd + r_off * (1 - wd);
	}

	public void stamp() {
		sim.stampNonLinear(nodes[0]);
		sim.stampNonLinear(nodes[1]);
	}

	public void doStep() {
		sim.stampResistor(nodes[0], nodes[1], resistance);
	}

	public void getInfo(String arr[]) {
		arr[0] = "memristor";
		getBasicInfo(arr);
		arr[3] = "R = " + getUnitText(resistance, CirSim.ohmString);
		arr[4] = "P = " + getUnitText(getPower(), "W");
	}

	protected double getScopeValue(int x) {
		return (x == 2) ? resistance : (x == 1) ? getPower() : getVoltageDiff();
	}

	protected String getScopeUnits(int x) {
		return (x == 2) ? CirSim.ohmString : (x == 1) ? "W" : "V";
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Max Resistance (ohms)", r_on, 0, 0);
		if (n == 1)
			return new EditInfo("Min Resistance (ohms)", r_off, 0, 0);
		if (n == 2)
			return new EditInfo("Width of Doped Region (nm)", dopeWidth * 1e9, 0, 0);
		if (n == 3)
			return new EditInfo("Total Width (nm)", totalWidth * 1e9, 0, 0);
		if (n == 4)
			return new EditInfo("Mobility (um^2/(s*V))", mobility * 1e12, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			r_on = ei.getValue();
		if (n == 1)
			r_off = ei.getValue();
		if (n == 2)
			dopeWidth = ei.getValue() * 1e-9;
		if (n == 3)
			totalWidth = ei.getValue() * 1e-9;
		if (n == 4)
			mobility = ei.getValue() * 1e-12;
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}

