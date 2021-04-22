package oric;


import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class NetworkAdjust implements Control {
    private static final String PAR_PROT="protocol";
    private static final String PAR_TRANSPORT="transport";

    private static final String ADJUST_TIME="adjust_time";
    private static final String ASSUME_BADNWIDTH="assume_bandwidth";
    private static final String ASSUME_BLOCK_SIZE="assume_blocksize";
    private static final String ZETA="zeta";
    private static double guess_bandwidth;

    private final int pid;
    private final int tid;
    private final long adjust_time;

    // Unit: Mbps;
    private static int assume_bandwidth;
    // Unit: KB;
    private static int assume_blocksize;
    // zeta
    private final double zeta;




    public NetworkAdjust(String prefix){
        pid = Configuration.getPid(prefix+"."+PAR_PROT);
        tid = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
        adjust_time = Configuration.getLong(prefix +"."+  ADJUST_TIME);
        assume_bandwidth = Configuration.getInt(prefix +"."+ ASSUME_BADNWIDTH);
        assume_blocksize = Configuration.getInt(prefix +"." + ASSUME_BLOCK_SIZE);
        zeta = Configuration.getDouble(prefix+"."+ZETA);
    }

    private static Integer last_gen_block_num = 0;
    @Override
    public boolean execute() {
        int current_gen_block_num = Oric.gen_block_num;
        if(current_gen_block_num == 0){
            return false;

        }

        Block block = ((Oric)Network.get(0).getProtocol(pid)).now_block;

        if(block != null //block is not null
                && block.getHeight()==5000){
                assume_bandwidth = 8000000;
        }

        if(block != null
                && block.getHeight()==10000){
            assume_bandwidth = 16000000;
        }



        //int net_min = UniformRandomTransport2
        Node node = Network.get(0);
        long minStatic_latency = ((UniformRandomTransport2)node.getProtocol(tid)).getStatic_min();
        long maxStatic_latency = ((UniformRandomTransport2)node.getProtocol(tid)).getStatic_max();



        // guess the latest 10s transaction bandwidth：
        // total size / time
        long total_size = (long) (current_gen_block_num - last_gen_block_num) *assume_blocksize*8;
        guess_bandwidth = total_size/(adjust_time/1000);

        //System.out.println(CommonState.getTime()+":"+guess_bandwidth);
        //System.exit(0);

        double K = assume_bandwidth/2;
        if(guess_bandwidth > K){
            Double aDouble1 = new Double(Math.pow((guess_bandwidth - K)/100000,2) * zeta);
            long min_latency = minStatic_latency + aDouble1.longValue();
            long max_latency = maxStatic_latency + aDouble1.longValue();
            ((UniformRandomTransport2)node.getProtocol(tid)).setMinMaxDelay(min_latency,max_latency);
        }else{
            ((UniformRandomTransport2)node.getProtocol(tid)).setMinMaxDelay(minStatic_latency,maxStatic_latency);
        }

        //System.out.println("latency:" +((UniformRandomTransport2)node.getProtocol(tid)).getMinDelay()+" "+
        //        ((UniformRandomTransport2)node.getProtocol(tid)).getMaxDelay());

        last_gen_block_num = current_gen_block_num;
        return false;
    }

    public static double get_TB(){
        return guess_bandwidth;
    }

    public static double get_AB(){
        return assume_bandwidth;
    }
}
