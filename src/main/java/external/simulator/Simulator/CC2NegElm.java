package external.simulator.Simulator;

class CC2NegElm extends CC2Elm {
    public CC2NegElm(int xx, int yy) {
        super(xx, yy, -1);
    }

    Class getDumpClass() {
        return CC2Elm.class;
    }
}
