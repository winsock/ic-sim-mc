package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import external.simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

// 0 = switch
// 1 = switch end 1
// 2 = switch end 2
// ...
// 3n   = coil
// 3n+1 = coil
// 3n+2 = end of coil resistor

public class RelayElm extends CircuitElm {
	private final int nSwitch0 = 0;
	private final int nSwitch1 = 1;
	private final int nSwitch2 = 2;
	private final int FLAG_SWAP_COIL = 1;
	private final Inductor ind;
	double a1, a2, a3, a4;
	private double inductance;
	private double r_on;
	private double r_off;
	private double onCurrent;
	private Point[] coilPosts;
	private Point[] coilLeads;
	private Point[][] swposts;
	private Point[][] swpoles;
	private Point[] ptSwitch;
	private Point[] lines;
	private double coilCurrent;
	private double[] switchCurrent;
	private double coilCurCount;
	private double[] switchCurCount;
	private double d_position;
	private double coilR;
	private int i_position;
	private int poleCount;
	private int openhs;
	private int nCoil1;
	private int nCoil2;
	private int nCoil3;

	public RelayElm(int xx, int yy) {
		super(xx, yy);
		ind = new Inductor(sim);
		inductance = .2;
		ind.setup(inductance, 0, Inductor.FLAG_BACK_EULER);
		noDiagonal = true;
		onCurrent = .02;
		r_on = .05;
		r_off = 1e6;
		coilR = 20;
		coilCurrent = coilCurCount = 0;
		poleCount = 1;
		setupPoles();
	}

	public RelayElm(int xa, int ya, int xb, int yb, int f,
	                StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		poleCount = new Integer(st.nextToken());
		inductance = new Double(st.nextToken());
		coilCurrent = new Double(st.nextToken());
		r_on = new Double(st.nextToken());
		r_off = new Double(st.nextToken());
		onCurrent = new Double(st.nextToken());
		coilR = new Double(st.nextToken());
		noDiagonal = true;
		ind = new Inductor(sim);
		ind.setup(inductance, coilCurrent, Inductor.FLAG_BACK_EULER);
		setupPoles();
	}

	private void setupPoles() {
		nCoil1 = 3 * poleCount;
		nCoil2 = nCoil1 + 1;
		nCoil3 = nCoil1 + 2;
		if (switchCurrent == null || switchCurrent.length != poleCount) {
			switchCurrent = new double[poleCount];
			switchCurCount = new double[poleCount];
		}
	}

	public int getDumpType() {
		return 178;
	}

	protected String dump() {
		return super.dump() + " " + poleCount + " " +
			inductance + " " + coilCurrent + " " +
			r_on + " " + r_off + " " + onCurrent + " " + coilR;
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int i, p;
		for (i = 0; i != 2; i++) {
			drawThickLine(g, coilLeads[i], coilPosts[i], getVoltageColor(volts[nCoil1 + i]));
		}
		int x = ((flags & FLAG_SWAP_COIL) != 0) ? 1 : 0;
		drawCoil(g, dsign * 6, coilLeads[x], coilLeads[1 - x], volts[nCoil1 + x], volts[nCoil2 - x], lightGrayColor);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		// draw lines
		for (i = 0; i != poleCount; i++) {
			if (i == 0)
				interpPoint(point1, point2, lines[i * 2], .5,
					openhs * 2 + 5 * dsign - i * openhs * 3);
			else
				interpPoint(point1, point2, lines[i * 2], .5,
					(int) (openhs * (-i * 3 + 3 - .5 + d_position)) + 5 * dsign);
			interpPoint(point1, point2, lines[i * 2 + 1], .5,
				(int) (openhs * (-i * 3 - .5 + d_position)) - 5 * dsign);
			renderer.startDrawing(GL11.GL_LINE);
			renderer.setColorRGBA(Color.darkGray.getRed(), Color.darkGray.getGreen(), Color.darkGray.getBlue(), Color.darkGray.getAlpha());
			renderer.addVertex(g.getGuiLeft() + lines[i * 2].getX(), g.getGuiTop() + lines[i * 2].getY(), g.getZLevel());
			renderer.addVertex(g.getGuiLeft() + lines[i * 2 + 1].getX(), g.getGuiTop() + lines[i * 2 + 1].getY(), g.getZLevel());
		}
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();

		for (p = 0; p != poleCount; p++) {
			int po = p * 3;
			for (i = 0; i != 3; i++) {
				// draw lead
				drawThickLine(g, swposts[p][i], swpoles[p][i], getVoltageColor(volts[nSwitch0 + po + i]));
			}

			interpPoint(swpoles[p][1], swpoles[p][2], ptSwitch[p], d_position);
			//getVoltageColor(g, volts[nSwitch0]);
			drawThickLine(g, swpoles[p][0], ptSwitch[p], lightGrayColor);
			switchCurCount[p] = updateDotCount(switchCurrent[p],
				switchCurCount[p]);
			drawDots(g, swposts[p][0], swpoles[p][0], switchCurCount[p]);

			if (i_position != 2)
				drawDots(g, swpoles[p][i_position + 1], swposts[p][i_position + 1],
					switchCurCount[p]);
		}

		coilCurCount = updateDotCount(coilCurrent, coilCurCount);

		drawDots(g, coilPosts[0], coilLeads[0], coilCurCount);
		drawDots(g, coilLeads[0], coilLeads[1], coilCurCount);
		drawDots(g, coilLeads[1], coilPosts[1], coilCurCount);

		drawPosts(g, lightGrayColor);
		setBbox(coilPosts[0], coilLeads[1], 0);
		adjustBbox(swpoles[poleCount - 1][0], swposts[poleCount - 1][1]); // XXX
	}

	protected void setPoints() {
		super.setPoints();
		setupPoles();
		allocNodes();
		openhs = -dsign * 16;

		// switch
		calcLeads(32);
		swposts = new Point[poleCount][3];
		swpoles = new Point[poleCount][3];
		int i, j;
		for (i = 0; i != poleCount; i++) {
			for (j = 0; j != 3; j++) {
				swposts[i][j] = new Point();
				swpoles[i][j] = new Point();
			}
			interpPoint(lead1, lead2, swpoles[i][0], 0, -openhs * 3 * i);
			interpPoint(lead1, lead2, swpoles[i][1], 1, -openhs * 3 * i - openhs);
			interpPoint(lead1, lead2, swpoles[i][2], 1, -openhs * 3 * i + openhs);
			interpPoint(point1, point2, swposts[i][0], 0, -openhs * 3 * i);
			interpPoint(point1, point2, swposts[i][1], 1, -openhs * 3 * i - openhs);
			interpPoint(point1, point2, swposts[i][2], 1, -openhs * 3 * i + openhs);
		}

		// coil
		coilPosts = newPointArray(2);
		coilLeads = newPointArray(2);
		ptSwitch = newPointArray(poleCount);

		int x = ((flags & FLAG_SWAP_COIL) != 0) ? 1 : 0;
		interpPoint(point1, point2, coilPosts[0], x, openhs * 2);
		interpPoint(point1, point2, coilPosts[1], x, openhs * 3);
		interpPoint(point1, point2, coilLeads[0], .5, openhs * 2);
		interpPoint(point1, point2, coilLeads[1], .5, openhs * 3);

		// lines
		lines = newPointArray(poleCount * 2);
	}

	protected Point getPost(int n) {
		if (n < 3 * poleCount)
			return swposts[n / 3][n % 3];
		return coilPosts[n - 3 * poleCount];
	}

	protected int getPostCount() {
		return 2 + poleCount * 3;
	}

	protected int getInternalNodeCount() {
		return 1;
	}

	public void reset() {
		super.reset();
		ind.reset();
		coilCurrent = coilCurCount = 0;
		int i;
		for (i = 0; i != poleCount; i++)
			switchCurrent[i] = switchCurCount[i] = 0;
	}

	public void stamp() {
		// inductor from coil post 1 to internal node
		ind.stamp(nodes[nCoil1], nodes[nCoil3]);
		// resistor from internal node to coil post 2
		sim.stampResistor(nodes[nCoil3], nodes[nCoil2], coilR);

		int i;
		for (i = 0; i != poleCount * 3; i++)
			sim.stampNonLinear(nodes[nSwitch0 + i]);
	}

	protected void startIteration() {
		ind.startIteration(volts[nCoil1] - volts[nCoil3]);

		// magic value to balance operate speed with reset speed semi-realistically
		double magic = 1.3;
		double pmult = Math.sqrt(magic + 1);
		double p = coilCurrent * pmult / onCurrent;
		d_position = Math.abs(p * p) - 1.3;
		if (d_position < 0)
			d_position = 0;
		if (d_position > 1)
			d_position = 1;
		if (d_position < .1)
			i_position = 0;
		else if (d_position > .9)
			i_position = 1;
		else
			i_position = 2;
		//System.out.println("ind " + this + " " + current + " " + voltdiff);
	}

	// we need this to be able to change the matrix for each step
	protected boolean nonLinear() {
		return true;
	}

	public void doStep() {
		double voltdiff = volts[nCoil1] - volts[nCoil3];
		ind.doStep(voltdiff);
		int p;
		for (p = 0; p != poleCount * 3; p += 3) {
			sim.stampResistor(nodes[nSwitch0 + p], nodes[nSwitch1 + p],
				i_position == 0 ? r_on : r_off);
			sim.stampResistor(nodes[nSwitch0 + p], nodes[nSwitch2 + p],
				i_position == 1 ? r_on : r_off);
		}
	}

	protected void calculateCurrent() {
		double voltdiff = volts[nCoil1] - volts[nCoil3];
		coilCurrent = ind.calculateCurrent(voltdiff);

		// actually this isn't correct, since there is a small amount
		// of current through the switch when off
		int p;
		for (p = 0; p != poleCount; p++) {
			if (i_position == 2)
				switchCurrent[p] = 0;
			else
				switchCurrent[p] =
					(volts[nSwitch0 + p * 3] - volts[nSwitch1 + p * 3 + i_position]) / r_on;
		}
	}

	public void getInfo(String arr[]) {
		arr[0] = i_position == 0 ? "relay (off)" :
		         i_position == 1 ? "relay (on)" : "relay";
		int i;
		int ln = 1;
		for (i = 0; i != poleCount; i++)
			arr[ln++] = "I" + (i + 1) + " = " + getCurrentDText(switchCurrent[i]);
		arr[ln++] = "coil I = " + getCurrentDText(coilCurrent);
		arr[ln++] = "coil Vd = " +
			getVoltageDText(volts[nCoil1] - volts[nCoil2]);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Inductance (H)", inductance, 0, 0);
		if (n == 1)
			return new EditInfo("On Resistance (ohms)", r_on, 0, 0);
		if (n == 2)
			return new EditInfo("Off Resistance (ohms)", r_off, 0, 0);
		if (n == 3)
			return new EditInfo("On Current (A)", onCurrent, 0, 0);
		if (n == 4)
			return new EditInfo("Number of Poles", poleCount, 1, 4).
				setDimensionless();
		if (n == 5)
			return new EditInfo("Coil Resistance (ohms)", coilR, 0, 0);
		if (n == 6) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Swap Coil Direction",
				(flags & FLAG_SWAP_COIL) != 0));
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0 && ei.getValue() > 0) {
			inductance = ei.getValue();
			ind.setup(inductance, coilCurrent, Inductor.FLAG_BACK_EULER);
		}
		if (n == 1 && ei.getValue() > 0)
			r_on = ei.getValue();
		if (n == 2 && ei.getValue() > 0)
			r_off = ei.getValue();
		if (n == 3 && ei.getValue() > 0)
			onCurrent = ei.getValue();
		if (n == 4 && ei.getValue() >= 1) {
			poleCount = (int) ei.getValue();
			setPoints();
		}
		if (n == 5 && ei.getValue() > 0)
			coilR = ei.getValue();
		if (n == 6) {
			if (ei.getCheckbox().getState())
				flags |= FLAG_SWAP_COIL;
			else
				flags &= ~FLAG_SWAP_COIL;
			setPoints();
		}
	}

	protected boolean getConnection(int n1, int n2) {
		return (n1 / 3 == n2 / 3);
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public int getShortcut() {
		return 'R';
	}
}
    
