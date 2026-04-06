package cz.cvut.fel.pjv;

/**
 * MouseClick represents the state of the mouse click event.
 *
 * <p>
 * It records whether the mouse is pressed and, if so, the corresponding coordinates on screen,
 * where the mouse was pressed.
 * </p>
 */
public class MouseClick {

    public boolean mousePressed = false;
    public double mouseX = 0, mouseY = 0;

    public MouseClick(boolean isPressed, double mouseX, double mouseY) {
        this.mousePressed = isPressed;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}
