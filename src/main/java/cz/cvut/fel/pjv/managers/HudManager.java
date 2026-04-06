package cz.cvut.fel.pjv.managers;

import cz.cvut.fel.pjv.Game;
import cz.cvut.fel.pjv.UserInputHandler;
import cz.cvut.fel.pjv.entities.Interactable;
import cz.cvut.fel.pjv.entities.Player;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

import static cz.cvut.fel.pjv.Game.inputHandler;
import static cz.cvut.fel.pjv.managers.TextureManager.logger;


/**
 * HudManager is responsible for managing and displaying the game's heads-up display (HUD).
 * It shows various visual elements such as health, ammo, weapon selection boxes, pause menu,
 * and interaction prompts.
 */
public class HudManager {
    /// Weapon Boxes Settings
    private final double WEAPON_BOX_WIDTH = 50;
    private final double WEAPON_BOX_HEIGHT = 50;
    private final double SPLACE_BETWEEN_BOXES = 10;
    private final double BOXES_OPACITY = 0.5;
    private final double BOXES_PADDING_Y = 15;
    private final double WEAPON_ICONS_PADDING = 8;

    private final int PAUSE_MENU_WIDTH = 200;
    private final int PAUSE_MENU_HEIGHT = 200;
    private final int PAUSE_BUTTON_WIDTH = 180;
    private final int PAUSE_BUTTON_HEIGHT = 50;
    private final double PAUSE_MENU_SPACING = 2.87;

    public ArrayList<Node> labels = new ArrayList<>();
    public ArrayList<Node> menus = new ArrayList<>();
    public ArrayList<Rectangle> weaponBoxes = new ArrayList<>();
    public ArrayList<ImageView> weaponIcons = new ArrayList<>();

    private Label hpLabel = new Label("HP: 100");
    private Label ammoLabel = new Label("∞ / ∞");

    private Rectangle firstWeaponBox = new Rectangle();
    private Rectangle secondWeaponBox = new Rectangle();
    private Rectangle thirdWeaponBox = new Rectangle();
    private ImageView firstWeaponIcon;
    private ImageView secondWeaponIcon;
    private ImageView thirdWeaponIcon;
    private VBox pauseMenu;
    private Label interactionLabel = new Label("");

    private Player player;


    /**
     * Constructs the HUD manager and initializes the HUD elements.
     * <p>
     * Sets up labels for health, ammo, interaction hints, weapon boxes,
     * weapon icons, and initializes the pause menu.
     * </p>
     *
     * @param player the player entity whose information will be displayed on the HUD.
     */
    public HudManager(Player player) {
        this.player = player;
        /// Hp Label
        hpLabel.setLayoutX(20);
        hpLabel.setLayoutY(Game.HEIGHT - 40);
        hpLabel.setTextFill(Color.color(57/255.0, 255/255.0, 20/255.0));
        hpLabel.setStyle("-fx-font-size: 25");
        /// Interaction Label
        interactionLabel.setLayoutX(Game.WIDTH - 400);
        interactionLabel.setLayoutY(Game.HEIGHT - 120);
        interactionLabel.setTextFill(Color.color(1, .5, 0));
        interactionLabel.setStyle("-fx-font-size: 30");
        /// Ammo Label
        ammoLabel.setLayoutX(Game.WIDTH - 200);
        ammoLabel.setLayoutY(Game.HEIGHT - 60);
        ammoLabel.setTextFill(Color.color(1, 1, 1));
        ammoLabel.setStyle("-fx-font-size: 40");
        /// Item Selection Interface
        /// Weapon Boxes
        firstWeaponBox.setX(30);
        firstWeaponBox.setY(BOXES_PADDING_Y);
        firstWeaponBox.setFill(Color.YELLOW);
        firstWeaponBox.setWidth(WEAPON_BOX_WIDTH);
        firstWeaponBox.setHeight(WEAPON_BOX_HEIGHT);
        firstWeaponBox.setStroke(Color.BLACK);
        firstWeaponBox.setOpacity(BOXES_OPACITY);

        secondWeaponBox.setX(firstWeaponBox.getX() + firstWeaponBox.getWidth() + SPLACE_BETWEEN_BOXES);
        secondWeaponBox.setY(BOXES_PADDING_Y);
        secondWeaponBox.setFill(Color.WHITE);
        secondWeaponBox.setWidth(WEAPON_BOX_WIDTH);
        secondWeaponBox.setHeight(WEAPON_BOX_HEIGHT);
        secondWeaponBox.setStroke(Color.BLACK);
        secondWeaponBox.setOpacity(BOXES_OPACITY);

        thirdWeaponBox.setX(secondWeaponBox.getX() + secondWeaponBox.getWidth() + SPLACE_BETWEEN_BOXES);
        thirdWeaponBox.setY(BOXES_PADDING_Y);
        thirdWeaponBox.setFill(Color.WHITE);
        thirdWeaponBox.setWidth(WEAPON_BOX_WIDTH);
        thirdWeaponBox.setHeight(WEAPON_BOX_HEIGHT);
        thirdWeaponBox.setStroke(Color.BLACK);
        thirdWeaponBox.setOpacity(BOXES_OPACITY);

        /// Weapon Icons
        firstWeaponIcon = new ImageView(Game.textureManager.getTexture(player.getWeapon(0).getIcon()));
        firstWeaponIcon.setX(firstWeaponBox.getX() + WEAPON_ICONS_PADDING/2);
        firstWeaponIcon.setY(firstWeaponBox.getY() + WEAPON_ICONS_PADDING/2);
        firstWeaponIcon.setFitWidth(firstWeaponBox.getWidth() - WEAPON_ICONS_PADDING);
        firstWeaponIcon.setFitHeight(firstWeaponBox.getHeight() - WEAPON_ICONS_PADDING);

        secondWeaponIcon = new ImageView(Game.textureManager.getTexture(player.getWeapon(1).getIcon()));
        secondWeaponIcon.setX(secondWeaponBox.getX() + WEAPON_ICONS_PADDING/2);
        secondWeaponIcon.setY(secondWeaponBox.getY() + WEAPON_ICONS_PADDING/2);
        secondWeaponIcon.setFitWidth(secondWeaponBox.getWidth() - WEAPON_ICONS_PADDING);
        secondWeaponIcon.setFitHeight(secondWeaponBox.getHeight() - WEAPON_ICONS_PADDING);

        thirdWeaponIcon = new ImageView(Game.textureManager.getTexture(player.getWeapon(2).getIcon()));
        thirdWeaponIcon.setX(thirdWeaponBox.getX() + WEAPON_ICONS_PADDING/2);
        thirdWeaponIcon.setY(thirdWeaponBox.getY() + WEAPON_ICONS_PADDING/2);
        thirdWeaponIcon.setFitWidth(thirdWeaponBox.getWidth() - WEAPON_ICONS_PADDING);
        thirdWeaponIcon.setFitHeight(thirdWeaponBox.getHeight() - WEAPON_ICONS_PADDING);


        /// PauseMenu
        /// Buttons
        Button resume = new Button();
        resume.setText("Resume");
        resume.setOnMouseClicked(inputHandler::onResumeButton);
        resume.setOnKeyPressed(inputHandler::onResumeButton);
        resume.setPrefWidth(PAUSE_BUTTON_WIDTH);
        resume.setPrefHeight(PAUSE_BUTTON_HEIGHT);

        Button quit = new Button();
        quit.setText("Quit");
        quit.setOnMouseClicked(inputHandler::onQuitButton);
        quit.setOnKeyPressed(inputHandler::onQuitButton);
        quit.setPrefWidth(PAUSE_BUTTON_WIDTH);
        quit.setPrefHeight(PAUSE_BUTTON_HEIGHT);

        /// Layout and settings
        Pane menu = new Pane();
        pauseMenu = new VBox();
        pauseMenu.setStyle("-fx-font-size: 25");
        pauseMenu.setLayoutX(Game.WIDTH/2 - PAUSE_MENU_WIDTH/2);
        pauseMenu.setLayoutY(Game.HEIGHT/2 - PAUSE_MENU_HEIGHT/2);
        pauseMenu.setPrefWidth(PAUSE_MENU_WIDTH);
        pauseMenu.setPrefHeight(PAUSE_MENU_HEIGHT);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setVisible(false);
        pauseMenu.setSpacing(PAUSE_MENU_SPACING);
        pauseMenu.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));

        /// Add all buttons to pauseMenu
        pauseMenu.getChildren().addAll(resume, quit);
        menu.getChildren().add(pauseMenu);




        /// Add everything to ArrayLists
        weaponIcons.add(firstWeaponIcon);
        weaponIcons.add(secondWeaponIcon);
        weaponIcons.add(thirdWeaponIcon);

        weaponBoxes.add(firstWeaponBox);
        weaponBoxes.add(secondWeaponBox);
        weaponBoxes.add(thirdWeaponBox);

        labels.addAll(weaponIcons);
        labels.addAll(weaponBoxes);
        labels.add(hpLabel);
        labels.add(interactionLabel);
        labels.add(ammoLabel);

        menus.add(menu);
    }


    public void updateHP(int hp) {
        this.hpLabel.setText("HP:  " + hp);
    }

    /// Shows interaction String when near Interactable
    public void updateKeyTip() {
        if(player.getInteractionTarget() != null) {
            interactionLabel.setText("Press " + inputHandler.getInteractionKey() + " to  " + ((Interactable) player.getInteractionTarget()).getInteractionText());
        } else {
            interactionLabel.setText("");
        }
    }

    /// Shows reloading text
    public void reloadingText()
    {
        Platform.runLater(()->{
            ammoLabel.setText("Reloading ...");
            ammoLabel.setLayoutX(Game.WIDTH - 200);
        });
    }

    /// Updates the ammo label
    public void updateAmmoLabel() {
        if(player.getWeaponInHands().isReloading())
        {
            reloadingText();
        }
        else{
            Platform.runLater(() -> {
                ammoLabel.setLayoutX(Game.WIDTH - 200);
                int ammoInMagazine = player.getWeaponInHands().getAmmoInMagazine();
                int maxAmmo = player.getWeaponInHands().getMaxAmmo();
                String ammoText = "";

                if(ammoInMagazine == Integer.MAX_VALUE)  ammoText += "∞ / ";
                else ammoText += ammoInMagazine + " / ";

                if(maxAmmo == Integer.MAX_VALUE)  ammoText += "∞";
                else ammoText += maxAmmo;

                ammoLabel.setText(ammoText);
            });
        }
    }

    /**
     * Changes the fill color of the selected weapon box.
     *
     * @param boxNumber the index of the weapon box to highlight.
     * @throws IllegalArgumentException if an invalid box number is provided.
     */
    public void changeSelectedWeaponBox(int boxNumber) {
        if(boxNumber < 0 || boxNumber > weaponBoxes.size()) {
            logger.warning("Invalid box number: " + boxNumber);
            throw new IllegalArgumentException("Invalid box number: " + boxNumber);
        }
        this.weaponBoxes.get(boxNumber).setFill(Color.YELLOW);
        updateAmmoLabel();
    }

    /**
     * Resets the weapon box color to its default state (white).
     *
     * @param boxNumber the index of the weapon box to reset.
     * @throws IllegalArgumentException if an invalid box number is provided.
     */
    public void setWeaponBoxDefault(int boxNumber) {
        if(boxNumber < 0 || boxNumber > weaponBoxes.size()) {
            logger.warning("Invalid box number: " + boxNumber);
            throw new IllegalArgumentException("Invalid box number: " + boxNumber);
        }
        this.weaponBoxes.get(boxNumber).setFill(Color.WHITE);
    }

    /**
     * Updates the weapon icon for a specified weapon box.
     *
     * @param boxIconNumber the index of the weapon icon to update.
     * @param iconTexture   the texture identifier for the new icon image.
     * @throws IllegalArgumentException if an invalid icon index is provided.
     */
    public void setNewIcon(int boxIconNumber, String iconTexture) {
        if(boxIconNumber < 0 || boxIconNumber > weaponIcons.size()) {
            logger.warning("Invalid box number: " + boxIconNumber);
            throw new IllegalArgumentException("Invalid box number: " + boxIconNumber);
        }
        this.weaponIcons.get(boxIconNumber).setImage(Game.textureManager.getTexture(iconTexture));
    }

    /**
     * Makes the pause menu visible.
     */
    public void setPauseMenuVisible()
    {
        this.pauseMenu.setVisible(true);
    }

    /**
     * Hides the pause menu.
     */
    public void setPauseMenuInvisible()
    {
        this.pauseMenu.setVisible(false);
    }

    /**
     * Clears the HUD by making all HUD elements invisible.
     * <p>
     * Uses Platform.runLater to ensure the UI modification happens on the JavaFX Application Thread.
     * </p>
     */
    public void clearHud() {
        Platform.runLater(() -> {
            for (Node label : labels) {
                label.setVisible(false);
            }
        });

    }
}
