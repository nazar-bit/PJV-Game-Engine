package cz.cvut.fel.pjv.managers;


import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.Utils;
import cz.cvut.fel.pjv.weapons.Weapon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;


/**
 * The WeaponManager class is responsible for managing and instantiating weapon types in the game.
 *
 * <p>It scans a specific package for weapon classes, registers them in an internal registry,
 * and provides functionality to create instances of these weapons by their class names.</p>
 */
public class WeaponManager{
    private static final Logger logger = Logger.getLogger("WeaponManager");
    private final Map<String, Class<? extends Weapon>> weaponRegistry = new HashMap<>();
    private static final String PACKAGE_NAME = "cz.cvut.fel.pjv.weapons.weaponInstances";

    /**
     * Constructs a new WeaponManager and initializes the registry by scanning the designated package
     * for weapon classes.
     */
    public WeaponManager() {
        ArrayList<Class<?>> classes;
        try {
            classes = Utils.findClassesInPackage(PACKAGE_NAME);
            for (Class<?> clazz : classes) {
                registerWeaponClass(clazz);
            }
        } catch (Exception e) {
            logger.severe("Failed to load weapons: " + e.getMessage());
            e.printStackTrace();
        }

        StringBuilder msg = new StringBuilder( "WeaponManager initialized with " + weaponRegistry.size() + " weapons:");
        int i = 0;
        for (String name : weaponRegistry.keySet()) {
            if (i % 9 == 0) {
                msg.delete(msg.length() - 1, msg.length());
                msg.append("\n\t");
            }
            msg.append(name).append(", ");
            i++;
        }
        msg.delete(msg.length() - 2, msg.length());
        logger.info(msg.toString());
    }

    /**
     * Registers new weapon class into weaponRegistry
     * @param clazz class
     */
    private void registerWeaponClass(Class<?> clazz) {

        if (Weapon.class.isAssignableFrom(clazz) &&
                !Modifier.isAbstract(clazz.getModifiers()) &&
                !clazz.isInterface()) {
            weaponRegistry.put(clazz.getSimpleName(), clazz.asSubclass(Weapon.class));
        }
    }


    /** Creates a new weapon by String (Class name)
     * @param name class name
     * @throws IllegalArgumentException if no weapon found with the given name
     * @throws RuntimeException if the weapon constructor fails
     * @return Weapon
     */
    public Weapon createWeapon(String name) {
        Class<? extends Weapon> clazz = weaponRegistry.get(name);
        if (clazz == null) {
            logger.severe("No weapon found with name: " + name);
            throw new IllegalArgumentException("No weapon found with name: " + name);
        }

        try {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                return (Weapon) constructor.newInstance();
            }
            logger.severe("No matching constructor found for weapon: " + name);
            throw new RuntimeException("No matching constructor found for weapon: " + name);
        } catch (RuntimeException e) {
            logger.severe("Failed to create weapon " + name + ": " +e.getMessage());
            return null;
        }catch (Exception e) {
            logger.severe("Failed to create weapon " + name + " due to internal error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
