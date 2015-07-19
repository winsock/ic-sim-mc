package external.simulator.Simulator;// stub implementation of TriacElm, based on SCRElm
// FIXME need to add TriacElm to srclist
// FIXME need to uncomment TriacElm line from CirSim.java

import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

// Silicon-Controlled Rectifier
// 3 nodes, 1 internal node
// 0 = anode, 1 = cathode, 2 = gate
// 0, 3 = variable resistor
// 3, 2 = diode
// 2, 1 = 50 ohm resistor

public class TriacElm extends CircuitElm {
	private final int anode = 0;
	private final int cnode = 1;
	private final int gnode = 2;
	private final int inode = 3;
	private final int hs = 8;
	private Diode diode;
	private double ia;
	private double ic;
	private double ig;
	private double curcount_a;
	private double curcount_c;
	private double curcount_g;
	private double lastvac;
	private double lastvag;
	private double cresistance;
	private double triggerI;
	private double holdingI;
	Polygon poly;
	private Point[] cathode;
	private Point[] gate;
	private double aresistance;

	public TriacElm(int xx, int yy) {
		super(xx, yy);
		setDefaults();
		setup();
	}

	public TriacElm(int xa, int ya, int xb, int yb, int f,
	                StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		setDefaults();
		try {
			lastvac = new Double(st.nextToken());
			lastvag = new Double(st.nextToken());
			volts[anode] = 0;
			volts[cnode] = -lastvac;
			volts[gnode] = -lastvag;
			triggerI = new Double(st.nextToken());
			holdingI = new Double(st.nextToken());
			cresistance = new Double(st.nextToken());
		} catch (Exception ignored) {
		}
		setup();
	}

	void setDefaults() {
		cresistance = 50;
		holdingI = .0082;
		triggerI = .01;
	}

	void setup() {
		diode = new Diode(sim);
		diode.setup(.8, 0);
	}

	boolean nonLinear() {
		return true;
	}

	void reset() {
		volts[anode] = volts[cnode] = volts[gnode] = 0;
		diode.reset();
		lastvag = lastvac = curcount_a = curcount_c = curcount_g = 0;
	}

	int getDumpType() {
		return 206;
	}

	String dump() {
		return super.dump() + " " + (volts[anode] - volts[cnode]) + " " +
			(volts[anode] - volts[gnode]) + " " + triggerI + " " + holdingI + " " +
			cresistance;
	}

	void setPoints() {
		super.setPoints();
		int dir = 0;
		if (abs(dx) > abs(dy)) {
			dir = -sign(dx) * sign(dy);
			point2.setY(point1.getY());
		} else {
			dir = sign(dy) * sign(dx);
			point2.setX(point1.getX());
		}
		if (dir == 0)
			dir = 1;
		calcLeads(16);
		cathode = newPointArray(2);
		Point pa[] = newPointArray(2);
		interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
		interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
		poly = createPolygon(pa[0], pa[1], lead2);

		gate = newPointArray(2);
		double leadlen = (dn - 16) / 2;
		int gatelen = sim.gridSize;
		gatelen += leadlen % sim.gridSize;
		if (leadlen < gatelen) {
			x2 = x;
			y2 = y;
			return;
		}
		interpPoint(lead2, point2, gate[0], gatelen / leadlen, gatelen * dir);
		interpPoint(lead2, point2, gate[1], gatelen / leadlen, sim.gridSize * 2 * dir);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		setBbox(point1, point2, hs);
		adjustBbox(gate[0], gate[1]);

		double v1 = volts[anode];
		double v2 = volts[cnode];

		draw2Leads(g, lightGrayColor);

		// draw arrow thingy
		drawPolygon(g, poly, getVoltageColor(v1));

		// draw thing arrow is pointing to
		Color color = getVoltageColor(v2);
		drawThickLine(g, cathode[0], cathode[1], color);

		drawThickLine(g, lead2, gate[0], color);
		drawThickLine(g, gate[0], gate[1], color);

		curcount_a = updateDotCount(ia, curcount_a);
		curcount_c = updateDotCount(ic, curcount_c);
		curcount_g = updateDotCount(ig, curcount_g);
		if (sim.dragElm != this) {
			drawDots(g, point1, lead2, curcount_a);
			drawDots(g, point2, lead2, curcount_c);
			drawDots(g, gate[1], gate[0], curcount_g);
			drawDots(g, gate[0], lead2, curcount_g + distance(gate[1], gate[0]));
		}
		drawPosts(g, lightGrayColor);
	}

	Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? point2 : gate[1];
	}

	int getPostCount() {
		return 3;
	}

	int getInternalNodeCount() {
		return 1;
	}

	double getPower() {
		return (volts[anode] - volts[gnode]) * ia + (volts[cnode] - volts[gnode]) * ic;
	}

	void stamp() {
		sim.stampNonLinear(nodes[anode]);
		sim.stampNonLinear(nodes[cnode]);
		sim.stampNonLinear(nodes[gnode]);
		sim.stampNonLinear(nodes[inode]);
		sim.stampResistor(nodes[gnode], nodes[cnode], cresistance);
		diode.stamp(nodes[inode], nodes[gnode]);
	}

	void doStep() {
		double vac = volts[anode] - volts[cnode]; // typically negative
		double vag = volts[anode] - volts[gnode]; // typically positive
		if (Math.abs(vac - lastvac) > .01 ||
			Math.abs(vag - lastvag) > .01)
			sim.converged = false;
		lastvac = vac;
		lastvag = vag;
		diode.doStep(volts[inode] - volts[gnode]);
		double icmult = 1 / triggerI;
		double iamult = 1 / holdingI - icmult;
		//System.out.println(icmult + " " + iamult);
		aresistance = (-icmult * ic + ia * iamult > 1) ? .0105 : 10e5;
		//System.out.println(vac + " " + vag + " " + sim.converged + " " + ic + " " + ia + " " + aresistance + " " + volts[inode] + " " + volts[gnode] + " " + volts[anode]);
		sim.stampResistor(nodes[anode], nodes[inode], aresistance);
	}

	public void getInfo(String arr[]) {
		arr[0] = "SCR";
		double vac = volts[anode] - volts[cnode];
		double vag = volts[anode] - volts[gnode];
		double vgc = volts[gnode] - volts[cnode];
		arr[1] = "Ia = " + getCurrentText(ia);
		arr[2] = "Ig = " + getCurrentText(ig);
		arr[3] = "Vac = " + getVoltageText(vac);
		arr[4] = "Vag = " + getVoltageText(vag);
		arr[5] = "Vgc = " + getVoltageText(vgc);
	}

	void calculateCurrent() {
		ic = (volts[cnode] - volts[gnode]) / cresistance;
		ia = (volts[anode] - volts[inode]) / aresistance;
		ig = -ic - ia;
	}

	public EditInfo getEditInfo(int n) {
		// ohmString doesn't work here on linux
		if (n == 0)
			return new EditInfo("Trigger Current (A)", triggerI, 0, 0);
		if (n == 1)
			return new EditInfo("Holding Current (A)", holdingI, 0, 0);
		if (n == 2)
			return new EditInfo("Gate-Cathode Resistance (ohms)", cresistance, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0 && ei.value > 0)
			triggerI = ei.value;
		if (n == 1 && ei.value > 0)
			holdingI = ei.value;
		if (n == 2 && ei.value > 0)
			cresistance = ei.value;
	}

	@Override
	boolean isWire() {
		return false;
	}
}

