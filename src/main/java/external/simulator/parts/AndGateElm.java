package external.simulator.parts;

import org.lwjgl.util.Point;

import java.util.StringTokenizer;

public class AndGateElm extends GateElm {
	public AndGateElm(int xx, int yy) {
		super(xx, yy);
	}

	public AndGateElm(int xa, int ya, int xb, int yb, int f,
	                  StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
	}

	protected void setPoints() {
		super.setPoints();

		// 0=topleft, 1-10 = top curve, 11 = right, 12-21=bottom curve,
		// 22 = bottom left
		Point triPoints[] = newPointArray(23);
		interpPoint2(lead1, lead2, triPoints[0], triPoints[22], 0, hs2);
		int i;
		for (i = 0; i != 10; i++) {
			double a = i * .1;
			double b = Math.sqrt(1 - a * a);
			interpPoint2(lead1, lead2,
				triPoints[i + 1], triPoints[21 - i],
				.5 + a / 2, b * hs2);
		}
		triPoints[11] = new Point(lead2);
		if (isInverting()) {
			pcircle = interpPoint(point1, point2, .5 + (ww + 4) / dn);
			lead2 = interpPoint(point1, point2, .5 + (ww + 8) / dn);
		}
		gatePoly = createPolygon(triPoints);
	}

	public String getGateName() {
		return "AND gate";
	}

	public boolean calcFunction() {
		int i;
		boolean f = true;
		for (i = 0; i != inputCount; i++)
			f &= getInput(i);
		return f;
	}

	public int getDumpType() {
		return 150;
	}

	public int getShortcut() {
		return '2';
	}
}