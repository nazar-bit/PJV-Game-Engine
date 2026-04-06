package cz.cvut.fel.pjv.entities;


import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

/**
 * The CustomCursor class provides functionality for tracking the mouse cursor's position
 * within a JavaFX scene.
 *
 * <p>
 * It listens for mouse movement events and updates the x and y coordinates accordingly.
 * </p>
 */
public class CustomCursor{

    private double x;
    private double y;

    public CustomCursor() {

    }

    public void updatePosition(Scene scene)
    {
        scene.setOnMouseMoved((MouseEvent event) -> {
            this.x = event.getX();
            this.y = event.getY();
        });
    }


}
