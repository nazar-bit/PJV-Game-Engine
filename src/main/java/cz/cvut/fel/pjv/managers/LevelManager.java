package cz.cvut.fel.pjv.managers;

import cz.cvut.fel.pjv.Level;
import cz.cvut.fel.pjv.LevelGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Stream;


/**
 * LevelManager is responsible for discovering and loading level files.
 *
 * <p>It scans for files with a ".level" extension and maintains a list
 * of available level names. When asked to load a level, it either returns the existing
 * level or generates a new level if the requested one is not found.</p>
 */
public class LevelManager {
    private static final Logger logger = Logger.getLogger("LevelManager");
    ArrayList<String> levelNames = new ArrayList<>();
    public static final String LEVEL_RESOURCE_PATH = "/levels/";
    public static final String LEVEL_EXTERNAL_PATH = "levels";

    /**
     * Creates a new LevelManager instance. It scans both the internal levels within
     * the packaged jar or resources path and the external levels in the file system.
     *
     * <p>The discovered level names are stored in a list. A log message is then printed
     * showing the number and names of loaded levels.</p>
     */
    public LevelManager() {
        File externalDir = new File(LEVEL_EXTERNAL_PATH);
        if (!externalDir.exists()) {
            externalDir.mkdirs();
            logger.info("Created external chunks directory.");
        }

        Set<String> internalLevels = new HashSet<>();
        try {
            String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            if (jarFile.isFile()) {
                try (JarFile jf = new JarFile(jarFile)) {
                    jf.stream()
                            .filter(e -> e.getName().startsWith("levels/") && e.getName().endsWith(".level"))
                            .forEach(e -> internalLevels.add(new File(e.getName()).getName()));
                }
            } else {
                Path chunksDir = Paths.get("src/main/resources/levels");
                if (Files.exists(chunksDir)) {
                    try (Stream<Path> paths = Files.walk(chunksDir)) {
                        paths.filter(Files::isRegularFile)
                                .filter(p -> p.toString().endsWith(".level"))
                                .map(p -> p.getFileName().toString())
                                .forEach(internalLevels::add);
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to load internal chunks: " + e.getMessage());
        }


        Set<String> externalLevels = new HashSet<>();
        try (Stream<Path> paths = Files.walk(Paths.get(LEVEL_EXTERNAL_PATH))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".level"))
                    .map(p -> p.getFileName().toString())
                    .forEach(externalLevels::add);
        } catch (IOException e) {
            logger.warning("Failed to load external chunks: " + e.getMessage());
            e.printStackTrace();
        }

        LinkedHashSet<String> allLevels = new LinkedHashSet<>();
        allLevels.addAll(internalLevels);
        allLevels.addAll(externalLevels);


        for (String level : allLevels) {
            if (!levelNames.contains(level)) {
                levelNames.add(level);
            }
        }
        StringBuilder msg = new StringBuilder("LevelManager initialized with " + levelNames.size() + " levels:");
        int i = 0;
        for (String level : levelNames) {
            if (i % 9 == 0) {
                msg.delete(msg.length() - 1, msg.length());
                msg.append("\n\t");
            }
            msg.append(level.replace(".level", ""));
            msg.append(", ");
            i++;
        }
        msg.delete(msg.length() - 2, msg.length());
        logger.info(msg.toString());
    }

    /**
     * Loads a level given its name.
     *
     * <p>If the level exists, a new Level instance is created using the provided ChunkManager.
     * If the level is not found, a new generated level is returned instead.</p>
     *
     * @param levelName    the name of the level to load (without the .level extension)
     * @param chunkManager the ChunkManager instance to be used by the Level
     * @return a Level instance, either loaded or generated
     */
    public Level loadLevel(String levelName, ChunkManager chunkManager) {
        if (levelNames.contains(levelName + ".level")) {
            return new Level(levelName, chunkManager);
        } else {
            logger.warning("Level not found: " + levelName + " generated level instead");
            return LevelGenerator.generateLevel(chunkManager, 5, 5, 10);
        }
    }
}
