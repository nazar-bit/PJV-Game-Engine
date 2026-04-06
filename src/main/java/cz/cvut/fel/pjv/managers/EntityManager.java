package cz.cvut.fel.pjv.managers;

import cz.cvut.fel.pjv.Utils;
import cz.cvut.fel.pjv.entities.Entity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.Game.addEntity;

/**
 * The EntityManager class is responsible for discovering and registering entity classes
 * and creating entity instances for game use.
 *
 * <p>It scans a predefined package to find entity classes, stores them in a registry,
 * and provides methods to create entities by name or based on a provided identifier.</p>
 */
public class EntityManager {
    private static final Logger logger = Logger.getLogger("EntityManager");
    private final Map<String, Class<? extends Entity>> entityRegistry = new HashMap<>();

    private static final String PACKAGE_NAME = "cz.cvut.fel.pjv.entities";


    /**
     * Constructs an EntityManager instance.
     * <p>
     * This constructor scans the specified package for entity classes,
     * registers them into the entityRegistry, and logs the total number of registered entities.
     * </p>
     */
    public EntityManager() {
        ArrayList<Class<?>> classes;
        try {
            classes = Utils.findClassesInPackage(PACKAGE_NAME);
            for (Class<?> clazz : classes) {
                registerEntityClass(clazz);
            }
        } catch (Exception e) {
            logger.severe("Failed to load entities: " + e.getMessage());
            e.printStackTrace();
        }


        StringBuilder msg = new StringBuilder("EntityManager initialized with " + entityRegistry.size() + " entities:");
        int i = 0;
        for (String name : entityRegistry.keySet()) {
            if (i % 9 == 0) {
                msg.delete(msg.length() - 1, msg.length());
                msg.append("\n\t");
            }
            msg.append(name);
            msg.append(", ");
            i++;

        }
        msg.delete(msg.length() - 2, msg.length());
        logger.info(msg.toString());
    }

    /// Registers new entity class into entity
    private void registerEntityClass(Class<?> clazz) throws ClassNotFoundException {

        if (Entity.class.isAssignableFrom(clazz) &&
                !Modifier.isAbstract(clazz.getModifiers()) &&
                !clazz.isInterface()) {
            entityRegistry.put(clazz.getSimpleName(), clazz.asSubclass(Entity.class));
        }
    }


    /**
     * Create an entity by its ID for network synchronization
     * @param id The entity ID to create
     * @param x The x position
     * @param y The y position
     * @return A new entity with the specified ID
     */
    public Entity createEntityById(long id, double x, double y) {
        // Determine entity type from ID if you have a way to store this info
        // This is just an example - you'll need to adapt based on your system

        Entity entity = null;

        // Here you need to decide what type of entity to create based on the ID
        // You might want to store a mapping of IDs to entity types

        // Example:
        if (id % 10 == 1) {
            entity = createEntity("Enemy", x, y);
        } else if (id % 10 == 2) {
            entity = createEntity("WeaponEntity", x, y, "Pistol");
        } else if (id % 10 == 3) {
            entity = createEntity("WeaponEntity", x, y, "Shotgun");
        } else {
            // Create a default entity type
            entity = createEntity("Enemy", x, y);
        }

        if (entity != null) {
            entity.id = id;
        }

        return entity;
    }

    /**
     * Creates an entity by its registered name and initialization parameters.
     *
     * @param name   the simple name of the entity type.
     * @param params the parameters required for the entity's constructor.
     * @return the created Entity instance.
     * @throws IllegalArgumentException if no entity is registered with the given name.
     * @throws RuntimeException         if a matching constructor is not found or instantiation fails.
     */
    public Entity createEntity(String name, Object... params) {
        Class<? extends Entity> clazz = entityRegistry.get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("No entity found with name: " + name);
        }

        try {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (constructor.getParameterCount() == params.length) {
                    Entity entity = (Entity) constructor.newInstance(params);
                    addEntity(entity);
                    return entity;
                }
            }
            logger.severe("No matching constructor found for entity: " + name);
            throw new RuntimeException("No matching constructor found for entity: " + name);
        } catch (RuntimeException e) {
            logger.warning( "Failed to create entity " + name + ": " + e.getMessage());
            return null;
        }catch (Exception e) {
            logger.severe( "Failed to create entity " + name + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
