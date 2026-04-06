package cz.cvut.fel.pjv.levelGraph;

import java.util.Objects;

/**
 * LevelNode represents a single node in the level graph.
 *
 * <p>
 * Each LevelNode stores its x and y coordinates in the level and a weight value used for
 * pathfinding algorithms.
 * </p>
 */
public class LevelNode {

    private int x;
    private int y;
    private double weight;

    /**
     * Constructs a LevelNode with the specified coordinates.
     *
     * @param x the x-coordinate of the node.
     * @param y the y-coordinate of the node.
     */
    public LevelNode(int x, int y) {
        this.x = x;
        this.y = y;
        this.weight = 0;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LevelNode levelNode = (LevelNode) o;
        return x == levelNode.x && y == levelNode.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
