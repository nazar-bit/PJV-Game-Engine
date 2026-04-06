package cz.cvut.fel.pjv.chunks;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.Utils;
import cz.cvut.fel.pjv.blocks.Block;
import cz.cvut.fel.pjv.entities.Entity;
import cz.cvut.fel.pjv.entities.WeaponEntity;
import cz.cvut.fel.pjv.managers.ChunkManager;


import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.Game.*;
import static cz.cvut.fel.pjv.managers.ChunkManager.*;


/**
 * The Chunk class is used to store info about chunk.
 *
 * <p>
 * It is responsible for storing blocks, entities, doors, and spawn positions.
 * The class handles reading chunk data from resources or external files.
 * </p>
 */

public class Chunk implements Serializable {
    private static final Logger logger = Logger.getLogger("Chunk");
    private final String chunkName;
    private final int id;
    private final int x, y; ///chunk coordinates in chunk grid
    private final ArrayList<Block> blocks = new ArrayList<>();
    private final ArrayList<Entity> entities = new ArrayList<>();
    private boolean isComplete = false;
    private Boolean[] doors = {false, false, false, false};/// 0 - north, 1 - east, 2 - south, 3 - west
    private final ArrayList<ChunkTags> chunkTags = new ArrayList<>();
    private final ArrayList<Utils.Coordinates> spawnPositions = new ArrayList<>();


    /**
     * Constructs a new chunk instance using the provided parameters.
     *
     * @param chunkName the base name for the chunk file.
     * @param id the unique identifier for the chunk.
     * @param x the x-coordinate location for the chunk on the grid.
     * @param y the y-coordinate location for the chunk on the grid.
     * @param generate flag indicating whether entities should be generated while loading.
     */
    public Chunk( String chunkName, int id, int x, int y, boolean generate) {
        this.chunkName = chunkName;
        this.id = id;
        this.x = x;
        this.y = y;
        setChunk(generate);
    }

    public String getChunkName() {
        return chunkName;
    }

    public int getId() {
        return id;
    }

    ///  Returns block at chunk coordinates x, y
    public Block getBlock(int x, int y) {
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE) {
            return blocks.get(y * CHUNK_SIZE + x);
        } else {
            logger.severe("Block out of chunk");
            return null;
        }
    }

    /// Returns block at index n
    public Block getBlock(int n) {
        if (n >= 0 && n < CHUNK_SIZE * CHUNK_SIZE) {
            return blocks.get(n);
        } else {
            logger.severe("Block out of chunk");
            return null;
        }
    }


    /// Reads chunk data from a file
    /// @param generate - if true, loads chunk with entities
    private void setChunk(boolean generate) {
        Scanner scanner = null;

        try {
            // Try loading from internal resources
            InputStream internalStream = getClass().getResourceAsStream(CHUNK_RESOURCE_PATH + chunkName + ".chunk");

            if (internalStream != null) {
                if(generate) logger.info("Loading chunk from internal resource: " + chunkName);
                scanner = new Scanner(internalStream);
            } else {
                // Fallback: Try loading from external file
                File externalFile = new File(CHUNK_EXTERNAL_PATH + "/" + chunkName + ".chunk");
                if (externalFile.exists()) {
                    logger.info("Loading chunk from external file: " + externalFile.getPath());
                    scanner = new Scanner(externalFile);
                } else {
                    logger.warning("Chunk not found: " + chunkName);
                    return;
                }
            }

            String option = "";
            while (!option.equals("[END]") && scanner.hasNextLine()) {
                option = scanner.nextLine();
                if (option.equals("[BLOCKS]")) {
                    setBlockList(scanner);
                } else if(option.equals("[DOORS]")){
                    String line = "";
                    while (scanner.hasNextLine() && !(line = scanner.nextLine()).equals("[END-DOORS]")) {
                        if (line.equals("north")) doors[0] = true;
                        else if (line.equals("east")) doors[1] = true;
                        else if (line.equals("south")) doors[2] = true;
                        else if (line.equals("west")) doors[3] = true;
                    }
                } else if (option.equals("[ENTITIES]")) {
                    String line = "";
                    while (scanner.hasNextLine() && !(line = scanner.nextLine()).equals("[END-ENTITIES]")) {
                        String[] parts = line.split(" ");
                        String entityName = parts[0];
                        double entityX = Double.parseDouble(parts[1]);
                        double entityY = Double.parseDouble(parts[2]);
                        if (entityX < 0 || entityX >= CHUNK_SIZE || entityY < 0 || entityY >= CHUNK_SIZE) {
                            logger.warning("Entity coordinates out of chunk bounds: " + entityName);
                            continue;
                        }

                        List<Object> params = new ArrayList<>(); params.add(entityX + x * CHUNK_SIZE);
                        params.add(entityY + y * CHUNK_SIZE);



                        for (int i = 3; i < parts.length; i++) {
                            try {
                                params.add(Double.parseDouble(parts[i]));
                            } catch (NumberFormatException e) {
                                if (parts[i].equalsIgnoreCase("true") || parts[i].equalsIgnoreCase("false")) {
                                    params.add(Boolean.parseBoolean(parts[i]));
                                } else {
                                    params.add(parts[i]);
                                }
                            }
                        }

                        try {
                            if (generate){
                                Entity entity = entityManager.createEntity(entityName, params.toArray());
                                entities.add(entity);
                                logger.info("Entity " + entityName + " created in chunk " + id + " at (" + entity.getX() + ", " + entity.getY() + ")");
                            }

                        } catch (IllegalArgumentException e) {
                            logger.warning("Error creating entity " + entityName + ": " + e.getMessage());
                        }
                    }
                } else if (option.equals("[OPTIONS]")) {
                    String line;
                    while (scanner.hasNextLine() && !(line = scanner.nextLine()).equals("[END-OPTIONS]")) {
                        if (line.equals("[spawn]")){
                            chunkTags.add(ChunkTags.SPAWN);
                            while (scanner.hasNextLine() && !(line = scanner.nextLine()).equals("[end-spawn]")) {
                                String[] parts = line.split(" ");
                                double x = Double.parseDouble(parts[0]);
                                double y = Double.parseDouble(parts[1]);
                                spawnPositions.add(new Utils.Coordinates(x , y ));
                            }
                        }
                        if (line.equals("exit")){
                            chunkTags.add(ChunkTags.EXIT);
                        }
                    }
                }
            }
            if((doors[0] || doors[1] || doors[2] || doors[3]) && blocks.size() == CHUNK_SIZE * CHUNK_SIZE){
                isComplete = true;
            }

        } catch (Exception e) {
            logger.severe("An error occurred while loading chunk: " + chunkName);
            e.printStackTrace();
            logger.severe("Chunk " + chunkName + " is not complete");
            isComplete = false;
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        if (spawnPositions.isEmpty()) {
            spawnPositions.add(new Utils.Coordinates(CHUNK_SIZE / 2, CHUNK_SIZE / 2));
            spawnPositions.add(new Utils.Coordinates(CHUNK_SIZE / 2 - 1, CHUNK_SIZE  / 2 - 1));
        }else if (spawnPositions.size() == 1){
            spawnPositions.add(new Utils.Coordinates(spawnPositions.get(0).x + 1, spawnPositions.get(0).y + 1));
        }

    }


    /// Sets blocks in chunk from scanner
    public void setBlockList(Scanner scanner) {
        for (int i = 0; i < CHUNK_SIZE * CHUNK_SIZE; i++) {
            Block block;
            if (scanner.hasNextInt()){
                int blockId = scanner.nextInt();
                block = blockManager.getBlockById(blockId);
                if (block == null) {
                    logger.warning("Block not found, VoidBlock added");
                    block = blockManager.getBlockById(0);
                }
            } else {
                logger.warning("No more blocks found in chunk, VoidBlock added");
                block = blockManager.getBlockById(0);
            }

            blocks.add(block);

        }
    }


    /// Returns Boolean array of doors
    public Boolean[] getDoors() {
        return doors;
    }

    /// Tells if the chunk is complete (has all blocks and doors)
    public boolean isComplete() {
        return isComplete;
    }

    public ArrayList<ChunkTags> getChunkTags() {
        return  chunkTags;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ArrayList<Utils.Coordinates> getSpawnPositions() {
        return spawnPositions;
    }
}
