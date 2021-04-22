package oric;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Vector;

public class Block {

    private int type = -1;

    private final int height;
    private final Node miner;
    private long minedTime = 0;
    private final Block parent;
    private final HashSet<Block> include_block;
    private final BigInteger difficulty;
    private final long throughput_k;


    /*
    test field
     */
    private long BPD = 0;

    private final int id;
    private static int latest_id = 0;

    public Block(int height, Node miner, Block parent){
        this.height = height;
        this.miner = miner;
        this.include_block = new HashSet<Block>();
        this.parent = parent;
        this.difficulty = BigInteger.valueOf(NetworkInitializer.getTotalMiningPower()*NetworkInitializer.getBlockTime());
        this.throughput_k = NetworkInitializer.getThroughputK();

        this.id = latest_id;
        latest_id ++;
    }

    public int getHeight(){
        return this.height;
    }
    public Node getMiner(){
        return this.miner;
    }
    public Block getParent() {
        return this.parent;
    }
    public BigInteger getDifficulty(){
        return this.difficulty;
    }
    public long getThroughputK(){
        return this.throughput_k;
    }

    public HashSet<Block> getIncludeBlock() {
        return  this.include_block;
    }
    public void insertIncludeBlock(Block block){
        this.include_block.add(block);
    }
    public void removeIncludeBlock(Block block){
        this.include_block.remove(block);
    }

    public boolean isInIncludeBlock(Block block){
        return include_block.contains(block);
    }

    public void setMinedTime(){
        this.minedTime = CommonState.getTime();
    }
    public long getMinedTime(){
        return this.minedTime;
    }

    public void setBPD(long arrive_time){
        if((arrive_time-minedTime)>BPD){
            BPD = arrive_time-minedTime;
        }
    }

    public long getBPD(){
        return BPD;
    }


    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }
    public void setType(int block_type) {
        this.type = block_type;
    }

    public Block getBlockWithHeight(int height){
        if(this.height == height){
            return this;
        }else{
            return this.parent.getBlockWithHeight(height);
        }
    }

    public boolean isOnSameChainAs(Block block){
        if(block == null){
            return false;
        }else if (this.getHeight() <= block.getHeight()){

            return this.equals(block.getBlockWithHeight(this.height));
        } else {
            return this.getBlockWithHeight(block.height).equals(block);
        }
    }

    @Override
    public String toString(){
        return this.getHeight()+"-"+this.getType()+"-"+this.getId();
    }

}
