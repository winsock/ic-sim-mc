package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class VoltageElm extends CircuitElm {
	static final int WF_DC = 0;
	static final int WF_AC = 1;
	static final int WF_SQUARE = 2;
	static final int WF_VAR = 6;
	private static final int FLAG_COS = 2;
	private static final int WF_TRIANGLE = 3;
	private static final int WF_SAWTOOTH = 4;
	private static final int WF_PULSE = 5;
	final int circleSize = 17;
	int waveform;
	double frequency;
	double maxVoltage;
	double bias;
	private double freqTimeZero;
	private double phaseShift;
	private double dutyCycle;

	public VoltageElm(int xx, int yy, int wf) {
		super(xx, yy);
		waveform = wf;
		maxVoltage = 5;
		frequency = 40;
		dutyCycle = .5;
		reset();
	}

	public VoltageElm(int xa, int ya, int xb, int yb, int f,
	                  StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		maxVoltage = 5;
		frequency = 40;
		waveform = WF_DC;
		dutyCycle = .5;
		try {
			waveform = new Integer(st.nextToken());
			frequency = new Double(st.nextToken());
			maxVoltage = new Double(st.nextToken());
			bias = new Double(st.nextToken());
			phaseShift = new Double(st.nextToken());
			dutyCycle = new Double(st.nextToken());
		} catch (Exception ignored) {
		}
		if ((flags & FLAG_COS) != 0) {
			flags &= ~FLAG_COS;
			phaseShift = Math.PI / 2;
		}
		reset();
	}

	public int getDumpType() {
		return 'v';
	}
	/*void setCurrent(double c) {
	  current = c;
      System.out.print("v current set to " + c + "\n");
      }*/

	protected String dump() {
		return super.dump() + " " + waveform + " " + frequency + " " +
			maxVoltage + " " + bias + " " + phaseShift + " " +
			dutyCycle;
	}

	public void reset() {
		freqTimeZero = 0;
		curcount = 0;
	}

	private double triangleFunc(double x) {
		if (x < Math.PI)
			return x * (2 / Math.PI) - 1;
		return 1 - (x - Math.PI) * (2 / Math.PI);
	}

	public void stamp() {
		if (waveform == WF_DC)
			sim.stampVoltageSource(nodes[0], nodes[1], voltSource,
				getVoltage());
		else
			sim.stampVoltageSource(nodes[0], nodes[1], voltSource);
	}

	public void doStep() {
		if (waveform != WF_DC)
			sim.updateVoltageSource(nodes[0], nodes[1], voltSource,
				getVoltage());
	}

	protected double getVoltage() {
		double w = 2 * Math.PI * (sim.getTime() - freqTimeZero) * frequency + phaseShift;
		switch (waveform) {
			case WF_DC:
				return maxVoltage + bias;
			case WF_AC:
				return Math.sin(w) * maxVoltage + bias;
			case WF_SQUARE:
				return bias + ((w % (2 * Math.PI) > (2 * Math.PI * dutyCycle)) ?
				               -maxVoltage : maxVoltage);
			case WF_TRIANGLE:
				return bias + triangleFunc(w % (2 * Math.PI)) * maxVoltage;
			case WF_SAWTOOTH:
				return bias + (w % (2 * Math.PI)) * (maxVoltage / Math.PI) - maxVoltage;
			case WF_PULSE:
				return ((w % (2 * Math.PI)) < 1) ? maxVoltage + bias : bias;
			default:
				return 0;
		}
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads((waveform == WF_DC || waveform == WF_VAR) ? 8 : circleSize * 2);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		setBbox(x, y, x2, y2);
		draw2Leads(g, lightGrayColor);
		if (waveform == WF_DC) {
			interpPoint2(lead1, lead2, ps1, ps2, 0, 10);
			drawThickLine(g, ps1, ps2, getVoltageColor(volts[0]));
			int hs = 16;
			setBbox(point1, point2, hs);
			interpPoint2(lead1, lead2, ps1, ps2, 1, hs);
			drawThickLine(g, ps1, ps2, getVoltageColor(volts[1]));
		} else {
			setBbox(point1, point2, circleSize);
			interpPoint(lead1, lead2, ps1, .5);
			drawWaveform(g, ps1);
		}
		updateDotCount();
		if (sim.getDragElm() != this) {
			if (waveform == WF_DC)
				drawDots(g, point1, point2, curcount);
			else {
				drawDots(g, point1, lead1, curcount);
				drawDots(g, point2, lead2, -curcount);
			}
		}
		drawPosts(g, lightGrayColor);
	}

	void drawWaveform(CircuitGUI.ClientCircuitGui g, Point center) {
		Color color = needsHighlight() ? selectColor : Color.gray;
		int xc = center.getX();
		int yc = center.getY();
		drawThickCircle(g, xc, yc, circleSize, color);
		int wl = 8;
		adjustBbox(xc - circleSize, yc - circleSize,
			xc + circleSize, yc + circleSize);
		int xc2;
		switch (waveform) {
			case WF_DC: {
				break;
			}
			case WF_SQUARE:
				xc2 = (int) (wl * 2 * dutyCycle - wl + xc);
				xc2 = max(xc - wl + 3, min(xc + wl - 3, xc2));
				drawThickLine(g, xc - wl, yc - wl, xc - wl, yc, color);
				drawThickLine(g, xc - wl, yc - wl, xc2, yc - wl, color);
				drawThickLine(g, xc2, yc - wl, xc2, yc + wl, color);
				drawThickLine(g, xc + wl, yc + wl, xc2, yc + wl, color);
				drawThickLine(g, xc + wl, yc, xc + wl, yc + wl, color);
				break;
			case WF_PULSE:
				yc += wl / 2;
				drawThickLine(g, xc - wl, yc - wl, xc - wl, yc, color);
				drawThickLine(g, xc - wl, yc - wl, xc - wl / 2, yc - wl, color);
				drawThickLine(g, xc - wl / 2, yc - wl, xc - wl / 2, yc, color);
				drawThickLine(g, xc - wl / 2, yc, xc + wl, yc, color);
				break;
			case WF_SAWTOOTH:
				drawThickLine(g, xc, yc - wl, xc - wl, yc, color);
				drawThickLine(g, xc, yc - wl, xc, yc + wl, color);
				drawThickLine(g, xc, yc + wl, xc + wl, yc, color);
				break;
			case WF_TRIANGLE: {
				int xl = 5;
				drawThickLine(g, xc - xl * 2, yc, xc - xl, yc - wl, color);
				drawThickLine(g, xc - xl, yc - wl, xc, yc, color);
				drawThickLine(g, xc, yc, xc + xl, yc + wl, color);
				drawThickLine(g, xc + xl, yc + wl, xc + xl * 2, yc, color);
				break;
			}
			case WF_AC: {
				int i;
				int xl = 10;
				int ox = -1, oy = -1;
				for (i = -xl; i <= xl; i++) {
					int yy = yc + (int) (.95 * Math.sin(i * Math.PI / xl) * wl);
					if (ox != -1)
						drawThickLine(g, ox, oy, xc + i, yy, color);
					ox = xc + i;
					oy = yy;
				}
				break;
			}
		}
/*        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(frequency, "Hz");
            if (dx == 0 || dy == 0)
                drawValues(g, s, circleSize);
        }*/
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	protected double getPower() {
		return -getVoltageDiff() * current;
	}

	protected double getVoltageDiff() {
		return volts[1] - volts[0];
	}

	public void getInfo(String arr[]) {
		switch (waveform) {
			case WF_DC:
			case WF_VAR:
				arr[0] = "voltage source";
				break;
			case WF_AC:
				arr[0] = "A/C source";
				break;
			case WF_SQUARE:
				arr[0] = "square wave gen";
				break;
			case WF_PULSE:
				arr[0] = "pulse gen";
				break;
			case WF_SAWTOOTH:
				arr[0] = "sawtooth gen";
				break;
			case WF_TRIANGLE:
				arr[0] = "triangle gen";
				break;
		}
		arr[1] = "I = " + getCurrentText(getCurrent());
		arr[2] = ((this instanceof RailElm) ? "V = " : "Vd = ") +
			getVoltageText(getVoltageDiff());
		if (waveform != WF_DC && waveform != WF_VAR) {
			arr[3] = "f = " + getUnitText(frequency, "Hz");
			arr[4] = "Vmax = " + getVoltageText(maxVoltage);
			int i = 5;
			if (bias != 0)
				arr[i++] = "Voff = " + getVoltageText(bias);
			else if (frequency > 500)
				arr[i++] = "wavelength = " +
					getUnitText(2.9979e8 / frequency, "m");
			arr[i++] = "P = " + getUnitText(getPower(), "W");
		}
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo(waveform == WF_DC ? "Voltage" :
			                    "Max Voltage", maxVoltage, -20, 20);
/*        if (n == 1) {
            EditInfo ei = new EditInfo("Waveform", waveform, -1, -1);
            ei.choice = new Choice();
            ei.choice.add("D/C");
            ei.choice.add("A/C");
            ei.choice.add("Square Wave");
            ei.choice.add("Triangle");
            ei.choice.add("Sawtooth");
            ei.choice.add("Pulse");
            ei.choice.select(waveform);
            return ei;
        }*/
		if (waveform == WF_DC)
			return null;
		if (n == 2)
			return new EditInfo("Frequency (Hz)", frequency, 4, 500);
		if (n == 3)
			return new EditInfo("DC Offset (V)", bias, -20, 20);
		if (n == 4)
			return new EditInfo("Phase Offset (degrees)", phaseShift * 180 / Math.PI,
				-180, 180).setDimensionless();
		if (n == 5 && waveform == WF_SQUARE)
			return new EditInfo("Duty Cycle", dutyCycle * 100, 0, 100).
				setDimensionless();
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			maxVoltage = ei.getValue();
		if (n == 3)
			bias = ei.getValue();
		if (n == 2) {
			// adjust time zero to maintain continuity ind the waveform
			// even though the frequency has changed.
			double oldfreq = frequency;
			frequency = ei.getValue();
			double maxfreq = 1 / (8 * sim.getTimeStep());
			if (frequency > maxfreq)
				frequency = maxfreq;
			double adj = frequency - oldfreq;
			freqTimeZero = sim.getTime() - oldfreq * (sim.getTime() - freqTimeZero) / frequency;
		}
/*        if (n == 1) {
            int ow = waveform;
            waveform = ei.choice.getSelectedIndex();
            if (waveform == WF_DC && ow != WF_DC) {
                ei.newDialog = true;
                bias = 0;
            } else if (waveform != WF_DC && ow == WF_DC) {
                ei.newDialog = true;
            }
            if ((waveform == WF_SQUARE || ow == WF_SQUARE) &&
                    waveform != ow)
                ei.newDialog = true;
            setPoints();
        }*/
		if (n == 4)
			phaseShift = ei.getValue() * Math.PI / 180;
		if (n == 5)
			dutyCycle = ei.getValue() * .01;
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}
