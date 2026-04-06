package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.entities.Interactable;
import cz.cvut.fel.pjv.entities.Player;
import cz.cvut.fel.pjv.weapons.MeleeWeapon;
import cz.cvut.fel.pjv.weapons.weaponInstances.BlankWeapon;
import cz.cvut.fel.pjv.weapons.FireMode;
import javafx.application.Platform;
import javafx.scene.input.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import static cz.cvut.fel.pjv.Game.player;
import static cz.cvut.fel.pjv.Utils.createSettingsFile;

/**
 * UserInputHandler handles all user inputs including keyboard, mouse, and scroll events.
 *
 * <p>
 * It allows configuration of hotkeys, processes input events, and triggers game actions
 * such as moving the player, interacting with objects, shooting, dropping weapons, and managing game state.
 * </p>
 */
public class UserInputHandler {

    private KeyCode moveUp = KeyCode.W;
    private KeyCode moveDown = KeyCode.S;
    private KeyCode moveLeft = KeyCode.A;
    private KeyCode moveRight = KeyCode.D;
    private KeyCode interact = KeyCode.F;
    private KeyCode FirstSlotKey = KeyCode.DIGIT1;
    private KeyCode SecondSlotKey = KeyCode.DIGIT2;
    private KeyCode ThirdSlotKey = KeyCode.DIGIT3;



    public HashMap<KeyCode, Boolean> pressedKeys = new HashMap<>();
    public HashMap<MouseButton, MouseClick> pressedMouseButtons = new HashMap<>();
    private final double maxInteractionTimeOut = .3;
    private double interactionTimeOut;
    private final double maxMouseScrollInteractionTimeOut = .2;
    private double mouseScrollInteractionTimeOut;
    private int mouseScrollCounter = 0;

    private static final Logger logger = Logger.getLogger("UserInputHandler");

    /**
     * Constructs a UserInputHandler and initializes key and mouse button mappings.
     * It also sets up hotkeys using the settings file.
     */
    public UserInputHandler() {

        for(KeyCode keyCode : KeyCode.values()) {
            this.pressedKeys.put(keyCode, false);
        }
        for(MouseButton button : MouseButton.values()) {
            this.pressedMouseButtons.put(button, new MouseClick(false, 0, 0));
        }
        SetHotKeys();
    }

    /// sets hotkeys from the settings file
    public void SetHotKeys(){
        File hotkeysFile = new File("settings.txt");

        if (!hotkeysFile.exists()) {
            try {
                createSettingsFile(hotkeysFile);
            } catch (Exception e) {
                logger.warning("Unable to create hotkeys file");
            }
        }

        if (hotkeysFile.exists()) {
            try {
                loadHotkeys(hotkeysFile);
            } catch (Exception e) {
                logger.warning("Unable to load hotkeys");
            }
        } else {
            logger.warning("Settings file not found.");
        }
    }


    /// loads hotkeys from the settings file
    private void loadHotkeys(File hotkeysFile) {
        if (hotkeysFile.exists()) {
            try (Scanner scanner = new Scanner(hotkeysFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (!line.startsWith("#")) {
                        if (line.equals("[HOTKEYS]")) {
                            while (scanner.hasNextLine() && !(line = scanner.nextLine()).equals("[END-HOTKEYS]")) {
                                if (line.startsWith("#")) {
                                    continue; // Skip comments
                                }
                                String[] parts = line.split("=");
                                if (parts.length == 2) {
                                    String functionName = parts[0].trim();
                                    String keyName = parts[1].trim();
                                    KeyCode keyCode = KeyCode.getKeyCode(keyName);
                                    if (keyCode != null) {
                                        // Assign the keyCode to the corresponding function
                                        assignKeyToFunction(functionName, keyCode);
                                    } else {
                                        logger.warning("Unknown key: " + keyName + ", hotkey could not be set.");
                                    }
                                }
                            }
                            break;
                        }

                    }
                }
            } catch (IOException e) {
                logger.warning("Failed to read hotkeys file: " + e.getMessage());
            }
        } else {
            logger.warning("Hotkeys file not found.");
        }
    }

    /**
     * Assigns a key to a specific game function (like go up, go down, interact, etc.) based on the function name.
     *
     * @param functionName The name of the function to be mapped.
     * @param keyCode      The key code to assign.
     */
    private void assignKeyToFunction(String functionName, KeyCode keyCode) {
        switch (functionName) {
            case "go_up":
                this.moveUp = keyCode;
                break;
            case "go_down":
                this.moveDown = keyCode;
                break;
            case "go_left":
                this.moveLeft = keyCode;
                break;
            case "go_right":
                this.moveRight = keyCode;
                break;
            case "interact":
                this.interact = keyCode;
                break;
            case "first_slot":
                this.FirstSlotKey = keyCode;
                break;
            case "second_slot":
                this.SecondSlotKey = keyCode;
                break;
            case "third_slot":
                this.ThirdSlotKey = keyCode;
                break;
            default:
                logger.warning("Unknown function: " + functionName);
        }
    }

    public void handleKeyPressed(KeyEvent event) {
        this.pressedKeys.put(event.getCode(), true);
    }
    public void handleKeyReleased(KeyEvent event) {
        this.pressedKeys.put(event.getCode(), false);
    }
    public void handleMousePressed(MouseEvent event) {
        this.pressedMouseButtons.put(event.getButton(), new MouseClick(true, event.getX(), event.getY()));
    }
    public void handleMouseDragged(MouseEvent event) {
        if(player.getWeaponInHands().getFireMode() != FireMode.MANUAL) {
            this.pressedMouseButtons.put(event.getButton(), new MouseClick(true, event.getX(), event.getY()));
        }
    }
    public void handleMouseReleased(MouseEvent event) {
        this.pressedMouseButtons.put(event.getButton(), new MouseClick(false, event.getX(), event.getY()));
    }

    public void handleMouseScrolled(ScrollEvent event) {
        /// The code abstract below prevents from changing the weapon too fast
        if(mouseScrollCounter >= 2 && mouseScrollInteractionTimeOut > maxMouseScrollInteractionTimeOut) {
            mouseScrollCounter = 0;
            mouseScrollInteractionTimeOut = 0;
        }

        if(mouseScrollCounter < 2) {
            mouseScrollCounter++;
            int newWeapon;
            if (event.getDeltaY() > 0) {
                newWeapon = (player.getSelectedWeapon() + 1) % 3;
            } else if (event.getDeltaY() < 0) {
                newWeapon = player.getSelectedWeapon() - 1;
                if (newWeapon == -1) newWeapon = 2;
            } else {
                return;
            }

            switch (newWeapon) {
                case 0:
                    this.selectWeaponBox1();
                    break;
                case 1:
                    this.selectWeaponBox2();
                    break;
                case 2:
                    this.selectWeaponBox3();
                    break;
            }
       }
    }


    public void update() {
        checkPlayerDirection();
        checkMousePressed();
        interactionTimeOut += 1 * Game.deltaTime;
        mouseScrollInteractionTimeOut += 1 * Game.deltaTime;
    }

    /**
     * Checks the player's key-based directional input and updates the player's direction.
     *
     * <p>
     * Conflicting inputs cancel out to maintain a neutral direction.
     * </p>
     */
    private void checkPlayerDirection()
    {
        player.setDirection(Player.Direction.NONE);
        if(pressedKeys.get(moveUp) && pressedKeys.get(moveDown)) player.setDirection(Player.Direction.NONE);
        else {
            if (pressedKeys.get(moveUp)) player.setDirection(Player.Direction.UP);
            if (pressedKeys.get(moveDown)) player.setDirection(Player.Direction.DOWN);
        }
        if(player.getDirection() == Player.Direction.NONE) {
            if (pressedKeys.get(moveLeft) && pressedKeys.get(moveRight)) player.setDirection(Player.Direction.NONE);
            else {
                if (pressedKeys.get(moveLeft)) player.setDirection(Player.Direction.LEFT);
                if (pressedKeys.get(moveRight)) player.setDirection(Player.Direction.RIGHT);
            }
        }
        else if(player.getDirection() == Player.Direction.UP) {
            if (pressedKeys.get(moveLeft) && pressedKeys.get(moveRight)) player.setDirection(Player.Direction.UP);
            else {
                if (pressedKeys.get(moveLeft)) player.setDirection(Player.Direction.UP_LEFT);
                if (pressedKeys.get(moveRight)) player.setDirection(Player.Direction.UP_RIGHT);
            }
        }
        else if(player.getDirection() == Player.Direction.DOWN) {
            if (pressedKeys.get(moveLeft) && pressedKeys.get(moveRight)) player.setDirection(Player.Direction.DOWN);
            else {
                if (pressedKeys.get(moveLeft)) player.setDirection(Player.Direction.DOWN_LEFT);
                if (pressedKeys.get(moveRight)) player.setDirection(Player.Direction.DOWN_RIGHT);
            }
        }
    }

    /**
     * Checks if the interaction key is pressed and triggers interaction if applicable.
     *
     * <p>
     * An interaction timeout helps prevent rapid repeated interactions.
     * </p>
     */
    public void checkInteractionKey() {
        if(interactionTimeOut > maxInteractionTimeOut) {
            if (player.getInteractionTarget() != null) {
                if (pressedKeys.get(interact)) {
                    ((Interactable) player.getInteractionTarget()).interact();
                    interactionTimeOut = 0;
                }
            }
        }
    }

    /**
     * Checks mouse button states and triggers corresponding actions such as shooting or dropping a weapon.
     */
    public void checkMousePressed()
    {
        if(pressedMouseButtons.get(MouseButton.PRIMARY).mousePressed)
        {
            player.shoot(pressedMouseButtons.get(MouseButton.PRIMARY).mouseX, pressedMouseButtons.get(MouseButton.PRIMARY).mouseY);
        }

        if(pressedMouseButtons.get(MouseButton.SECONDARY).mousePressed)
        {
            Game.inputHandler.pressedMouseButtons.put(MouseButton.SECONDARY, new MouseClick(false, 0, 0));
            dropWeapon();
        }
    }

    /**
     * Drops the currently selected weapon if it is not a blank weapon.
     *
     * <p>
     * Updates the HUD icon, creates a weapon entity in the game, and removes the weapon from the player's inventory.
     * </p>
     */
    public void dropWeapon()
    {
        if(!(player.getWeaponInHands() instanceof BlankWeapon))
        {
            player.hudManager.setNewIcon(player.getSelectedWeapon(), "empty");
            player.getWeaponInHands().createWeaponEntity();
            player.removeSelectedWeapon();
            player.hudManager.updateAmmoLabel();
        }
    }

    /**
     * Checks for weapon selection key presses and triggers the corresponding weapon box selection.
     */
    public void checkKeyPressed() {
        if(pressedKeys.get(FirstSlotKey)){
            selectWeaponBox1();
        }
        else if(pressedKeys.get(SecondSlotKey)){
            selectWeaponBox2();
        }
        else if(pressedKeys.get(ThirdSlotKey)){
            selectWeaponBox3();
        }

        if(pressedKeys.get(KeyCode.ESCAPE)){
           Game.paused = !Game.paused;
           pressedKeys.put(KeyCode.ESCAPE, false);
        }

        if(pressedKeys.get(KeyCode.R)) {
            if(player.getWeaponInHands() instanceof MeleeWeapon || player.getWeaponInHands().getMaxAmmo() <= 0)  return;
            player.getWeaponInHands().reload();
            pressedKeys.put(KeyCode.R, false);
            player.hudManager.reloadingText();
        }
    }


    private void selectWeaponBox1()
    {
        player.hudManager.setWeaponBoxDefault(player.getSelectedWeapon());
        player.setSelectedWeapon(0);
        player.hudManager.changeSelectedWeaponBox(0);
        pressedKeys.put(KeyCode.DIGIT1, false);
    }

    private void selectWeaponBox2()
    {
        player.hudManager.setWeaponBoxDefault(player.getSelectedWeapon());
        player.setSelectedWeapon(1);
        player.hudManager.changeSelectedWeaponBox(1);
        pressedKeys.put(KeyCode.DIGIT2, false);
    }

    private void selectWeaponBox3()
    {
        player.hudManager.setWeaponBoxDefault(player.getSelectedWeapon());
        player.setSelectedWeapon(2);
        player.hudManager.changeSelectedWeaponBox(2);
        pressedKeys.put(KeyCode.DIGIT3, false);
    }


    public void onResumeButton(MouseEvent event)
    {
        Game.paused = false;
    }

    public void onResumeButton(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)
        {
            Game.paused = false;
        }
    }


    public void onQuitButton(MouseEvent event)
    {
        Platform.exit();
    }

    public void onQuitButton(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)
        {
            Platform.exit();
        }
    }


    public void onSingleplayerButton(MouseEvent event)
    {
        Game.paused = false;
        startGame();
    }

    public void onSingleplayerButton(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)
        {
            Game.paused = false;
            startGame();
        }
    }


    public void onMultiplayerButton(MouseEvent event)
    {
        Game.mainMenu.openMultiplayerMenu();
    }

    public void onMultiplayerButton(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)
        {
            Game.mainMenu.openMultiplayerMenu();
        }
    }


    public void onHostButton(MouseEvent event)
    {
        Game.paused = false;
        Game.host = true;
        Game.multiplayer = true;
        startGame();
    }

    public void onHostButton(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)
        {
            Game.paused = false;
            Game.host = true;
            Game.multiplayer = true;
            startGame();
        }
    }


    public void onJoinButton(MouseEvent event)
    {
        Game.paused = false;
        Game.host = false;
        Game.multiplayer = true;
        startGame();
    }

    public void onJoinButton(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)
        {
            Game.paused = false;
            Game.host = false;
            Game.multiplayer = true;
            startGame();
        }
    }


    public void onToMenu(MouseEvent event)
    {
        Game.currentStage.setScene(Game.mainMenuScene);
    }

    public void onToMenu(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)
        {
            Game.currentStage.setScene(Game.mainMenuScene);
        }
    }


    public void startGame()
    {
        Game.currentStage.setScene(Game.gameScene);
    }

    public String getInteractionKey() {
        return interact.toString();
    }


}
