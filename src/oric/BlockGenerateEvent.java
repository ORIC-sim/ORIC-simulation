package oric;

import peersim.core.CommonState;
import peersim.core.Node;

import java.math.BigInteger;

public class BlockGenerateEvent extends SimpleEvent {
    private final Block block;
    private final long delay;

    private final int id;
    private static int latest_id = 0;

    public BlockGenerateEvent(int type, long this_power, int height, Node miner, Block parent){
        super(type);
        block = new Block(height,miner,parent);
        delay = computeDelay(block.getDifficulty().divide(BigInteger.valueOf(block.getThroughputK())), this_power);
        int block_type = CommonState.r.nextLong(block.getThroughputK()) < 1.0 ? 0 :  1;
        block.setType(block_type);
        id = latest_id;
        latest_id ++;
    }

    public Block getBlock(){
        return this.block;
    }

    public long getDelay(){
        return this.delay;
    }

    public int getId(){
        return this.id;
    }

    private long computeDelay(BigInteger difficulty, long mining_power){


        double p = 1.0 / difficulty.doubleValue();
        double u = CommonState.r.nextDouble();
        return p <= Math.pow(2, -53) ? -1:(long) (Math.log(u) / Math.log(1.0 - p) / mining_power);
    }

}
