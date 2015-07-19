package external.simulator.Simulator.parts;

public class CC2NegElm extends CC2Elm {
    public CC2NegElm(int xx, int yy) {
        super(xx, yy, -1);
    }

    protected Class getDumpClass() {
        return CC2Elm.class;
    }
}
