package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import external.simulator.Simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class MosfetElm extends CircuitElm {
	final int hs = 16;
	private final int FLAG_SHOWVT = 2;
	private int pcircler;
	Point src[];
	Point drn[];
	private Point pcircle;
	final int pnp;
	private final int FLAG_PNP = 1;
	private final int FLAG_DIGITAL = 4;
	private double vt;
	private Point[] gate;
	private Polygon arrowPoly;
	private double lastv1;
	private double lastv2;
	double ids;
	private int mode = 0;
	private double gm = 0;

	public MosfetElm(int xx, int yy, boolean pnpflag) {
		super(xx, yy);
		pnp = (pnpflag) ? -1 : 1;
		flags = (pnpflag) ? FLAG_PNP : 0;
		noDiagonal = true;
		vt = getDefaultThreshold();
	}

	public MosfetElm(int xa, int ya, int xb, int yb, int f,
	          StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		pnp = ((f & FLAG_PNP) != 0) ? -1 : 1;
		noDiagonal = true;
		vt = getDefaultThreshold();
		try {
			vt = new Double(st.nextToken());
		} catch (Exception ignored) {
		}
	}

	protected double getDefaultThreshold() {
		return 1.5;
	}

	protected double getBeta() {
		return .02;
	}

	protected boolean nonLinear() {
		return true;
	}

	private boolean drawDigital() {
		return (flags & FLAG_DIGITAL) != 0;
	}

	public void reset() {
		lastv1 = lastv2 = volts[0] = volts[1] = volts[2] = curcount = 0;
	}

	protected String dump() {
		return super.dump() + " " + vt;
	}

	public int getDumpType() {
		return 'f';
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		setBbox(point1, point2, hs);
		drawThickLine(g, src[0], src[1], getVoltageColor(volts[1]));
		drawThickLine(g, drn[0], drn[1], getVoltageColor(volts[2]));
		int segments = 6;
		int i;
		double segf = 1. / segments;
		for (i = 0; i != segments; i++) {
			double v = volts[1] + (volts[2] - volts[1]) * i / segments;
			interpPoint(src[1], drn[1], ps1, i * segf);
			interpPoint(src[1], drn[1], ps2, (i + 1) * segf);
			drawThickLine(g, ps1, ps2, getVoltageColor(v));
		}
		drawThickLine(g, src[1], src[2], getVoltageColor(volts[1]));
		drawThickLine(g, drn[1], drn[2], getVoltageColor(volts[2]));
		if (!drawDigital()) {
			drawPolygon(g, arrowPoly, getVoltageColor(pnp == 1 ? volts[1] : volts[2]));
		}
		Color color = getVoltageColor(volts[0]);
		drawThickLine(g, point1, gate[1], color);
		drawThickLine(g, gate[0], gate[2], color);
		if (drawDigital() && pnp == -1)
			drawThickCircle(g, pcircle.getX(), pcircle.getY(), pcircler, color);
		if ((flags & FLAG_SHOWVT) != 0) {
			String s = "" + (vt * pnp);
			drawCenteredText(g, s, x2 + 2, y2, false, whiteColor);
		}
		if ((needsHighlight() || sim.getDragElm() == this) && dy == 0) {
			FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
			int ds = sign(dx);
			g.drawString(renderer, "G", gate[1].getX() - 10 * ds, gate[1].getY() - 5, whiteColor.getRGB());
			g.drawString(renderer, pnp == -1 ? "D" : "S", src[0].getX() - 3 + 9 * ds, src[0].getY() + 4, whiteColor.getRGB()); // x+6 if ds=1, -12 if -1
			g.drawString(renderer, pnp == -1 ? "S" : "D", drn[0].getX() - 3 + 9 * ds, drn[0].getY() + 4, whiteColor.getRGB());
		}
		curcount = updateDotCount(-ids, curcount);
		drawDots(g, src[0], src[1], curcount);
		drawDots(g, src[1], drn[1], curcount);
		drawDots(g, drn[1], drn[0], curcount);
		drawPosts(g, lightGrayColor);
	}

	protected Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? src[0] : drn[0];
	}

	protected double getCurrent() {
		return ids;
	}

	protected double getPower() {
		return ids * (volts[2] - volts[1]);
	}

	protected int getPostCount() {
		return 3;
	}

	protected void setPoints() {
		super.setPoints();

		// find the coordinates of the various points we need to draw
		// the MOSFET.
		int hs2 = hs * dsign;
		src = newPointArray(3);
		drn = newPointArray(3);
		interpPoint2(point1, point2, src[0], drn[0], 1, -hs2);
		interpPoint2(point1, point2, src[1], drn[1], 1 - 22 / dn, -hs2);
		interpPoint2(point1, point2, src[2], drn[2], 1 - 22 / dn, -hs2 * 4 / 3);

		gate = newPointArray(3);
		interpPoint2(point1, point2, gate[0], gate[2], 1 - 28 / dn, hs2 / 2); // was 1-20/dn
		interpPoint(gate[0], gate[2], gate[1], .5);

/*        if (!drawDigital()) {
            if (pnp == 1)
                arrowPoly = calcArrow(src[1], src[0], 10, 4);
            else
                arrowPoly = calcArrow(drn[0], drn[1], 12, 5);
        } else if (pnp == -1) {
            interpPoint(point1, point2, gate[1], 1 - 36 / dn);
            int dist = (dsign < 0) ? 32 : 31;
            pcircle = interpPoint(point1, point2, 1 - dist / dn);
            pcircler = 3;
        }*/
	}

	public void stamp() {
		sim.stampNonLinear(nodes[1]);
		sim.stampNonLinear(nodes[2]);
	}

	public void doStep() {
		double vs[] = new double[3];
		vs[0] = volts[0];
		vs[1] = volts[1];
		vs[2] = volts[2];
		if (vs[1] > lastv1 + .5)
			vs[1] = lastv1 + .5;
		if (vs[1] < lastv1 - .5)
			vs[1] = lastv1 - .5;
		if (vs[2] > lastv2 + .5)
			vs[2] = lastv2 + .5;
		if (vs[2] < lastv2 - .5)
			vs[2] = lastv2 - .5;
		int source = 1;
		int drain = 2;
		if (pnp * vs[1] > pnp * vs[2]) {
			source = 2;
			drain = 1;
		}
		int gate = 0;
		double vgs = vs[gate] - vs[source];
		double vds = vs[drain] - vs[source];
		if (Math.abs(lastv1 - vs[1]) > .01 ||
			Math.abs(lastv2 - vs[2]) > .01)
			sim.setConverged(false);
		lastv1 = vs[1];
		lastv2 = vs[2];
		double realvgs = vgs;
		double realvds = vds;
		vgs *= pnp;
		vds *= pnp;
		ids = 0;
		gm = 0;
		double Gds = 0;
		double beta = getBeta();
		if (vgs > .5 && this instanceof JfetElm) {
			sim.stop("JFET is reverse biased!", this);
			return;
		}
		if (vgs < vt) {
			// should be all zero, but that causes a singular matrix,
			// so instead we treat it as a large resistor
			Gds = 1e-8;
			ids = vds * Gds;
			mode = 0;
		} else if (vds < vgs - vt) {
			// linear
			ids = beta * ((vgs - vt) * vds - vds * vds * .5);
			gm = beta * vds;
			Gds = beta * (vgs - vds - vt);
			mode = 1;
		} else {
			// saturation; Gds = 0
			gm = beta * (vgs - vt);
			// use very small Gds to avoid nonconvergence
			Gds = 1e-8;
			ids = .5 * beta * (vgs - vt) * (vgs - vt) + (vds - (vgs - vt)) * Gds;
			mode = 2;
		}
		double rs = -pnp * ids + Gds * realvds + gm * realvgs;
		//System.out.println("M " + vds + " " + vgs + " " + ids + " " + gm + " "+ Gds + " " + volts[0] + " " + volts[1] + " " + volts[2] + " " + source + " " + rs + " " + this);
		sim.stampMatrix(nodes[drain], nodes[drain], Gds);
		sim.stampMatrix(nodes[drain], nodes[source], -Gds - gm);
		sim.stampMatrix(nodes[drain], nodes[gate], gm);

		sim.stampMatrix(nodes[source], nodes[drain], -Gds);
		sim.stampMatrix(nodes[source], nodes[source], Gds + gm);
		sim.stampMatrix(nodes[source], nodes[gate], -gm);

		sim.stampRightSide(nodes[drain], rs);
		sim.stampRightSide(nodes[source], -rs);
		if (source == 2 && pnp == 1 ||
			source == 1 && pnp == -1)
			ids = -ids;
	}

	void getFetInfo(String arr[], String n) {
		arr[0] = ((pnp == -1) ? "p-" : "n-") + n;
		arr[0] += " (Vt = " + getVoltageText(pnp * vt) + ")";
		arr[1] = ((pnp == 1) ? "Ids = " : "Isd = ") + getCurrentText(ids);
		arr[2] = "Vgs = " + getVoltageText(volts[0] - volts[pnp == -1 ? 2 : 1]);
		arr[3] = ((pnp == 1) ? "Vds = " : "Vsd = ") + getVoltageText(volts[2] - volts[1]);
		arr[4] = (mode == 0) ? "off" :
		         (mode == 1) ? "linear" : "saturation";
		arr[5] = "gm = " + getUnitText(gm, "A/V");
	}

	public void getInfo(String arr[]) {
		getFetInfo(arr, "MOSFET");
	}

	protected boolean canViewInScope() {
		return true;
	}

	protected double getVoltageDiff() {
		return volts[2] - volts[1];
	}

	protected boolean getConnection(int n1, int n2) {
		return !(n1 == 0 || n2 == 0);
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Threshold Voltage", pnp * vt, .01, 5);
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Digital Symbol", drawDigital()));
			return ei;
		}

		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			vt = pnp * ei.getValue();
		if (n == 1) {
			flags = (ei.getCheckbox().getState()) ? (flags | FLAG_DIGITAL) :
			        (flags & ~FLAG_DIGITAL);
			setPoints();
		}
	}
}
