package cz.cvut.fel.pjv;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;


/**
 * MainMenu represents the graphical main menu of the game.
 *
 * <p>
 * It sets up several buttons for navigating the game: singleplayer, multiplayer, quit, host, and join.
 * The menu layout is built using a VBox for button selection inside a StackPane, all contained within
 * a Group that stores the menu elements.
 * </p>
 */
public class MainMenu {

    private final double BUTTON_WIDTH = 250;
    private final double BUTTON_HEIGHT = 60;

    private final double BUTTON_SELECTION_WIDTH = 300;
    private final double BUTTON_SELECTION_HEIGHT = 300;
    private final double BUTTON_SELECTION_SPACING = 2.87;

    private Group mainMenuGroup = new Group();
    private List<Node> elements = new ArrayList<>();
    private StackPane buttons = new StackPane();
    private VBox buttonSelection = new VBox();

    private final Button singleplayer;
    private final Button multiplayer;
    private final Button quit;
    private final Button host;
    private final Button join;

    /**
     * Constructs the MainMenu and initializes all components.
     *
     * <p>
     * Here the main menu buttons are created, configured with event handlers, and added to the layout.
     * The multiplayer-related buttons (host and join) are also initialized but not immediately visible.
     * </p>
     */
    public MainMenu() {
        /// Buttons
        singleplayer = new Button();
        singleplayer.setText("Singleplayer");
        singleplayer.setOnMouseClicked(Game.inputHandler::onSingleplayerButton);
        singleplayer.setOnKeyPressed(Game.inputHandler::onSingleplayerButton);
        singleplayer.setPrefWidth(BUTTON_WIDTH);
        singleplayer.setPrefHeight(BUTTON_HEIGHT);

        multiplayer = new Button();
        multiplayer.setText("Multiplayer");
        multiplayer.setOnMouseClicked(Game.inputHandler::onMultiplayerButton);
        multiplayer.setOnKeyPressed(Game.inputHandler::onMultiplayerButton);
        multiplayer.setPrefWidth(BUTTON_WIDTH);
        multiplayer.setPrefHeight(BUTTON_HEIGHT);

        quit = new Button();
        quit.setText("Quit");
        quit.setOnMouseClicked(Game.inputHandler::onQuitButton);
        quit.setOnKeyPressed(Game.inputHandler::onQuitButton);
        quit.setPrefWidth(BUTTON_WIDTH);
        quit.setPrefHeight(BUTTON_HEIGHT);

        /// Multiplayer Menu
        host = new Button();
        host.setText("Host");
        host.setOnMouseClicked(Game.inputHandler::onHostButton);
        host.setOnKeyPressed(Game.inputHandler::onHostButton);
        host.setPrefWidth(BUTTON_WIDTH);
        host.setPrefHeight(BUTTON_HEIGHT);

        join = new Button();
        join.setText("Join");
        join.setOnMouseClicked(Game.inputHandler::onJoinButton);
        join.setOnKeyPressed(Game.inputHandler::onJoinButton);
        join.setPrefWidth(BUTTON_WIDTH);
        join.setPrefHeight(BUTTON_HEIGHT);


        /// ButtonSelection
        buttonSelection.setStyle("-fx-font-size: 25");
        buttonSelection.setPrefWidth(BUTTON_SELECTION_WIDTH);
        buttonSelection.setPrefHeight(BUTTON_SELECTION_HEIGHT);
        buttonSelection.setAlignment(Pos.CENTER);
        buttonSelection.setVisible(true);
        buttonSelection.setSpacing(BUTTON_SELECTION_SPACING);
        buttonSelection.setBackground(new Background(new BackgroundFill(Color.LIGHTCORAL, null, null)));


        buttons.setPrefSize(Game.WIDTH, Game.HEIGHT);


        /// Add everything to a Group
        buttonSelection.getChildren().addAll(singleplayer, multiplayer, quit);
        buttons.getChildren().add(buttonSelection);
        elements.add(buttons);
        mainMenuGroup.getChildren().addAll(elements);
    }


    public List<Node> getElements() {
        return elements;
    }

    public Group getMainMenuGroup() {
        return mainMenuGroup;
    }

    /**
     * Opens the multiplayer submenu.
     *
     * <p>
     * This method clears the current button selection and displays only the host and join buttons.
     * </p>
     */
    public void openMultiplayerMenu() {
        buttonSelection.getChildren().clear();
        buttonSelection.getChildren().addAll(host, join);
    }

}
