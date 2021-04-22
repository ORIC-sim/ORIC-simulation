package oric;


import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import java.util.*;


public class Oric implements EDProtocol {

    /* ===========================
                   Config
     ============================*/

    private final static String PAR_TRANSPORT = "transport";


    /* ===========================
            Field
     =============================*/

    // cache size
    private final static int block_cache_size = 5000;
    private final static int block_processed_size = 5000;

    // Cache(for receiving blocks)
    private Cache<Block> block_cache = new Cache<Block>(block_cache_size);

    // transport id
    int tid;

    // now block
    Block now_block = null;
    // orphan blocks
    Set<Block> orphan_blocks = new HashSet<Block>();

    // mining power
    private long mining_power=0;

    // mining event
    private BlockGenerateEvent ming_event = null;


    // Cache (for recent processed blocks)
    private Cache<Block> block_processed = new Cache<Block>(block_processed_size);
    // blocks waiting for processing
    private Vector<Block> block_need_process = new Vector<Block>();

    // generate the block number corresponding block height(include orphan blocks)
    public static Map<Integer,Integer> height_gen_block_num = new HashMap<>();
    // generate block number (include orphan blocks)
    public static Integer gen_block_num = 0;


    public Oric(String prefix){ // Used for the tracker's protocol
        tid = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
    }


    /**
     * The standard method that processes incoming events.
     * @param node reference to the local node for which the event is going to be processed
     * @param pid Oric's protocol id
     * @param event the event to process
     */

    public void processEvent(Node node, int pid, Object event) {

        switch(((SimpleEvent)event).getType()){

            //======================BLOCK_MSG================
            case MsgType.BLOCK_MSG: {

                BlockMsg blockMsg = (BlockMsg)event;
                Node sender = blockMsg.getSender();
                Block block = blockMsg.getBlock();
                this.receiveBlock(node, pid, block ,sender);
            //======================BLOCK_MSG================
            } break;

            //=====================BLOCK_GEN_EVT=================
            case MsgType.BLOCK_GEN_EVT: {
                BlockGenerateEvent blockGenEvt = (BlockGenerateEvent) event;
                ((BlockGenerateEvent) event).getBlock().setMinedTime();



                if (blockGenEvt.getId() != this.ming_event.getId()){
                    // System.out.println("here---");
                    return;
                }


                if( height_gen_block_num.containsKey(((BlockGenerateEvent) event).getBlock().getHeight())){
                    height_gen_block_num.put(((BlockGenerateEvent) event).getBlock().getHeight(),
                            height_gen_block_num.get(((BlockGenerateEvent) event).getBlock().getHeight())+1);
                }else{
                    height_gen_block_num.put(((BlockGenerateEvent) event).getBlock().getHeight(),1);
                }
                gen_block_num += 1;


                // if block is mined by self
                if( blockGenEvt.getBlock().getMiner() != node ) {
                    //should never happen
                    System.err.println("never happen(1)");
                    System.exit(0);
                }

                Block block = ((BlockGenerateEvent) event).getBlock();

                if(this.now_block !=null){
                    if(Math.abs(this.now_block.getHeight()-block.getHeight())>20){
                        return;
                    }
                }

                this.receiveBlock(node,pid,block,node);


            //=====================BLOCK_GEN_EVT=================
            } break;

        }
    }

    @Override
    /**
     * TODO
     * this method need deep clone.
     */
    public Object clone() {
        Object prot = null;
        try{
           prot = (Oric) super.clone();
        }catch (CloneNotSupportedException e){ };

        assert prot != null;
        ((Oric)prot).block_cache = new Cache<Block>(block_cache_size);
        ((Oric)prot).now_block = null;
        ((Oric)prot).mining_power = 0;
        ((Oric)prot).ming_event = null;
        ((Oric)prot).block_processed = new Cache<Block>(block_processed_size);
        ((Oric)prot).block_need_process = new Vector<Block>();
        ((Oric)prot).orphan_blocks = new HashSet<Block>();

        return prot;
    }

    public long  getMiningPower(){
        return this.mining_power;
    }
    public void setMiningPower(long mining_power){
        this.mining_power = mining_power;
    }
    public  void setMiningEvent(BlockGenerateEvent e){
        this.ming_event = e;
    }

    public void broadcastMsg(Node node,int pid, Object event,Node sender){
        Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
        if (linkable.degree() > 0) {
            for(int i = 0; i < linkable.degree(); i++){
                Node peern = linkable.getNeighbor(i);
                if(!peern.isUp() || peern.getID() == sender.getID())
                    continue;
                long latency = ((Transport)node.getProtocol(tid)).getLatency(node, peern);
                EDSimulator.add(latency,event,peern,pid);
                //((Transport)node.getProtocol(FastConfig.getTransport(pid))).send(node,peern,new BlockMsg(MsgType.BLOCK_MSG,node,block), pid);
            }
        }
    }

    public void receiveBlock(Node node, int pid, Block block ,Node sender){

        if (this.block_cache.hasElements(block)){
            return;
        }else{
            block.setBPD(CommonState.getTime());
            //System.out.printf("Node %d receive Block id:%d, type:%d height:%d- miner:%d \n",node.getID(),block.getId(),block.getType(),block.getHeight(),block.getMiner().getID());
            this.block_cache.putIntoCache(block);
            block_need_process.insertElementAt(block,0);
            processBlock(node, pid, sender);
            this.block_processed.putIntoCache(block);
            // forward
            this.broadcastMsg(node,pid,new BlockMsg(MsgType.BLOCK_MSG,node,block),sender);
        }
    }

    public Block getNeedProcessBlock(){
        Block tmp_block = null;
        for (Block block : block_need_process ){
            boolean flag = true;
            if (!block_processed.hasElements(block.getParent()) && block.getParent() != null ){
                continue;
            }

            for( Block in_block : block.getIncludeBlock()){
                if (!block_processed.hasElements(in_block)){
                    flag = false;
                    break;
                }
            }
            if(flag){
                tmp_block = block;
                break;
            }
        }
        block_need_process.remove(tmp_block);
        return tmp_block;
    }

    public void processBlock(Node node, int pid, Node sender){
        Block block = getNeedProcessBlock();

        //int k = 0;
        while(block != null){
            //k++;
            // Verify correctness of the block

            //=================================
            if(block.getType() == 0) {
                //System.out.printf("main block include %d\n",block.getIncludeBlock().size());
                // main block
                if(this.now_block != null && !this.now_block.isOnSameChainAs(block)){
                    this.addOrphans(this.now_block, block);
                }
                this.addToChain(block);
                // new mining task
                newMining(node,pid);
            }else if (block.getType() == 1){
                //sub block
                if(block.getParent() == this.now_block || block.getParent() == null){
                    // if the block has been added in the front sub, then its inneccessary for this subblock
                    //if(! block_processed.hasElements(block)){
                    if(block.getMiner()==sender){
                        newMining(node,pid);
                        this.ming_event.getBlock().insertIncludeBlock(block);
                    }else{

                        for(Block con_block: block.getIncludeBlock()){
                            if(this.ming_event.getBlock().isInIncludeBlock(con_block)){
                                this.ming_event.getBlock().removeIncludeBlock(con_block);
                            }
                        }
                        this.ming_event.getBlock().insertIncludeBlock(block);
                    }
                    //}
                    //newMining(node,pid);
                }else{
                   // System.out.println("dropped sub block");
                }
            }else{
                //never happen
                System.err.println("never happen(2)");
                System.exit(0);
            }

            block_processed.putIntoCache(block);
            block = getNeedProcessBlock();
        }
        //System.out.printf("get need process:%d, nedd process: %d \n",k,block_need_process.size());

    }

    public void addOrphans(Block orphan_block, Block valid_block) {
        if(orphan_block != valid_block){
            this.orphan_blocks.add(orphan_block);
            this.orphan_blocks.remove(valid_block);
            if (valid_block==null || orphan_block.getHeight() > valid_block.getHeight()) {
                this.addOrphans(orphan_block.getParent(), valid_block);
            } else if (orphan_block.getHeight() == valid_block.getHeight()){
                this.addOrphans(orphan_block.getParent(),valid_block.getParent());
            } else {
                this.addOrphans(orphan_block, valid_block.getParent());
            }
        }
    }

    public void addToChain(Block block){
        this.now_block = block;
    }

    public void newMining(Node node,int pid){
        BlockGenerateEvent mining_evt = new BlockGenerateEvent(
                MsgType.BLOCK_GEN_EVT,
                this.getMiningPower(),
                this.now_block == null? 0: this.now_block.getHeight()+1,
                node,
                this.now_block
        );
        this.ming_event = mining_evt;
        EDSimulator.add(mining_evt.getDelay(),mining_evt,node,pid);
    }

    public Block getNowBlock(){
        return this.now_block;
    }
}
