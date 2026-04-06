package cz.cvut.fel.pjv.levelGraph;

import cz.cvut.fel.pjv.Level;

import java.util.*;

import static cz.cvut.fel.pjv.managers.ChunkManager.CHUNK_SIZE;
import static cz.cvut.fel.pjv.managers.TextureManager.logger;


public class LevelGraph {

    private List<LevelNode> levelNodes;
    private Map<LevelNode, List<LevelNode>> levelEdges;
    private int levelWidth;
    private int levelHeight;

    /**
     * LevelGraph represents a graph structure built on the game level.
     *
     * <p>
     * The graph is built from level data by converting each non-wall position into a LevelNode
     * and creating edges between adjacent nodes based on various criteria.
     * </p>
     */
    public LevelGraph(Level level) {
        if(level == null) {
            logger.severe("LevelManager cannot be null ::: Error when creating LevelGraph");
            throw new IllegalArgumentException("LevelManager cannot be null ::: Error when creating LevelGraph");
        }

        levelNodes = new ArrayList<>();
        levelEdges = new HashMap<>();
        levelWidth = level.getWidth() * CHUNK_SIZE;
        levelHeight = level.getHeight() * CHUNK_SIZE;

        createLevelGraph(level);
    }

    /**
     * Creates Level Graph
     * @param level
     **/
    private void createLevelGraph(Level level) {
        for(int y = 0; y < levelHeight; ++y){
            for(int x = 0; x < levelWidth; ++x){

                if(level.isWall(x, y)){
                    levelNodes.add(new VoidLevelNode(x, y));
                    if(level.isInLevel(x-1, y) && !level.isWall(x-1, y))
                    {
                        if(!levelEdges.get(getLevelNode(x-1, y)).isEmpty()) {
                            levelEdges.get(getLevelNode(x-1, y)).remove(levelEdges.get(getLevelNode(x-1, y)).size()-1);
                        }
                    }

                    if(level.isInLevel(x, y-1) && !level.isWall(x, y-1))
                    {
                        if(!levelEdges.get(getLevelNode(x, y-1)).isEmpty()) {
                            levelEdges.get(getLevelNode(x, y-1)).remove(levelEdges.get(getLevelNode(x, y-1)).size()-1);
                        }
                    }
                    continue;
                }

                /// The boolean values below decide whether the TopLEft or TopRight Nodes will be connected to the current Node
                boolean topRight = true, topLeft = true;
                levelNodes.add(new LevelNode(x, y));
                levelEdges.put(levelNodes.get(levelNodes.size() - 1), new ArrayList<>());


                /// Checks if the block above is InLevel and NotWall and then adds edges if the condition is met
                /// Also if the condition is not met then do not connect topRight nor topLeft
                if(level.isInLevel(x, y-1) && !(level.isWall(x, y-1))){
                   levelEdges.get(levelNodes.get(levelNodes.size()-1)).add(getLevelNode(x, y - 1));
                   levelEdges.get(getLevelNode(x, y - 1)).add(levelNodes.get(levelNodes.size()-1));
                } else {
                    topRight = false;
                    topLeft = false;
                }

                /// Checks if the block to the left is InLevel and NotWall and then adds edges if the condition is met
                /// Also if the condition is not met then do not connect topLeft
                if(level.isInLevel(x-1, y) && !(level.isWall(x-1, y))){
                    levelEdges.get(levelNodes.get(levelNodes.size()-1)).add(getLevelNode(x-1, y));
                    levelEdges.get(getLevelNode(x-1, y)).add(levelNodes.get(levelNodes.size()-1));
                } else{
                    topLeft = false;
                }


                /// Checks if the block above and to the left is InLevel and NotWall and then adds edges if the condition is met
                /// Or if it was not decided before that this edge does not belong to the graph
                if(topLeft && level.isInLevel(x-1, y-1) && !(level.isWall(x-1, y-1))){
                    levelEdges.get(levelNodes.get(levelNodes.size()-1)).add(getLevelNode(x-1, y-1));
                    levelEdges.get(getLevelNode(x-1, y-1)).add(levelNodes.get(levelNodes.size()-1));
                }


                /// Checks if the block above and to the right is InLevel and NotWall and then adds edges if the condition is met
                /// Or if it was not decided before that this edge does not belong to the graph
                if(topRight && level.isInLevel(x+1, y-1) && !(level.isWall(x+1, y-1))){
                    levelEdges.get(levelNodes.get(levelNodes.size()-1)).add(getLevelNode(x+1, y-1));
                    levelEdges.get(getLevelNode(x+1, y-1)).add(levelNodes.get(levelNodes.size()-1));
                }
            }
        }
    }


    private LevelNode getLevelNode(int x, int y) {
        return levelNodes.get(y * levelWidth + x);
    }

    /** Finds Path
     * @param start
     * @param end
     * @return path
     */
    public List<LevelNode> findPath(LevelNode start, LevelNode end) {
        List<LevelNode> path = new ArrayList<>();
        List<LevelNode> visited = new ArrayList<>();
        Map<LevelNode, LevelNode> parents = new HashMap<>();
        Queue<LevelNode> pq = new PriorityQueue<>(Comparator.comparingDouble(LevelNode::getWeight));
        LevelNode current = start;


        if(current.equals(end)) {
            return path;
        }


        while(!(current.equals(end))){     /// Checks if found end
            if(!visited.contains(current)) {   /// Checks if was not visited
                for (LevelNode levelNode : levelEdges.get(current)) {   /// Takes every Node with common Edge
                    if (visited.contains(levelNode) || levelNode instanceof VoidLevelNode) continue;          ///Checks if Node was visited
                    if (!parents.containsKey(levelNode)) {
                        levelNode.setWeight(calculateWeight(levelNode, end));
                        parents.put(levelNode, current);
                        pq.add(levelNode);
                    }
                }
            }
            visited.add(current);
            current = pq.poll();

            if(pq.isEmpty()){
                return null;
            }
        }

        while(current != start){
            path.add(current);
            current = parents.get(current);
        }

        return path;
    }


    private double calculateWeight(LevelNode a, LevelNode b) {
        return Math.hypot(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
    }

}
