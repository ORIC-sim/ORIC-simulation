package oric;


import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;

public class DataWriter implements Control {

    private static final String PAR_PROT="protocol";
    private static final String PAR_TRANSPORT="transport";
    private static final String PAR_FILENAME_BASE = "file_base";

    private final int pid;
    private final int tid;

    private final String graph_filename;
    private final MyFileNameGen fng;
    private PrintStream pstr;
    private FileOutputStream fos;



    public DataWriter(String prefix){
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        graph_filename = Configuration.getString(prefix + "."
                + PAR_FILENAME_BASE, "graph_dump");
        fng = new MyFileNameGen(graph_filename, ".dat");
        tid = Configuration.getPid(prefix+"."+PAR_TRANSPORT);

        // write
        try{
            String fname = fng.nextCounterName(""+System.currentTimeMillis());
            fos = new FileOutputStream(fname);
            System.out.println("Writing to file " + fname);
            pstr = new PrintStream(fos);

            //graphToFile( pstr, pid,tid);

            //fos.close();

        }catch (IOException e) {
            throw new RuntimeException(e);
        }




    }

    @Override
    public boolean execute() {

        graphToFile(pstr,pid,tid);
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    private static void graphToFile(PrintStream ps, int pid, int tid) {
        Node node = Network.get(0);

        //time
        long time = CommonState.getTime();

        if(time==0){
            return;
        }

        //System.out.println(time);


        //height
        if(((Oric)node.getProtocol(pid)).getNowBlock()==null){
            return;
        }

        int height = ((Oric)node.getProtocol(pid)).getNowBlock().getHeight();

        // Min latency
        long min_latency = ((UniformRandomTransport2)node.getProtocol(tid)).getMinDelay();
        long max_latency = ((UniformRandomTransport2)node.getProtocol(tid)).getMaxDelay();

        //BPD
        HashSet<Block> visitied = new HashSet<Block>();
        visitAllBlocks(((Oric)node.getProtocol(pid)).getNowBlock(),visitied);
        long BPD_all = 0;
        for(Block b : visitied){
            BPD_all += b.getBPD();
        }

        long BPD_mean = BPD_all / visitied.size();

        //CB
        double CB = NetworkAdjust.get_TB();

        //AB
        double AB = NetworkAdjust.get_AB();

        //k
        long k = NetworkInitializer.getThroughputK();

        //tau
        double tau = OricObserverAd.getTau();



        ps.println(time+"," +
                height+"," +
                min_latency+"," +
                max_latency+"," +
                BPD_mean+"," +
                CB+"," +
                AB+"," +
                k+"," +
                tau+"," +
                ((Oric)node.getProtocol(pid)).getNowBlock().getMiner().getID()+"," +
                ((Oric)node.getProtocol(pid)).getNowBlock().getMinedTime()+"," +
                ((Oric)node.getProtocol(pid)).getNowBlock().getType());
    }

    public static void  visitAllBlocks(Block block,  HashSet<Block> visited) {
        visited.add(block);
        for(Block child:block.getIncludeBlock()){
            if(!visited.contains(child) && child.getParent() == block.getParent()){
                visitAllBlocks(child,visited);
            }
        }
    }

}
