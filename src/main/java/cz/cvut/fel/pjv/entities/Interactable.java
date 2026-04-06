package cz.cvut.fel.pjv.entities;

/**
 * The Interactable interface should be implemented by any class whose objects
 * can interact with the player or other entities within the game.
 *
 * <p>
 * It requires implementing methods to perform an interaction, retrieve the interaction
 * distance, and the interaction instruction text.
 * </p>
 */
public interface Interactable {
    void interact();
    double getInteractionDistance();
    String getInteractionText();
}
