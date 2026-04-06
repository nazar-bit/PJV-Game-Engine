package cz.cvut.fel.pjv.managers;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.Utils;
import cz.cvut.fel.pjv.blocks.Block;
import cz.cvut.fel.pjv.entities.Entity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import java.lang.reflect.Field;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;


/**
 * BlockManager is responsible for discovering, registering, and providing instances of Block classes.
 *
 * <p>
 * It uses reflection to scan a specific package, locates classes that extend Block, retrieves their
 * unique static IDs, and registers a supplier that creates a new instance. Clients can then request blocks
 * by their ID.
 * </p>
 */
public class BlockManager {
    private static final Logger logger = Logger.getLogger("BlockManager");
    private final Map<Integer, Supplier<Block>> blockRegistry = new HashMap<>();

    private static final String PACKAGE_NAME = "cz.cvut.fel.pjv.blocks";

    /**
     * Constructs a new BlockManager and initializes the block registry.
     *
     * <p>
     * It uses a utility to find all classes within the specified package and registers them, based on their
     * static ID field. After registration, logs the list of registered block types and their IDs.
     * </p>
     */
    public BlockManager() {
        ArrayList<Class<?>> classes;
        try {
            classes = Utils.findClassesInPackage(PACKAGE_NAME);
            for (Class<?> clazz : classes) {
                registerBlockClass(clazz);
            }
        } catch (Exception e) {
            logger.severe("Failed to load blocks: " + e.getMessage());
            e.printStackTrace();
        }

        StringBuilder msg = new StringBuilder( "BlockManager initialized with " + blockRegistry.size() + " blocks:");
        int i = 0;
        for (int ID : blockRegistry.keySet()) {
            if (i % 9 == 0) {
                msg.delete(msg.length() - 1, msg.length());
                msg.append("\n\t");
            }
            msg.append(blockRegistry.get(ID).get().getClass().getSimpleName()).append("(ID: ").append(ID).append("), ");
            i++;
        }
        msg.delete(msg.length() - 2, msg.length());
        logger.info(msg.toString());
    }


    /**
     * Registers a block class if it extends Block.
     *
     * <p>
     * It obtains the static ID field from the block class and registers a Supplier that creates an instance using its
     * default constructor.
     * </p>
     *
     * @param clazz the class to register.
     * @throws ClassNotFoundException if the class is not found (unlikely, since it is already loaded).
     */
    private void registerBlockClass(Class<?> clazz) throws ClassNotFoundException {
        if (Block.class.isAssignableFrom(clazz)) {
            try {
                Field idField = clazz.getDeclaredField("ID");
                idField.setAccessible(true);
                int id = idField.getInt(null); // Get static field value

                blockRegistry.put(id, () -> {
                    try {
                        return (Block) clazz.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
                    }
                });
            }catch (Exception e){
                logger.warning("Failed to register block class: " + clazz.getName());
                e.printStackTrace();
                return;
            }
        }
    }

    /// Returns a new instance of the block with the given ID
    public Block getBlockById(int id) {
        Supplier<Block> supplier = blockRegistry.get(id);
        if (supplier != null) {
            return supplier.get();
        } else {
            logger.severe("No block found for ID: " + id);
            throw new IllegalArgumentException("No block found for ID: " + id);
        }
    }

    /// Returns block registry
    public Map<Integer, Supplier<Block>> getBlockRegistry() {
        return blockRegistry;
    }
}

