package oric;

import peersim.core.Node;

/**
 * This class defines a simple message.
 * A simple message has its type and the reference of the sender node.
 * @see SimpleEvent
 */
public class SimpleMsg extends SimpleEvent {

    /**
     * The sender of the message.
     */
    protected Node sender;

    public SimpleMsg(){
    }

    /**
     * Initializes the simple message with its type and sender.
     * @param type The identifier of the type of the message
     * @param sender The sender of the message
     */
    public SimpleMsg(int type, Node sender){
        super.type = type;
        this.sender = sender;
    }

    /**
     * Gets the sender of the message.
     * @return The sender of the message.
     */
    public Node getSender(){
        return this.sender;
    }
}
