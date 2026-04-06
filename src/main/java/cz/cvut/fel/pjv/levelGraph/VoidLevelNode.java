package cz.cvut.fel.pjv.levelGraph;

public class VoidLevelNode extends LevelNode {

    /// VoidLevelNode exists only because I had to add at least some Node to the LevelNodes in LevelGraph.java
    /// It should never be used for anything else
    public VoidLevelNode(int x, int y) {
        super(x, y);
    }
}
