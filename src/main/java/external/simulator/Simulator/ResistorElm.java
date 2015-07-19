package external.simulator.Simulator;

import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class ResistorElm extends CircuitElm {
	double resistance;
	private Point ps3;
	private Point ps4;

	public ResistorElm(int xx, int yy) {
		super(xx, yy);
		resistance = 100;
	}

	public ResistorElm(int xa, int ya, int xb, int yb, int f,
	                   StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		resistance = new Double(st.nextToken());
	}

	int getDumpType() {
		return 'r';
	}

	String dump() {
		return super.dump() + " " + resistance;
	}

	void setPoints() {
		super.setPoints();
		calcLeads(32);
		ps3 = new Point();
		ps4 = new Point();
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int segments = 16;
		int i;
		int ox = 0;
		int hs = sim.euroResistors ? 6 : 8;
		double v1 = volts[0];
		double v2 = volts[1];
		setBbox(point1, point2, hs);
		draw2Leads(g, lightGrayColor);
		double segf = 1. / segments;
		if (!sim.euroResistors) {
			// draw zigzag
			for (i = 0; i != segments; i++) {
				int nx = 0;
				switch (i & 3) {
					case 0:
						nx = 1;
						break;
					case 2:
						nx = -1;
						break;
					default:
						nx = 0;
						break;
				}
				double v = v1 + (v2 - v1) * i / segments;
				interpPoint(lead1, lead2, ps1, i * segf, hs * ox);
				interpPoint(lead1, lead2, ps2, (i + 1) * segf, hs * nx);
				drawThickLine(g, ps1, ps2, getVoltageColor(v));
				ox = nx;
			}
		} else {
			// draw rectangle
			interpPoint2(lead1, lead2, ps1, ps2, 0, hs);
			Color color = getVoltageColor(v1);
			drawThickLine(g, ps1, ps2, color);
			for (i = 0; i != segments; i++) {
				double v = v1 + (v2 - v1) * i / segments;
				Color innerColor = getVoltageColor(v);
				interpPoint2(lead1, lead2, ps1, ps2, i * segf, hs);
				interpPoint2(lead1, lead2, ps3, ps4, (i + 1) * segf, hs);
				drawThickLine(g, ps1, ps3, innerColor);
				drawThickLine(g, ps2, ps4, innerColor);
			}
			interpPoint2(lead1, lead2, ps1, ps2, 1, hs);
			drawThickLine(g, ps1, ps2, color);
		}
/*        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(resistance, "");
            drawValues(g, s, hs);
        }*/
		doDots(g);
		drawPosts(g, lightGrayColor);
	}

	void calculateCurrent() {
		current = (volts[0] - volts[1]) / resistance;
		//System.out.print(this + " res current set to " + current + "\n");
	}

	void stamp() {
		sim.stampResistor(nodes[0], nodes[1], resistance);
	}

	public void getInfo(String arr[]) {
		arr[0] = "resistor";
		getBasicInfo(arr);
		arr[3] = "R = " + getUnitText(resistance, CirSim.ohmString);
		arr[4] = "P = " + getUnitText(getPower(), "W");
	}

	public EditInfo getEditInfo(int n) {
		// ohmString doesn't work here on linux
		if (n == 0)
			return new EditInfo("Resistance (ohms)", resistance, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (ei.value > 0)
			resistance = ei.value;
	}

	@Override
	boolean isWire() {
		return false;
	}

	int getShortcut() {
		return 'r';
	}
}
