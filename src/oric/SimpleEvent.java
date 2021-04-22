package oric;

/**
 * This class defines a simple event. A simple event is characterized only
 * by its type.
 */
public class SimpleEvent {

    /**
     * The identifier of the type of the event.
     * <p>
     * The available identifiers for event type are:<br/>
     * <ul>
     *  <li>1 is BLOCK message</li>
     *  <li>2 is BLOCK_GEN message</li>
     *</ul></p>
     */
    protected int type;

    public SimpleEvent(){
    }

    /**
     * Initializes the type of the event.
     * @param type The identifier of the type of the event
     */
    public SimpleEvent(int type){
        this.type = type;
    }

    /**
     * Gets the type of the event.
     * @return The type of the current event.
     */
    public int getType(){
        return this.type;
    }
}
