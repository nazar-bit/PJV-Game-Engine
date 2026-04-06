package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.blocks.Block;
import cz.cvut.fel.pjv.blocks.VoidBlock;
import cz.cvut.fel.pjv.chunks.Chunk;
import cz.cvut.fel.pjv.entities.Player;
import cz.cvut.fel.pjv.managers.ChunkManager;
import cz.cvut.fel.pjv.managers.LevelManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.managers.LevelManager.LEVEL_RESOURCE_PATH;

/**
 * Level encapsulates the game level, including its layout, chunks, and spawn positions.
 *
 * <p>
 * It supports level loading from internal resources or external files, retrieving individual blocks,
 * and assigning spawn positions to the player.
 * </p>
 */
public class Level implements Serializable{
    ArrayList<Chunk> chunks = new ArrayList<>();
    ArrayList<Utils.Coordinates> spawnPositions = new ArrayList<>();
    private int width;
    private int height;
    private static final Logger logger = Logger.getLogger("Level");

    /**
     * Constructs a Level by loading level data from a file.
     *
     * <p>
     * It first attempts to load the level from internal resources. If not found, it falls back to an external file.
     * The level's dimensions and chunks are loaded from the file.
     * </p>
     *
     * @param levelName the name of the level file (without the file extension).
     * @param chunkManager the ChunkManager used to retrieve individual chunks.
     */
    public Level(String levelName, ChunkManager chunkManager) {
        InputStream input = null;
        Scanner scanner = null;

        try {
            // Try internal first
            input = getClass().getResourceAsStream(LEVEL_RESOURCE_PATH + levelName + ".level");// or adjust path
            File externalDir = new File("levels");
            if (!externalDir.exists()) {
                externalDir.mkdirs();
                logger.info("Created external level directory.");
            }

            if (input != null) {
                logger.info("Loaded level from internal resource: " + levelName);
                scanner = new Scanner(input);
            } else {
                // Fall back to external
                File externalFile = new File("levels/" + levelName + ".level");
                if (externalFile.exists()) {
                    logger.info("Loaded level from external file: " + externalFile.getPath());
                    scanner = new Scanner(externalFile);
                } else {
                    logger.warning("Level file not found: " + levelName);
                    return;
                }
            }

            width = scanner.nextInt();
            height = scanner.nextInt();

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int chunkId = scanner.nextInt();
                    Chunk chunk = chunkManager.getChunk(chunkId, j, i);

                    if (chunk != null) {
                        chunks.add(chunk);
                    } else {
                        logger.severe("Chunk with ID " +  chunkId + "at ( " + j + ", " + i +" ) could not be loaded.");
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("An error occurred while loading the level.");
            e.printStackTrace();
        } finally {
            if (scanner != null) scanner.close();
            if (input != null) try { input.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Constructs a Level using a pre-built list of chunks and explicit dimensions.
     *
     * @param chunks a list of chunks that make up the level.
     * @param width width of the level in chunks.
     * @param height height of the level in chunks.
     */
    public Level(ArrayList<Chunk> chunks, int width, int height) {
        this.chunks = chunks;
        this.width = width;
        this.height = height;

    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Retrieves the block located at the specified position in the level.
     *
     * <p>
     * The level is divided into chunks, and this method calculates the corresponding chunk and
     * block index within that chunk. If the position is outside the level bounds, a VoidBlock is returned.
     * </p>
     *
     * @param x the x-coordinate of the block.
     * @param y the y-coordinate of the block.
     * @return the Block at the given coordinates, or a VoidBlock if out-of-bounds.
     */
    public Block getBlock(int x, int y) {
        if (isInLevel(x, y)) {
            return chunks.get((y / ChunkManager.CHUNK_SIZE) * width + x / ChunkManager.CHUNK_SIZE).getBlock((y % ChunkManager.CHUNK_SIZE) * ChunkManager.CHUNK_SIZE + x % ChunkManager.CHUNK_SIZE);
        } else {
            return new VoidBlock();
        }


    }

    public boolean isInLevel(int x, int y) {
        return x >= 0 && x < width * ChunkManager.CHUNK_SIZE && y >= 0 && y < height * ChunkManager.CHUNK_SIZE;
    }

    public boolean isWall(int x, int y) {
        return getBlock(x, y).isWall();
    }

    /**
     * Sets the available player's spawn positions based on chunks tagged for spawning.
     *
     * <p>
     * Performs adjustments to the player's spawn coordinates based on the chunk's position in the level.
     * </p>
     */
    private void setSpawnPositions() {
        for (Chunk chunk : chunks) {
            if (chunk.getChunkTags().contains(ChunkManager.ChunkTags.SPAWN)) {

                spawnPositions = chunk.getSpawnPositions();
                for (Utils.Coordinates spawnPosition : spawnPositions) {
                    spawnPosition.x += chunk.getX() * ChunkManager.CHUNK_SIZE;
                    spawnPosition.y += chunk.getY() * ChunkManager.CHUNK_SIZE;
                }
                break;
            }
        }
        if (spawnPositions.isEmpty()) {
            Random rand = new Random();
            Chunk chunk = chunks.get(rand.nextInt(chunks.size()));
            spawnPositions = chunk.getSpawnPositions();
            for (Utils.Coordinates spawnPosition : spawnPositions) {
                spawnPosition.x += chunks.get(0).getX() * ChunkManager.CHUNK_SIZE;
                spawnPosition.y += chunks.get(0).getY() * ChunkManager.CHUNK_SIZE;
            }
        }
    }

    /**
     * Sets the player's spawn position using one of the available spawn positions.
     *
     * <p>
     * A random spawn position from the available ones is selected, the player's coordinates are set,
     * and the used spawn position is removed from the list.
     * </p>
     *
     * @param player the Player whose spawn position is to be set.
     */
    public void spawnPosition(Player player) {
        if (spawnPositions.isEmpty()) {
            setSpawnPositions();
        }
        Random rand = new Random();
        Utils.Coordinates spawnPosition = spawnPositions.get(rand.nextInt(spawnPositions.size()));
        player.setX(spawnPosition.x);
        player.setY(spawnPosition.y);
        logger.info("Player spawned at: " + spawnPosition.x + ", " + spawnPosition.y);
        spawnPositions.remove(spawnPosition);
        if (spawnPositions.isEmpty()) {
            logger.severe("No spawn positions left in level.");
        } else {
            logger.info("Spawn position removed, " + spawnPositions.size() + " left.");
        }
    }
}