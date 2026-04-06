package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.chunks.Chunk;
import cz.cvut.fel.pjv.managers.ChunkManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.managers.ChunkManager.getChunkCharByDoors;
import static cz.cvut.fel.pjv.managers.ChunkManager.getChunkIDsByTag;

/**
 * LevelGenerator is responsible for procedurally generating game levels.
 *
 * <p>
 * It creates an abstract representation of the level layout represented as a grid
 * of "chunks" with defined door connections. The generator then converts this
 * abstract map into an actual Level object populated with concrete chunks.
 * </p>
 */
public class LevelGenerator {
    private static final Logger logger = Logger.getLogger("LevelGen");
    /**
     * ChunkToPlace encapsulates the position and door configuration for a chunk placement.
     */
    public static class ChunkToPlace {
        int x;
        int y;
        Boolean doors[] = {false, false, false, false};/// 0 - north, 1 - east, 2 - south, 3 - west

        /**
         * Constructs a ChunkToPlace with specified coordinates and door configuration.
         *
         * @param x the x-coordinate in the grid.
         * @param y the y-coordinate in the grid.
         * @param doors an array indicating door presence in each direction.
         */
        public ChunkToPlace(int x, int y, Boolean[] doors) {
            this.x = x;
            this.y = y;
            this.doors = doors;
        }
    }


    /**
     * Generates a level with the specified dimensions and path length.
     *
     * <p>
     * The method creates an abstract map, assigns chunks based on door configurations,
     * and prints the generated map to the console.
     * </p>
     *
     * @param chunkManager the manager to retrieve concrete chunks.
     * @param width the horizontal number of chunks.
     * @param height the vertical number of chunks.
     * @param pathLength the number of chunks in the main path.
     * @return a Level object representing the generated level.
     */
    public static Level generateLevel(ChunkManager chunkManager, int width, int height, int pathLength) {
        AbstractLevel abstractLevel = generateAbstractMap(width, height, pathLength);


        ArrayList<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < abstractLevel.mapOfChunks.size(); i++) {
            for (int j = 0; j < abstractLevel.mapOfChunks.get(i).size(); j++) {
                if (abstractLevel.mapOfChunks.get(i).get(j) != null) {
                    ChunkToPlace chunk = abstractLevel.mapOfChunks.get(i).get(j);
                    ArrayList<ChunkManager.ChunkCharacteristics> chunkIDs;
                    if (i == abstractLevel.path.get(0).y && j == abstractLevel.path.get(0).x){
                        chunkIDs = getChunkIDsByTag(ChunkManager.ChunkTags.SPAWN);
                        chunkIDs.removeIf(chunkID -> !Arrays.equals(chunkID.doors, chunk.doors));
                    }else if (i == abstractLevel.path.get(abstractLevel.path.size() - 1).y && j == abstractLevel.path.get(abstractLevel.path.size() - 1).x){
                        chunkIDs = getChunkIDsByTag(ChunkManager.ChunkTags.EXIT);
                        chunkIDs.removeIf(chunkID -> !Arrays.equals(chunkID.doors, chunk.doors));
                    }else {
                        chunkIDs = getChunkCharByDoors(chunk.doors);
                        chunkIDs.removeIf(chunkID -> chunkID.chunkTags.contains(ChunkManager.ChunkTags.SPAWN)
                                || chunkID.chunkTags.contains(ChunkManager.ChunkTags.EXIT));
                    }
                    if (chunkIDs.isEmpty()) {
                        chunks.add(chunkManager.getChunk(0, j, i));
                    } else {
                        ChunkManager.ChunkCharacteristics chunkID = chunkIDs.get((int) (Math.random() * chunkIDs.size()));
                        chunks.add(chunkManager.getChunk(chunkID.id, j, i));
                    }
                }else{
                    chunks.add(chunkManager.getChunk(0, j, i));
                }
            }
        }



        printMap(abstractLevel.mapOfChunks, abstractLevel.path);

        return new Level(chunks, width, height);

    }

    /**
     * Generates an abstract map of chunks that outlines the level layout.
     *
     * <p>
     * It creates a valid path of chunks with possible additional branches based on a set path length.
     * The abstract map consists of a grid mapping each chunk's position and door connections.
     * </p>
     *
     * @param width the horizontal dimension of the map.
     * @param height the vertical dimension of the map.
     * @param pathLength the desired length of the main path.
     * @return an AbstractLevel object that holds the main path and complete map.
     */
    public static AbstractLevel generateAbstractMap(int width, int height, int pathLength) {
        List<ChunkToPlace> path = new ArrayList<>();
        Set<ChunkToPlace> visited = new java.util.HashSet<>();
        ArrayList<ArrayList<ChunkToPlace>> mapOfChunks = new ArrayList<>();
        List<List<ChunkToPlace>> branches = new ArrayList<>();

        boolean generate = true;

        while (generate) {
            generate = false;
            path.clear();
            visited.clear();
            mapOfChunks.clear();
            for (int i = 0; i < height; i++) {
                mapOfChunks.add(new ArrayList<>());
                for (int j = 0; j < width; j++) {
                    mapOfChunks.get(i).add(null);
                }
            }
            ChunkToPlace start = placeChunkRandomly(width, height, mapOfChunks);

            path.add(start);
            mapOfChunks.get(start.y).set(start.x, start);
            visited.add(start);


            for (int i = 1; i < pathLength; i++) {
                generate = !placeNextChunk(width, height, path, mapOfChunks, i == pathLength - 1);
            }

        }


        for (int i = 1; i < path.size() - 1; i++) {
            ChunkToPlace chunk = path.get(i);
            if(Math.random() < 0.2 && generateDoor(chunk.x, chunk.y, width, height, mapOfChunks, chunk.doors)) {
                ArrayList<ChunkToPlace> branch = new ArrayList<>();
                branch.add(chunk);
                branches.add(branch);
                for (int j = 0; j < 7; j++) {
                    if (placeNextChunk(width, height, branch, mapOfChunks, j == 6)) {
                        visited.add(branch.get(branch.size() - 1));
                    } else {
                        break;
                    }
                }
            }
        }
        AbstractLevel abstractLevel = new AbstractLevel(path, mapOfChunks);
        return abstractLevel;
    }

    /// Contains info on what doors the individual chunks have, without any information on chunks
    public static class AbstractLevel {
        public final List<ChunkToPlace> path;
        public final ArrayList<ArrayList<ChunkToPlace>> mapOfChunks;

        public AbstractLevel(List<ChunkToPlace> path, ArrayList<ArrayList<ChunkToPlace>> mapOfChunks) {
            this.path = path;
            this.mapOfChunks = mapOfChunks;
        }
    }


    /// places the chunk randomly in the map
    private static ChunkToPlace placeChunkRandomly(int width, int height, ArrayList<ArrayList<ChunkToPlace>> mapOfChunks) {
        int x = (int) (Math.random() * width);
        int y = (int) (Math.random() * height);


        Boolean[] doors = {false, false, false, false};
        generateDoor(x, y, width, height, mapOfChunks, doors);
        return new ChunkToPlace(x, y, doors);
    }


    /// generates a door chunk in the chunk to a random neighbor
    private static boolean generateDoor(int x, int y, int width, int height, ArrayList<ArrayList<ChunkToPlace>> mapOfChunks, Boolean[] doors) {
        Boolean[] possibleNewDoors = {false, false, false, false};/// 0 - north, 1 - east, 2 - south, 3 - west
        if (x >= 0 && y >= 0 && x < width && y < height) {
            if (!doors[0] && y-1 >= 0 && (mapOfChunks.get(y-1).get(x) == null || mapOfChunks.get(y-1).get(x).doors[2])) {
                possibleNewDoors[0] = true;
            }
            if (!doors[1] && x+1 < width && (mapOfChunks.get(y).get(x+1) == null || mapOfChunks.get(y).get(x+1).doors[3])) {
                possibleNewDoors[1] = true;
            }
            if (!doors[2] && y+1 < height && (mapOfChunks.get(y+1).get(x) == null || mapOfChunks.get(y+1).get(x).doors[0])) {
                possibleNewDoors[2] = true;
            }
            if (!doors[3] && x-1 >= 0 && (mapOfChunks.get(y).get(x-1) == null || mapOfChunks.get(y).get(x-1).doors[1])) {
                possibleNewDoors[3] = true;
            }
        }
        if (!possibleNewDoors[0] && !possibleNewDoors[1] && !possibleNewDoors[2] && !possibleNewDoors[3]) {
            return false;
        }
        int randomDoor = (int) (Math.random() * 4);
        while (!possibleNewDoors[randomDoor]) {
            randomDoor = (int) (Math.random() * 4);
        }
        doors[randomDoor] = true;
        return true;
    }


    /// places the next chunk in the path
    private static boolean placeNextChunk(int width, int height, List<ChunkToPlace> path, ArrayList<ArrayList<ChunkToPlace>> mapOfChunks, boolean isEnd ) {
        ChunkToPlace lastChunk = path.get(path.size() - 1);
        int x = lastChunk.x;
        int y = lastChunk.y;
        Boolean[] doors = lastChunk.doors;

        List<ChunkToPlace> possibleChunks = new ArrayList<>();

        if (doors[0] && y-1 >= 0 && mapOfChunks.get(y-1).get(x) == null) {
            Boolean[] newDoors = {false, false, true, false};
            if(!isEnd) generateDoor(x, y-1, width, height, mapOfChunks, newDoors);
            possibleChunks.add(new ChunkToPlace(x, y-1, newDoors));
        }
        if (doors[1] && x+1 < width && mapOfChunks.get(y).get(x+1) == null) {
            Boolean[] newDoors = {false, false, false, true};
            if(!isEnd) generateDoor(x+1, y, width, height, mapOfChunks, newDoors);
            possibleChunks.add(new ChunkToPlace(x+1, y, newDoors));
        }
        if (doors[2] && y+1 < height && mapOfChunks.get(y+1).get(x) == null) {
            Boolean[] newDoors = {true, false, false, false};
            if(!isEnd) generateDoor(x, y+1, width, height, mapOfChunks, newDoors);
            possibleChunks.add(new ChunkToPlace(x, y+1, newDoors));
        }
        if (doors[3] && x-1 >= 0 && mapOfChunks.get(y).get(x-1) == null) {
            Boolean[] newDoors = {false, true, false, false};
            if(!isEnd) generateDoor(x-1, y, width, height, mapOfChunks,newDoors);
            possibleChunks.add(new ChunkToPlace(x-1,y,newDoors));
        }

        if (possibleChunks.isEmpty()) {
            return false;
        } else {
            ChunkToPlace nextChunk = possibleChunks.get((int) (Math.random() * possibleChunks.size()));
            path.add(nextChunk);
            mapOfChunks.get(nextChunk.y).set(nextChunk.x,nextChunk);
            return true;
        }
    }

    /// prints the map of chunks to the console
    public static void printMap(ArrayList<ArrayList<ChunkToPlace>> mapOfChunks, List<ChunkToPlace> path) {
        StringBuilder msg = new StringBuilder("Map of chunks:\n");
        for (int i = 0; i < mapOfChunks.size() * 3; i++) {
            for (int j = 0; j < mapOfChunks.get(i/3).size() * 3 ; j++) {
                if (mapOfChunks.get(i/3).get(j/3) != null) {
                    if (i%3 == 0 && j%3 == 1 && mapOfChunks.get(i/3).get(j/3).doors[0]) {
                        msg.append(" | ");
                    } else if (i%3 == 1 && j%3 == 0 && mapOfChunks.get(i/3).get(j/3).doors[3]) {
                        msg.append(" - ");
                    } else if (i%3 == 1 && j%3 == 2 && mapOfChunks.get(i/3).get(j/3).doors[1]) {
                        msg.append(" - ");
                    } else if (i%3 == 2 && j%3 == 1 && mapOfChunks.get(i/3).get(j/3).doors[2]) {
                        msg.append(" | ");
                    } else if (i%3 == 1 && j%3 == 1 ){
                        if (path.get(path.size() - 1).x == j/3 && path.get(path.size() - 1).y == i/3){
                            msg.append(" E ");
                        }else if (path.get(0).x == j/3 && path.get(0).y == i/3){
                            msg.append(" S ");
                        }else msg.append(" # ");


                    } else {
                        msg.append("   ");
                    }
                }
                else {
                    msg.append(" 8 ");
                }
            }
            msg.append("\n");
        }
        logger.info(msg.toString());
    }
}
