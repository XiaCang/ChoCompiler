package core.compiler.utils;

import java.util.ArrayList;

public class BreakContinueTable {
    private ArrayList<Integer> breakPoints = new ArrayList<>();
    private ArrayList<Integer> continuePoints = new ArrayList<>();

    private BreakContinueTable outer = null;

    public BreakContinueTable() {}

    public BreakContinueTable(BreakContinueTable outer) { this.outer = outer; }

    public void addBreakPoint(int ip) { breakPoints.add(ip); }
    public void addContinuePoint(int ip) { continuePoints.add(ip); }

    public ArrayList<Integer> getBreakPoints() { return breakPoints; }
    public ArrayList<Integer> getContinuePoints() { return continuePoints; }

    public BreakContinueTable outer() { return outer; }
}
