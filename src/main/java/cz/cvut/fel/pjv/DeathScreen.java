package cz.cvut.fel.pjv;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * DeathScreen sets up the user interface displayed when the player loses the game.
 *
 * <p>
 * This screen includes a label "You have lost" and buttons to either return
 * to the menu or quit the game. The visual elements are constructed using JavaFX components.
 * </p>
 */
public class DeathScreen {

    private final double BUTTON_WIDTH = 250;
    private final double BUTTON_HEIGHT = 60;

    private final double BUTTON_SELECTION_WIDTH = 300;
    private final double BUTTON_SELECTION_HEIGHT = 300;
    private final double BUTTON_SELECTION_SPACING = 2.87;

    private Group deathScreenGroup = new Group();
    private List<Node> elements = new ArrayList<>();
    private StackPane buttons = new StackPane();
    private VBox buttonSelection = new VBox();


    private final Button toMenu;
    private final Button quit;

    /**
     * Constructs the DeathScreen UI.
     */
    public DeathScreen()
    {
        /// Death Label
        Label deathLabel = new Label("YOU HAVE LOST");
        deathLabel.setFont(new Font("Arial", 60));
        deathLabel.setTextFill(Color.WHITE);


        /// Buttons
        toMenu = new Button();
        toMenu.setText("Back To Menu");
        toMenu.setOnMouseClicked(Game.inputHandler::onToMenu);
        toMenu.setOnKeyPressed(Game.inputHandler::onToMenu);
        toMenu.setPrefWidth(BUTTON_WIDTH);
        toMenu.setPrefHeight(BUTTON_HEIGHT);


        quit = new Button();
        quit.setText("Quit");
        quit.setOnMouseClicked(Game.inputHandler::onQuitButton);
        quit.setOnKeyPressed(Game.inputHandler::onQuitButton);
        quit.setPrefWidth(BUTTON_WIDTH);
        quit.setPrefHeight(BUTTON_HEIGHT);



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
        buttonSelection.getChildren().addAll(deathLabel, quit);
        buttons.getChildren().add(buttonSelection);
        elements.add(buttons);
        deathScreenGroup.getChildren().addAll(elements);
    }

    /**
     * Returns the root group of the death screen UI.
     *
     * @return Group containing all components of the death screen.
     */
    public Group getDeathScreenGroup()
    {
        return deathScreenGroup;
    }
}
