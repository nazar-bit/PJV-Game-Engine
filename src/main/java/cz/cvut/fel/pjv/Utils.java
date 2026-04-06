package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.blocks.Block;
import cz.cvut.fel.pjv.managers.BlockManager;
import cz.cvut.fel.pjv.managers.ChunkManager;
import cz.cvut.fel.pjv.managers.NetworkManager;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.Game.*;

/**
 * The Utils class provides various utility functions including classpath scanning,
 * logging configuration, settings file creation/loading, and generating a list of blocks.
 */
public class Utils {
    private static final Logger logger = Logger.getLogger("Utils");

    /// Scans the specified package for classes and returns a list of them
    /// @param packageName the name of the package to scan
    /// @return a list of classes found in the package
    public static ArrayList<Class<?>> findClassesInPackage(String packageName) throws IOException, URISyntaxException, ClassNotFoundException {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        ArrayList<Class<?>> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {

            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();

            if (protocol.equals("file")) {
                File folder = new File(resource.toURI());
                classes.addAll(scanFolder(folder, packageName));
            } else if (protocol.equals("jar")) {
                classes.addAll(scanJar(resource, path));
            }
        }
        return classes;
    }

    /// Scans a folder for classes in the specified package
    /// @param folder the folder to scan
    /// @param packageName the package name
    /// @return a set of classes found in the folder
    private static Set<Class<?>> scanFolder(File folder, String packageName) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    scanFolder(file, packageName + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                            classes.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        logger.warning("Class not found: " + className);
                        System.err.println("Class not found: " + className);
                    }
                }
            }
        }
        return classes;
    }


    /// Scans a JAR file for classes in the specified package
    /// @param resource the URL of the JAR file
    /// @param path the package path
    /// @return a set of classes found in the JAR file
    private static Set<Class<?>> scanJar(URL resource, String path) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
            for (JarEntry entry : java.util.Collections.list(jarFile.entries())) {
                String entryName = entry.getName();
                if (entryName.startsWith(path) && entryName.endsWith(".class") && !entry.isDirectory()) {
                    String className = entryName.replace('/', '.').replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                            classes.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        logger.warning("Class not found: " + className);
                        System.err.println("Class not found: " + className);
                    }
                }
            }
        }
        return classes;
    }


    public static class Coordinates implements Serializable {
        public double x;
        public double y;
        public Coordinates(double x, double y){
            this.x = x;
            this.y = y;
        }
    }

    /// Sets up the LogManager and loads the logging properties file
    public static LogManager getLogManager() {
        LogManager logManager = LogManager.getLogManager();
        createLogPropFile();

        try {
            logManager.readConfiguration(new FileInputStream("logging.properties"));
        }catch (IOException e){
            logger.severe("Failed to load logging.properties file");
        }
        return logManager;
    }

    /// Creates a logging properties file if it doesn't exist
    public static void createLogPropFile() {
        try {
            File logProperties = new File("logging.properties");
            if (!logProperties.exists()) {
                logProperties.createNewFile();
                try (FileWriter writer = new FileWriter(logProperties)) {
                    writer.write("handlers= java.util.logging.ConsoleHandler\n");
                    writer.write("java.util.logging.ConsoleHandler.level = ALL\n");
                    writer.write("java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n");
                    writer.write("level = ALL\n");
                    writer.write("java.util.logging.SimpleFormatter.format = %1$tF %1$tT %4$s: %5$s%6$s%n");
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to create logging.properties file");
            e.printStackTrace();
        }
    }

    /// Creates a settings file with default hotkeys and general settings
    public static void createSettingsFile(File hotkeysFile) {
        try {
            if (hotkeysFile.createNewFile()) {
                logger.info("Settings file created: " + hotkeysFile.getAbsolutePath());
            } else {
                logger.info("Settings file already exists: " + hotkeysFile.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.warning("Failed to create settings file: " + e.getMessage());
        }
        try (FileWriter writer = new FileWriter(hotkeysFile)) {
            writer.write("[HOTKEYS]\n");
            writer.write("#Hotkeys configuration goes here.\n");
            writer.write("#How to use:\n");
            writer.write("#function_name=KeyName\n");
            writer.write("go_up=W\n");
            writer.write("go_down=S\n");
            writer.write("go_left=A\n");
            writer.write("go_right=D\n");
            writer.write("interact=F\n");
            writer.write("first_slot=1\n");
            writer.write("second_slot=2\n");
            writer.write("third_slot=3\n");
            writer.write("[END-HOTKEYS]\n");
            writer.write("[GENERAL]\n");
            writer.write("ip=localhost\n");
            writer.write("level=GENERATE\n");
            writer.write("[END-GENERAL]\n");


        } catch (IOException e) {
            logger.warning("Failed to write to settings file: " + e.getMessage());
        }
    }


    /// Loads general settings from the settings file
    public static void loadGeneralSettings() {
        File settingsFile = new File("settings.txt");
        if (!settingsFile.exists()) {
            createSettingsFile(settingsFile);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
            String line;
            boolean inGeneralSection = false;
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("#")) {
                    continue; // Skip comments
                }
                if (line.equals("[GENERAL]")) {
                    inGeneralSection = true;
                } else if (line.equals("[END-GENERAL]")) {
                    inGeneralSection = false;
                } else if (inGeneralSection) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        switch (key) {
                            case "ip":
                                networkManager.setIp(value);
                                break;
                            case "level":
                                setLevel(value);
                                break;
                            default:
                                logger.warning("Unknown setting: " + key + " = " + value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to load general settings: " + e.getMessage());
        }
    }

    /// Creates a file with available blocks' classNames and IDs
    public static void createBlocksFile(String dirName) {
        File blocksFile = new File(dirName + "/blocks.list");
        try {
            if (blocksFile.createNewFile()) {
                logger.info("Blocks file created: " + blocksFile.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.warning("Failed to create blocks file: " + e.getMessage());
        }

        try (FileWriter writer = new FileWriter(blocksFile)) {
            for (int ID : blockManager.getBlockRegistry().keySet()) {
                Block block = blockManager.getBlockRegistry().get(ID).get();
                writer.write(block.getClass().getSimpleName() + "(ID: " + ID + ")\n");
            }
        } catch (IOException e) {
            logger.warning("Failed to create blocks file: " + e.getMessage());
        }


    }

    private Utils() {
        // Prevent instantiation
    }
}
