package external.simulator.parts;

public class ACVoltageElm extends VoltageElm {
	public ACVoltageElm(int xx, int yy) {
		super(xx, yy, WF_AC);
	}

	protected Class<VoltageElm> getDumpClass() {
		return VoltageElm.class;
	}
}
