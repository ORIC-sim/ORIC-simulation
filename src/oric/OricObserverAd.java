package oric;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

import java.util.HashSet;
import java.util.Vector;

public class OricObserverAd implements Control {

    private static final String PAR_PROT="protocol";
    private static final String PAR_FILENAME_BASE = "file_base";

    private static Vector<Integer> visitied_height = new Vector<>();

    public static Vector<Long> BPD_tmp = new Vector<>();

    private static double last_tau = 0.5;

    private final int pid;



    public OricObserverAd(String prefix){
        pid = Configuration.getPid(prefix + "." + PAR_PROT);

        System.out.println("run type delay:");
        System.out.println(Configuration.getString("protocol.urt.mindelay"));
        System.out.println(Configuration.getString("protocol.urt.maxdelay"));
        System.out.println("run type k:");
        System.out.println(Configuration.getString("THROUGHPUT"));
    }

    @Override
    public boolean execute() {

        Block block = ((Oric)Network.get(0).getProtocol(pid)).now_block;

        int height_interval_log = 200;

        if(block != null
                && block.getHeight()>0 //
                && block.getHeight()%height_interval_log==0 //
                && !visitied_height.contains(block.getHeight())){
            visitied_height.add(block.getHeight());


            /*
              begin
             */
            int end_height = block.getHeight();
            int start_height = end_height - height_interval_log;

            //get block height list
            Vector<Block> block_vec = new Vector<>();
            for(int i = start_height; i<end_height;i++ ) {
                block_vec.insertElementAt(block.getParent(),0);
                block = block.getParent();
            }//[0,1,2,.....height_interval_log-1],[height_interval_log,.......,2*height_interval_log-1]

            long start_time=0;
            long end_time=0;

            int block_total_num = 0;
            for(int i = start_height; i<end_height;i++ ){
                int index = i%height_interval_log;
                Block current_block = block_vec.get(index);
                Graph<Block> graph = new Graph<Block>();
                HashSet<Block> visitied = new HashSet<Block>();
                addToGraph(current_block,graph,visitied);

                block_total_num+=graph.GetBlockNumber();

                if(current_block.getHeight()==0){
                    start_time = 0;

                }else{
                    if(index==0){
                        start_time = current_block.getParent().getMinedTime();
                    }
                    if(index==height_interval_log-1){
                        end_time = current_block.getMinedTime();
                    }
                }

            }


            double real_k = ((double)(end_time-start_time)/(double) (height_interval_log  *NetworkInitializer.getBlockTime()))*NetworkInitializer.getThroughputK();



            // Throughput adjust algo(start)
            double tau_new = block_total_num/(real_k*height_interval_log);
            long old_k = NetworkInitializer.getThroughputK();

            System.out.println("old_k " + old_k);
            System.out.println("last_tau " + last_tau);
            System.out.println("tau_new " + tau_new);

            double delta = 0.01;

            if(tau_new-last_tau>delta){
                long new_k = old_k + (long) (((tau_new - last_tau)/0.01)*old_k*0.04);
                new_k = (long) Math.min(old_k*1.1,new_k);
                NetworkInitializer.setThroughputK(new_k);
            }
            if(Math.abs(tau_new-last_tau)<=delta){
                long new_k = (long) (old_k+old_k*0.04);
                NetworkInitializer.setThroughputK(new_k);
            }
            if(last_tau-tau_new>delta){
                long new_k = old_k - (long) (((last_tau - tau_new )/0.01)*old_k*0.04);
                new_k = (long) Math.max(old_k*0.7,new_k);
                new_k = new_k<=0?1:new_k;
                NetworkInitializer.setThroughputK(new_k);
            }
            last_tau = tau_new;

            System.out.println("new_k " + NetworkInitializer.getThroughputK());




            // Throughput adjust algo(end)

            /*
               end
             */

        }



        return false;
    }

    public static void  addToGraph(Block block, Graph<Block> graph,HashSet<Block> visited){
        graph.AddNode(block);
        visited.add(block);
        BPD_tmp.add(block.getBPD());
        //System.out.print(block.getBPD()+" ");
        for(Block child:block.getIncludeBlock()){
            if(!visited.contains(child) && child.getParent() == block.getParent()){
                graph.AddNode(child);
                graph.AddEdge(block,child);
                addToGraph(child,graph,visited);
            }
        }
    }

    public static double getTau(){
        return last_tau;
    }
}
