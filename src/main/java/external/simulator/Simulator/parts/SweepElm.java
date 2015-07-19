package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import external.simulator.Simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;

import java.awt.*;
import java.util.StringTokenizer;

public class SweepElm extends CircuitElm {
	private final int FLAG_LOG = 1;
	private final int FLAG_BIDIR = 2;
	private final int circleSize = 17;
	private double maxV;
	private double maxF;
	private double minF;
	private double sweepTime;
	private double frequency;
	private double fadd;
	private double fmul;
	private double freqTime;
	private double savedTimeStep;
	private int dir = 1;
	private double v;

	public SweepElm(int xx, int yy) {
		super(xx, yy);
		minF = 20;
		maxF = 4000;
		maxV = 5;
		sweepTime = .1;
		flags = FLAG_BIDIR;
		reset();
	}

	public SweepElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		minF = new Double(st.nextToken());
		maxF = new Double(st.nextToken());
		maxV = new Double(st.nextToken());
		sweepTime = new Double(st.nextToken());
		reset();
	}

	public int getDumpType() {
		return 170;
	}

	protected int getPostCount() {
		return 1;
	}

	protected String dump() {
		return super.dump() + " " + minF + " " + maxF + " " + maxV + " " +
			sweepTime;
	}

	protected void setPoints() {
		super.setPoints();
		lead1 = interpPoint(point1, point2, 1 - circleSize / dn);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		setBbox(point1, point2, circleSize);
		drawThickLine(g, point1, lead1, getVoltageColor(volts[0]));
		Color color = (needsHighlight() ? selectColor : Color.gray);
		int xc = point2.getX();
		int yc = point2.getY();
		drawThickCircle(g, xc, yc, circleSize, color);
		int wl = 8;
		adjustBbox(xc - circleSize, yc - circleSize,
			xc + circleSize, yc + circleSize);
		int i;
		int xl = 10;
		int ox = -1, oy = -1;
		long tm = System.currentTimeMillis();
		//double w = (this == mouseElm ? 3 : 2);
		tm %= 2000;
		if (tm > 1000)
			tm = 2000 - tm;
		double w = 1 + tm * .002;
		if (!sim.isStopped())
			w = 1 + 2 * (frequency - minF) / (maxF - minF);
		for (i = -xl; i <= xl; i++) {
			int yy = yc + (int) (.95 * Math.sin(i * Math.PI * w / xl) * wl);
			if (ox != -1)
				drawThickLine(g, ox, oy, xc + i, yy, color);
			ox = xc + i;
			oy = yy;
		}
		/*if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(frequency, "Hz");
            if (dx == 0 || dy == 0)
                drawValues(g, s, circleSize);
        }*/

		drawPosts(g, lightGrayColor);
		curcount = updateDotCount(-current, curcount);
		if (sim.getDragElm() != this)
			drawDots(g, point1, lead1, curcount);
	}

	public void stamp() {
		sim.stampVoltageSource(0, nodes[0], voltSource);
	}

	private void setParams() {
		if (frequency < minF || frequency > maxF) {
			frequency = minF;
			freqTime = 0;
			dir = 1;
		}
		if ((flags & FLAG_LOG) == 0) {
			fadd = dir * sim.getTimeStep() * (maxF - minF) / sweepTime;
			fmul = 1;
		} else {
			fadd = 0;
			fmul = Math.pow(maxF / minF, dir * sim.getTimeStep() / sweepTime);
		}
		savedTimeStep = sim.getTimeStep();
	}

	public void reset() {
		frequency = minF;
		freqTime = 0;
		dir = 1;
		setParams();
	}

	protected void startIteration() {
		// has timestep been changed?
		if (sim.getTimeStep() != savedTimeStep)
			setParams();
		v = Math.sin(freqTime) * maxV;
		freqTime += frequency * 2 * Math.PI * sim.getTimeStep();
		frequency = frequency * fmul + fadd;
		if (frequency >= maxF && dir == 1) {
			if ((flags & FLAG_BIDIR) != 0) {
				fadd = -fadd;
				fmul = 1 / fmul;
				dir = -1;
			} else
				frequency = minF;
		}
		if (frequency <= minF && dir == -1) {
			fadd = -fadd;
			fmul = 1 / fmul;
			dir = 1;
		}
	}

	public void doStep() {
		sim.updateVoltageSource(0, nodes[0], voltSource, v);
	}

	protected double getVoltageDiff() {
		return volts[0];
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	protected boolean hasGroundConnection(int n1) {
		return true;
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public void getInfo(String arr[]) {
		arr[0] = "sweep " + (((flags & FLAG_LOG) == 0) ? "(linear)" : "(log)");
		arr[1] = "I = " + getCurrentDText(getCurrent());
		arr[2] = "V = " + getVoltageText(volts[0]);
		arr[3] = "f = " + getUnitText(frequency, "Hz");
		arr[4] = "range = " + getUnitText(minF, "Hz") + " .. " +
			getUnitText(maxF, "Hz");
		arr[5] = "time = " + getUnitText(sweepTime, "s");
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Min Frequency (Hz)", minF, 0, 0);
		if (n == 1)
			return new EditInfo("Max Frequency (Hz)", maxF, 0, 0);
		if (n == 2)
			return new EditInfo("Sweep Time (s)", sweepTime, 0, 0);
		if (n == 3) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Logarithmic", (flags & FLAG_LOG) != 0));
			return ei;
		}
		if (n == 4)
			return new EditInfo("Max Voltage", maxV, 0, 0);
		if (n == 5) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Bidirectional", (flags & FLAG_BIDIR) != 0));
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		double maxfreq = 1 / (8 * sim.getTimeStep());
		if (n == 0) {
			minF = ei.getValue();
			if (minF > maxfreq)
				minF = maxfreq;
		}
		if (n == 1) {
			maxF = ei.getValue();
			if (maxF > maxfreq)
				maxF = maxfreq;
		}
		if (n == 2)
			sweepTime = ei.getValue();
		if (n == 3) {
			flags &= ~FLAG_LOG;
			if (ei.getCheckbox().getState())
				flags |= FLAG_LOG;
		}
		if (n == 4)
			maxV = ei.getValue();
		if (n == 5) {
			flags &= ~FLAG_BIDIR;
			if (ei.getCheckbox().getState())
				flags |= FLAG_BIDIR;
		}
		setParams();
	}
}
    
