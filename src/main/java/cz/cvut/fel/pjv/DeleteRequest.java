package cz.cvut.fel.pjv;
import cz.cvut.fel.pjv.entities.Entity;


/**
 * DeleteRequest is used to send a request to the server to delete an entity.
 */
public class DeleteRequest extends Entity {

    public long deleteId;

    public DeleteRequest(long deleteId) {
        super(0, 0, 0, 0, "empty");

        this.deleteId = deleteId;
    }
}
