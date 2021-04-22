package oric;

import peersim.core.Node;

public class BlockMsg extends SimpleMsg{

    private final Block block;

    public BlockMsg(int type, Node sender, Block block){
        super(type,sender);
        this.block = block;
    }

    public Block getBlock(){
        return this.block;
    }

}
