package cz.cvut.fel.pjv;
import cz.cvut.fel.pjv.entities.Entity;


/// This class is used to send a request to the server to play a sound
public class ShareSound extends Entity {

    public String sound;

    public ShareSound(String sound) {
        super(0, 0, 0, 0, "empty");
        this.sound = sound;
    }
}
