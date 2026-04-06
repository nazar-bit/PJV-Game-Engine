package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.managers.*;
import cz.cvut.fel.pjv.entities.*;
import cz.cvut.fel.pjv.levelGraph.LevelGraph;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import jdk.jshell.execution.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.Utils.createLogPropFile;
import static cz.cvut.fel.pjv.Utils.loadGeneralSettings;

/**
 * The Game class is the main entry point for the application.
 *
 * <p>
 * It manages the primary game loop, scene setup,
 * and various managers such as texture, sound, network, and level. The class handles both single-player
 * and multiplayer scenarios.
 * </p>
 */
public class Game extends Application {

//    private static final double ASPECT_RATIO = 16.0 / 9.0;
    public static long entityIdCounter = 0;

    private static final Logger logger = Logger.getLogger("Game");
    public static final LogManager logManager = Utils.getLogManager();
    public static boolean multiplayer = false;
    public static boolean host = true;
    public static boolean connectionSetUp = false;
    public static boolean clientSetUp = false;
    public static final double WIDTH = 1200;
    public static final double HEIGHT = 800;

    public static double BLOCK_SIZE = 50;
    public static double deltaTime = 0;
    public static boolean paused = true;
    public boolean blur = false;

    public static Stage currentStage;
    public static Scene gameScene;
    public static Scene mainMenuScene;
    public static Scene deathScreenScene;

    static Group root = new Group();
    static Group blocks = new Group();
    static Group entitiesGroup = new Group();
    static Group debugInfo = new Group();
    static Group userInterfaceGroup = new Group();
    static Group menusGroup = new Group();

    /// Effect Groups
    static Group effectsImmune = new Group();
    static Group effectsAffected = new Group();

    public static final TextureManager textureManager = new TextureManager();
    public static final SoundManager soundManager = new SoundManager();
    public static final NetworkManager networkManager = new NetworkManager();

    public static final UserInputHandler inputHandler = new UserInputHandler();
    public static ArrayList<Entity> entities = new ArrayList<>();
    public static ArrayList<Entity> exportEntities = new ArrayList<>();
    public static final Player player = new Player(2, 2, 1, 1);

    public static ArrayList<Entity> players = new ArrayList<>();


    public static final ObservableList<Node> blocksList = blocks.getChildren();
    public static final ObservableList<Node> entitiesViewersList = entitiesGroup.getChildren();
    public static final ObservableList<Node> userInterfaceList = userInterfaceGroup.getChildren();
    public static final ObservableList<Node> debugInfoList = debugInfo.getChildren();
    public static final ObservableList<Node> menusList = menusGroup.getChildren();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static final CustomCursor customCursor = new CustomCursor();
    public static final Camera camera = new Camera(player);

    public static final MainMenu mainMenu = new MainMenu();
    public static final DeathScreen deathScreen = new DeathScreen();


    private final Label labelCoord = Setup.setCoordLabel();
    private static final int TARGET_FPS = 60;

    public static final ArrayList<Collision> collisions = new ArrayList<>();


    private static String basePath;
    List<BlockDrawer> blockDrawers = new ArrayList<>();

    public static long lastUpdateTime = System.nanoTime();



    public static Level level;
    public static final EntityManager entityManager = new EntityManager();
    public static final WeaponManager weaponManager = new WeaponManager();
    public static LevelGraph levelGraph;
    public static final BlockManager blockManager = new BlockManager();
    public static final LevelManager levelManager = new LevelManager();
    static String levelName = null;

    public Game() throws FileNotFoundException {
    }

    /**
     * The main entry point for the JavaFX application.
     *
     * <p>
     * This method sets up the stage, initializes levels, groups, and scenes, and schedules the game loop.
     * </p>
     *
     * @param primaryStage the primary stage for the application.
     */
    @Override
    public void start(Stage primaryStage) {


        currentStage = primaryStage;
        loadGeneralSettings();
        if(levelName == null) {
            setLevel("GENERATE");
        }
        if(!host && multiplayer)
        {
            networkManager.setUpMultiplayer();
        }
        setUpLevelGraph();
        Rectangle background = new Rectangle(WIDTH, HEIGHT);
        root.getChildren().add(background);

        player.id = Long.MAX_VALUE;
        entities.add(player);
        entities.add(camera);

        players.add(player);
        player.hudManager.updateHP((int)player.getHealth());


        effectsImmune.getChildren().addAll(menusGroup);
        effectsAffected.getChildren().addAll(blocks, entitiesGroup, userInterfaceGroup, debugInfo);

        root.getChildren().addAll(effectsAffected);
        root.getChildren().addAll(effectsImmune);

        debugInfoList.add(labelCoord);
        userInterfaceList.addAll(player.hudManager.labels);
        menusList.addAll(player.hudManager.menus);


        Setup.setBlocksGroup(blocksList, blockDrawers, level, camera.getX(), camera.getY());


        gameScene = new Scene(root, WIDTH, HEIGHT);
        mainMenuScene = new Scene(mainMenu.getMainMenuGroup(), WIDTH, HEIGHT);
        deathScreenScene = new Scene(deathScreen.getDeathScreenGroup(), WIDTH, HEIGHT);


        customCursor.updatePosition(gameScene);

        currentStage.setScene(mainMenuScene);
        currentStage.setTitle("Operation Magic");
        currentStage.show();

        gameScene.setOnKeyPressed(inputHandler::handleKeyPressed);
        gameScene.setOnKeyReleased(inputHandler::handleKeyReleased);
        gameScene.setOnMousePressed(inputHandler::handleMousePressed);
        gameScene.setOnMouseDragged(inputHandler::handleMouseDragged);
        gameScene.setOnMouseReleased(inputHandler::handleMouseReleased);
        gameScene.setOnScroll(inputHandler::handleMouseScrolled);

        executor.scheduleAtFixedRate(this::gameLoop, 0, (long) (1000_000_000.0 / TARGET_FPS), TimeUnit.NANOSECONDS);
    }

    /**
     * Sets up the level graph for the current level.
     */
    public static void setUpLevelGraph()
    {
        levelGraph = new LevelGraph(level);
        level.spawnPosition(player);
    }

    /**
     * The core game loop, responsible for updating game logic and rendering.
     *
     * <p>
     * It calculates the delta time, updates input handling, synchronizes multiplayer if applicable,
     * and triggers render calls accordingly.
     * </p>
     */
    private void gameLoop() {
        long now = System.nanoTime();
        deltaTime = (now - lastUpdateTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
        lastUpdateTime = now;
        updateInputHandler(deltaTime);
        if(connectionSetUp) {
            networkManager.synchronizeMultiplayer();
        }
        if(connectionSetUp && !host && !clientSetUp) {
            setUpLevelGraph();
            clientSetUp = true;
        }
        if (paused) {
            doOnPause();
            return;
        }
        doOnUnpause();
        Update.calculateNewBlockDrawersPos(blockDrawers, level, camera.getX(), camera.getY());
        Update.calculateEntityPos(entities, camera);
        render();
    }


    /**
     * Updates the input handler.
     *
     * @param deltaTime the elapsed time since the last update (in seconds).
     */
    private void updateInputHandler(double deltaTime) {
        inputHandler.update();
        inputHandler.checkKeyPressed();
        inputHandler.checkInteractionKey();
    }


    /**
     * Handles game behavior when the game is paused.
     *
     * <p>
     * Applies a blur effect, notifies multiplayer clients if necessary, displays the pause menu,
     * and resets the cursor.
     * </p>
     */
    private void doOnPause() {
        if(!blur) {
            if(connectionSetUp) {
                if(multiplayer) exportEntities.add(new PauseRequest());
            }
            BoxBlur boxBlur = new BoxBlur(5, 5, 50);
            effectsAffected.setEffect(boxBlur);
            blur = true;
            player.hudManager.setPauseMenuVisible();
            root.setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Handles behavior when the game is unpaused.
     *
     * <p>
     * Removes the blur effect, hides the pause menu, resets the cursor, and sets up multiplayer if needed.
     * </p>
     */
    private void doOnUnpause() {
        if(blur) {
            if(connectionSetUp) {
                if(multiplayer )exportEntities.add(new UnPauseRequest());
            }
            effectsAffected.setEffect(null);
            player.hudManager.setPauseMenuInvisible();
            blur = false;
            root.setCursor(Cursor.CROSSHAIR);
            if(multiplayer && !connectionSetUp) {
                networkManager.setUpMultiplayer();
            }
        }
    }



    /**
     * Renders the game by updating entities, blocks, debug information, and HUD elements.
     *
     * <p>
     * This method schedules UI updates on the JavaFX Application Thread using Platform.runLater.
     * </p>
     */
    private void render() {
        Platform.runLater(() -> {
            if(host)
            {
                for(Entity entity : entities)
                {
                    if (entity.getState() != Entity.State.DESTROYED) {
                        entity.update(level, entities);
                    }
                }
            }
            if(!host){
                player.update(level, entities);
                camera.update(level, entities);
            }


            debugInfoList.clear();
            checkIfAllImageViewsAreAdded();
            Update.updateBlockDrawers(blockDrawers);
            Update.updateEntityOnScreen(entities, entitiesViewersList);
            labelCoord.setText(String.format("X: %.2f Y: %.2f", player.getX(), player.getY()));
            debugInfoList.add(labelCoord);
        });
    }


    public Entity.Direction findDirection(double angle)
    {
        for(Entity.Direction direction : Entity.Direction.values())
        {
            if(direction == Entity.Direction.NONE)
            {
                return direction;
            }
            if(direction == Entity.Direction.LEFT && angle <= -direction.getAngle() + Math.PI / 8)
            {
                return direction;
            }
            if(angle >= direction.getAngle() - Math.PI / 8 && angle <= direction.getAngle() + Math.PI / 8)
            {
                return direction;
            }
        }
        logger.warning("Couldn't find direction");
        throw new RuntimeException("Couldn't find direction");
    }


    public static String getPath() {
        if (basePath == null) {
            basePath = new File("").getAbsolutePath();
        }
        return basePath;
    }

    /**
     * Ensures that each entity's image view is added to the viewer list.
     *
     * <p>
     * This method iterates through all entities and adds their associated image view if missing.
     * </p>
     */
    public void checkIfAllImageViewsAreAdded()
    {
        for(Entity entity : entities)
        {
            if(!entitiesViewersList.contains(entity.drawer.getBlockImageView()))
            {
                entitiesViewersList.add(entity.drawer.getBlockImageView());
            }
        }
    }

    @Override
    public void stop() {
        executor.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void addEntity(Entity entity) {
        if (entity != null) {
            if(host)
            {
                entities.add(entity);
            }
            else{
                if(multiplayer) exportEntities.add(entity);
            }
        }
    }

    public static void setLevel(String levelName) {

        Game.levelName = levelName;
        if (levelName.equals("GENERATE")) {
            level = LevelGenerator.generateLevel(new ChunkManager(blockManager), 5, 5, 10);
        } else {
            level = levelManager.loadLevel(levelName, new ChunkManager(blockManager));
        }
    }

}