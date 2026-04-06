package cz.cvut.fel.pjv.managers;

import cz.cvut.fel.pjv.chunks.Chunk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static cz.cvut.fel.pjv.Utils.createBlocksFile;

/**
 * ChunkManager is responsible for managing chunks in the game.
 *
 * <p>
 * It loads chunk files from both internal (packaged) resources and external storage,
 * assigns unique IDs to chunks, validates their completeness, and provides utility methods
 * for retrieving chunks based on IDs or specific characteristics. This class also saves
 * an updated list of chunks to an external file.
 * </p>
 */
public class ChunkManager {
    private static final Logger logger = Logger.getLogger("Game");
    public static final int CHUNK_SIZE = 16;
    public static final String CHUNK_RESOURCE_PATH = "/chunks/";
    public static final String CHUNK_EXTERNAL_PATH = "chunks";

    private static final Map<Integer, ChunkCharacteristics> chunkRegistry = new HashMap<>();
    private static final Map<String, Boolean[]> chunkDoors = new HashMap<>();
    private static BlockManager blockManager;

    /// Chunks tags
    public enum ChunkTags {
        SPAWN,
        EXIT,
    }

    /**
     * ChunkCharacteristics holds details about each chunk.
     */
    public static class ChunkCharacteristics {
        public int id;
        public String name;
        public Boolean[] doors;
        public ArrayList<ChunkTags> chunkTags;

        public ChunkCharacteristics(int id, String name, Boolean[] doors, ArrayList<ChunkTags> chunkTypes) {
            this.id = id;
            this.name = name;
            this.doors = doors;
            this.chunkTags = chunkTypes;
        }
    }

    /**
     * Constructs a new ChunkManager, loading chunks from both internal resources and the external folder.
     *
     * <p>
     * It verifies that an external directory exists (or creates it), loads chunk information from
     * internal JAR resources or source directories, merges these with external chunks,
     * and assigns unique IDs to new chunks.
     * </p>
     *
     * @param blockManager the BlockManager instance.
     */
    public ChunkManager(BlockManager blockManager) {

        this.blockManager = blockManager;
        int lastChunkId = 0;
        Queue<Integer> freeIds = new LinkedList<>();

        File externalDir = new File(CHUNK_EXTERNAL_PATH);
        if (!externalDir.exists()) {
            externalDir.mkdirs();
            logger.info("Created external chunks directory.");
        }
        createBlocksFile(CHUNK_EXTERNAL_PATH);
        File savedChunksFile = new File(externalDir, "chunks.list");

        //Collect chunks from internal (in-JAR) resources
        Set<String> internalChunks = new HashSet<>();
        try {
            String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            if (jarFile.isFile()) {
                try (JarFile jf = new JarFile(jarFile)) {
                    jf.stream()
                            .filter(e -> e.getName().startsWith("chunks/") && e.getName().endsWith(".chunk"))
                            .forEach(e -> internalChunks.add(new File(e.getName()).getName()));
                }
            } else {
                Path chunksDir = Paths.get("src/main/resources/chunks");
                if (Files.exists(chunksDir)) {
                    try (Stream<Path> paths = Files.walk(chunksDir)) {
                        paths.filter(Files::isRegularFile)
                                .filter(p -> p.toString().endsWith(".chunk"))
                                .map(p -> p.getFileName().toString().replace(".chunk", ""))
                                .forEach(internalChunks::add);
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to load internal chunks: " + e.getMessage());
        }


        //Collect chunks from the external folder
        Set<String> externalChunks = new HashSet<>();
        try (Stream<Path> paths = Files.walk(Paths.get(CHUNK_EXTERNAL_PATH))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".chunk"))
                    .map(p -> p.getFileName().toString().replace(".chunk", ""))
                    .forEach(externalChunks::add);
        } catch (IOException e) {
            logger.warning("Failed to load external chunks: " + e.getMessage());
            e.printStackTrace();
        }

        // Combine both sets (external takes precedence)
        LinkedHashSet<String> allChunks = new LinkedHashSet<>();
        allChunks.addAll(internalChunks);
        allChunks.addAll(externalChunks);


        //Read existing chunks.list if exists
        if (savedChunksFile.exists()) {
            try (Scanner scanner = new Scanner(savedChunksFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(" ");
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];

                    Chunk chunk = new Chunk(name, 0, 0, 0, false);
                    if(!chunk.isComplete() && !chunk.getChunkName().equals("void")) {
                        //delete chunk from set
                        logger.warning("Chunk " + name + " is not complete. Deleting...");
                        continue;
                    }

                    chunkRegistry.put(id, new ChunkCharacteristics(id, name, chunk.getDoors(), chunk.getChunkTags()));
                    lastChunkId = id;
                }
            } catch (IOException e) {
                logger.warning("Failed to load chunks.list: " + e.getMessage());
                e.printStackTrace();
            }
        }
        //Assign IDs to new chunks
        for (String chunkFile : allChunks) {
            Chunk chunk = new Chunk(chunkFile, 0, 0, 0, false);
            if (!isChunkInRegistry(chunkFile) && chunk.isComplete()) {
                int newId = ++lastChunkId;
                chunkRegistry.put(newId, new ChunkCharacteristics(newId, chunkFile, chunk.getDoors(), chunk.getChunkTags()));
            }
        }
        Boolean [] voidDoors = {false, false, false, false};
        chunkRegistry.put(0, new ChunkCharacteristics(0, "void", voidDoors, new ArrayList<>()));
        //Save an updated chunk list to an external folder
        try (FileWriter writer = new FileWriter(savedChunksFile)) {
            for (Map.Entry<Integer, ChunkCharacteristics> entry : chunkRegistry.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue().name + "\n");
            }
        } catch (IOException e) {
            logger.warning("Failed to save chunks.list: " + e.getMessage());
            e.printStackTrace();
        }

        StringBuilder msg = new StringBuilder("ChunkManager initialized with " + chunkRegistry.size() + " chunks:");
        int i = 0;
        for (int ID : chunkRegistry.keySet()) {
            if (i % 9 == 0) {
                msg.delete(msg.length() - 1, msg.length());
                msg.append("\n\t");
            }
            msg.append(chunkRegistry.get(ID).name).append("(ID: ").append(ID).append("), ");
            i++;
        }
        msg.delete(msg.length() - 2, msg.length());
        logger.info(msg.toString());
    }

    /**
     * Retrieves a new Chunk instance based on the provided ID and coordinates.
     *
     * @param ID the unique identifier of the chunk.
     * @param x  the x-coordinate where the chunk is located.
     * @param y  the y-coordinate where the chunk is located.
     * @return a Chunk instance if found; if not, returns a void chunk with a warning.
     */
    public Chunk getChunk(int ID, int x, int y){

        if (chunkRegistry.get(ID) != null) {
            return new Chunk(chunkRegistry.get(ID).name, ID, x, y, true);
        } else {
            logger.warning("Chunk with ID " + ID + " not found. Returning void chunk.");
            return new Chunk("void", ID, x, y, true);
        }

    }

    public static Map<String, Boolean[]> getChunkDoors() {
        return chunkDoors;
    }


    /// Returns array of chunk Characteristics by tags
    public static ArrayList<ChunkCharacteristics> getChunkIDsByTag(ChunkTags tag) {
        ArrayList<ChunkCharacteristics> chunkIDs = new ArrayList<>();
        for (Map.Entry<Integer, ChunkCharacteristics> entry : chunkRegistry.entrySet()) {
            if (entry.getValue().chunkTags.contains(tag)) {
                chunkIDs.add(entry.getValue());
            }
        }
        return chunkIDs;
    }

    /// Returns array of chunk Characteristics by doors
    public static ArrayList<ChunkCharacteristics> getChunkCharByDoors(Boolean[] doors) {
        ArrayList<ChunkCharacteristics> chunkIDs = new ArrayList<>();

        for (Map.Entry<Integer, ChunkCharacteristics> entry : chunkRegistry.entrySet()) {
            if (Arrays.equals(entry.getValue().doors, doors)) {
                chunkIDs.add(entry.getValue());
            }
        }
        return chunkIDs;
    }

    private static boolean isChunkInRegistry(String chunkName) {
        for (Map.Entry<Integer, ChunkCharacteristics> entry : chunkRegistry.entrySet()) {
            if (entry.getValue().name.equals(chunkName)) {
                return true;
            }
        }
        return false;
    }
}
