package oric;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import java.util.Random;

public class NetworkInitializer implements Control {

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT="protocol";

    private static final String PAR_TRANSPORT="transport";

    private final static String PAR_TOTAL_MINING_POWER = "total_mining_power";

    private final static String PAR_BLOCK_TIME = "block_time";

    private final static String PAR_THROUHPUT_K = "throughput_k";


    /** Protocol identifier, obtained from config property */
    private final int pid;
    private final int tid;

    private Random rnd;

    // block time
    private static long block_time;

    // total mining power
    private static long total_mining_power;

    // throughput parameter k
    private static long throughput_k;


    public NetworkInitializer(String prefix) {
        pid = Configuration.getPid(prefix+"."+PAR_PROT);
        tid = Configuration.getPid(prefix+"."+PAR_TRANSPORT);

        block_time = Configuration.getLong(prefix+"."+PAR_BLOCK_TIME);
        total_mining_power = Configuration.getLong(prefix + "." + PAR_TOTAL_MINING_POWER);
        throughput_k = Configuration.getLong(prefix + "." + PAR_THROUHPUT_K);
    }

    @Override
    public boolean execute() {

        // allocate computing power for each protocol Oric;
        for(int i = 0 ; i < Network.size() ; i++){
            ((Oric)Network.get(i).getProtocol(pid)).setMiningPower( getTotalMiningPower()/Network.size() );

            BlockGenerateEvent block_gen_evt = new BlockGenerateEvent(MsgType.BLOCK_GEN_EVT,
                    ((Oric)Network.get(i).getProtocol(pid)).getMiningPower(),
                    0,
                    Network.get(i),
                    null
            );
            ((Oric)Network.get(i).getProtocol(pid)).setMiningEvent(block_gen_evt);
            EDSimulator.add(block_gen_evt.getDelay(),block_gen_evt,Network.get(i),pid);
        }


//        Node beginer = Network.get(0);
//        ((Transport)beginer.getProtocol(FastConfig.getTransport(pid))).send(
//                beginer,
//                Network.get(1),
//                new BlockMsg(
//                        MsgType.BLOCK_MSG,
//                        beginer,
//                        new Block(0,beginer)
//                ),
//                pid);



        //System.out.println("here");

        return false;
    }

    static public long getTotalMiningPower(){
        return total_mining_power;
    }

    static public long getBlockTime(){
        return block_time;
    }

    static public long getThroughputK(){
        return throughput_k;
    }

    static public void setThroughputK(long new_k){
        throughput_k = new_k;
    }
}
