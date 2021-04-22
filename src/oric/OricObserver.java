package oric;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

import java.util.HashSet;
import java.util.Vector;

public class OricObserver implements Control {

    private static final String PAR_PROT="protocol";
    private static final String PAR_FILENAME_BASE = "file_base";

    private static Vector<Integer> visitied_height = new Vector<>();

    private static Vector<Long> BPD_tmp = new Vector<>();

    private static long all_block_num = 0;

    private final int pid;



    public OricObserver(String prefix){
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

        int height_interval_log = 100;


        if(block != null && block.getHeight()>0 && block.getHeight()%height_interval_log==0 && !visitied_height.contains(block.getHeight())){
            visitied_height.add(block.getHeight());

            //System.out.println("==");
            int wh = 0;
            float slot_num = 0; // slot的总数
            float block_total_num = 0; //根据slot计算的 block总数
            float edge_latency_num = 0; //边总延迟数
            float edge_total_num = 0; //边总数
            float block_latency_num = 0; //区块延迟总数
            float node_total_num = 0; //根据block计算的区块总数

            long end_time = block.getMinedTime();
            long start_time;

            int generate_block_num = 0;


            while(block!=null && wh < height_interval_log){
                Graph<Block> graph = new Graph<Block>();
                HashSet<Block> visitied = new HashSet<Block>();
                // System.out.printf("%s(%d)\n",block,block.getIncludeBlock().size());
                // for every mainblock , print the tree struct
                addToGraph(block,graph,visitied);

                graph.SetRoot(block);
                Vector<Block> L = graph.LogicSort2();
                //System.out.println(L);
                //System.out.println(graph.GetParallelSlotNum());
                //System.out.println(graph.GetEdgeNum());
                String[] a = graph.GetParallelSlotNum().split(":");
                String[] b = graph.GetEdgeNum().split(":");
                String[] c = graph.GetNodeLatency().split(":");
                slot_num += Float.parseFloat(a[0]);
                block_total_num += Float.parseFloat(a[1]);
                edge_latency_num += Float.parseFloat(b[0]);
                edge_total_num += Float.parseFloat(b[1]);

                block_latency_num += Float.parseFloat(c[0]);
                node_total_num += Float.parseFloat(c[1]);

                generate_block_num += Oric.height_gen_block_num.get(block.getHeight());

                block = block.getParent();
                //start_time = block.getMinedTime();
                wh++;
            }
            start_time = block.getParent()==null?0:block.getParent().getMinedTime();

            //System.out.println((end_time-start_time));

            double real_k = ((double)(end_time-start_time)/(double) (height_interval_log  *NetworkInitializer.getBlockTime()))*NetworkInitializer.getThroughputK();

            //System.out.printf("realK:%f \n",real_k);

            /*System.out.printf(
                            "current_log_height:%d,\n" +
                            "slot_num:%f\n" +
                            "block_total_num:%f\n" +
                            "edge_latency_num:%f\n" +
                            "edge_total_num:%f\n" +
                                    "block_latency_num:%f\n" +
                                    "node_total_num:%f\n" +
                                    "var1:%f\n" +
                                    "var2:%f\n" +
                                    "var2_realk:%f\n" +
                                    "var3:%f\n" +
                                    "var4:%f\n" +
                                    "var4_realk:%f\n" +
                                    "var5:%f\n" +
                                    "var6:%f\n" +
                                    "var6_realk:%f\n" +
                                    "var7:%f\n" +
                                    "var7_realk:%f\n",
                    block.getHeight(),
                    slot_num,
                    block_total_num,
                    edge_latency_num,
                    edge_total_num,
                    block_latency_num,
                    node_total_num,
                    block_total_num/slot_num,
                    block_total_num/(slot_num*NetworkInitializer.getThroughputK()),
                    block_total_num/(slot_num*real_k),
                    edge_latency_num/edge_total_num,
                    edge_latency_num/(edge_total_num* NetworkInitializer.getThroughputK()),
                    edge_latency_num/(edge_total_num* real_k),
                    block_latency_num/node_total_num,
                    block_latency_num/(node_total_num*NetworkInitializer.getThroughputK()),
                    block_latency_num/(node_total_num*real_k),
                    block_total_num/(NetworkInitializer.getThroughputK()*height_interval_log),
                    block_total_num/(real_k*height_interval_log)
            );*/
            //System.out.println("==");

            System.out.println(block_total_num/(real_k*height_interval_log));

            // 计算BPD的均值
            long BPD_mean = 0;
            long total_BPD = 0;
            for(int i = 0;i<BPD_tmp.size();i++){
                total_BPD += BPD_tmp.get(i);
            }
            BPD_mean = total_BPD/BPD_tmp.size();
            //System.out.println("BPD_mean:"+BPD_mean);


            //System.out.println("generated_block_mean_num:"+ generate_block_num/height_interval_log);

            //all_block_num += block_total_num;
            //System.out.println("Throughput:"+ all_block_num*200000*8/(CommonState.getTime()/1000) );

            BPD_tmp.clear();

            if(block.getHeight()>=900){
                System.exit(0);
            }

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


}
