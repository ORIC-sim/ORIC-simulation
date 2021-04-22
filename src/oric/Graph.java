package oric;

import org.nfunk.jep.function.Str;

import java.util.*;

public class Graph<T> {
    //private Vector<T> nodeList;
    //private Map<T,Integer> lookup;

    private Map<T,Vector<T>> nodeMap;
    private Map<T,Vector<T>> indegreeMap;

    private Map<T,Vector<T>> nodeMap_clone;
    private Map<T,Vector<T>> indegreeMap_clone;


    private Map<T,Integer> slotMap;
    private Map<Integer,HashSet<T>> slotMap_verse;

    private Map<T,Integer> latencyMap;

    private T root = null;
    private HashSet<T> noIndegreeNodes;
    private HashSet<T> noOutDegreeNodes;

    private HashSet<T> noIndegreeNodes_clone;
    private HashSet<T> noOutDegreeNodes_clone;

    private HashSet<T> usefulSet;

    public Graph(){
        //this.nodeList = new Vector<T>();
        //this.lookup = new HashMap<>();
        this.nodeMap = new HashMap<T, Vector<T>>();
        this.indegreeMap = new HashMap<>();
        this.slotMap = new HashMap<>();
        this.slotMap_verse = new HashMap<>();

        this.latencyMap = new HashMap<>();
        this.noIndegreeNodes = new HashSet<>();
        this.noOutDegreeNodes = new HashSet<>();
        this.usefulSet = new HashSet<>();
    }

    private boolean haveNode(T node){
        return this.nodeMap.containsKey(node);
        //return lookup.containsKey(node);
    }

    public void AddNode(T node){
        if(this.haveNode(node)){
            //System.out.println("have such node");
            return;
        }

        //this.nodeList.add(node);
        this.nodeMap.put(node, new Vector<T>());
        this.indegreeMap.put(node, new Vector<>());

        //this.lookup.put(node, this.nodeList.size()-1);
        this.noIndegreeNodes.add(node);
        this.noOutDegreeNodes.add(node);
    }

    public void AddEdge(T node1, T node2){
        if(!this.haveNode(node1) || !this.haveNode(node2)){
            System.err.println("edge add error");
            return;
        }

        for(T node : this.nodeMap.get(node1) ){
            if(node==node2){
                //System.out.println("already add this edge");
                return;
            }
        }

        if(node1==node2){
            System.err.println("same node");
        }

        if(this.nodeMap.get(node2)!=null){
            if(this.nodeMap.get(node2).contains(node1)){
                System.err.println("cycle edge");
            }
        }

        this.nodeMap.get(node1).add(node2);
        this.indegreeMap.get(node2).add(node1);
        this.noIndegreeNodes.remove(node2);
        this.noOutDegreeNodes.remove(node1);
    }

    public void SetRoot(T node){
        if(!this.haveNode(node)){
            System.err.println("no such node");
        }
        this.root=node;
        this.indegreeMap.put(node,new Vector<>());
        this.noIndegreeNodes.clear();
        this.noIndegreeNodes.add(node);
    }

    public Vector<T> Child(T node){
        if(!this.haveNode(node)){
            System.err.println("err, no such node");
        }
        return this.nodeMap.get(node);
    }


    private void makeUsefulItem(Vector<T> set, HashMap<T, Boolean> visited){
        if(set.size()>0){
            for(T node:set){
                this.usefulSet.add(node);
                if(visited.get(node) == null || !visited.get(node)){
                    this.makeUsefulItem(Child(node),visited);
                    visited.put(node,true);
                }
            }
        }
    }

    public void MakeUsefulItem(){
        usefulSet.clear();
        usefulSet.add(this.root);
        HashMap<T,Boolean> visited = new HashMap<>();
        this.makeUsefulItem(Child(root),visited);
    }

    private int contain(Vector<T> v, T i ){
        for(int k =0;k<v.size();k++){
            if(i==v.get(k)){
                return k;
            }
        }
        return -1;
    }

    // how to convert compare function in JAVA.
//    public Vector<T> LogicSort(){
//        this.MakeUsefulItem();
//
//        Vector<T> L = new Vector<>();// result set
//        LinkedHashSet<T> S = new LinkedHashSet<>();// node set that in-degree = 0
//        S.add(root);
//
//        int slot = 0;
//
//        slotMap.put(root,slot);
//
//        while(S.size()>0){
//            Vector<T> L2 = new Vector<>();
//            T node = S.iterator().next();
//            L.add(node);
//            S.remove(node);
//
//
//            for(Map.Entry<T,Vector<T>> entry:this.indegreeMap.entrySet()){
//                T key = entry.getKey();
//                Vector<T> value = entry.getValue();
//
//                if(!L.contains(key) && this.usefulSet.contains(key)){
//                    int co = contain(value,node);
//
//                    if(co!=-1){
//                        indegreeMap.get(key).remove(co);
//                    }
//
//                    int i_ = 0;
//                    for(int id =0 ; id < indegreeMap.get(key).size();id++ ){
//                        if(!usefulSet.contains(node)){
//                            indegreeMap.get(key).remove(id-i_);
//                        }
//                    }
//
//                    if(indegreeMap.get(key).isEmpty()){
//                        L2.add(key);
//                    }
//                }
//            }
//            // sort for L2
//
//            /////////////
//
//            // calculate slot
//            boolean flag = true;
//            for(T node_:L2){
//                if(!slotMap.containsKey(node_)){
//                    if(flag){
//                        slot++;
//                        flag=false;
//                    }
//                    slotMap.put(node,slot);
//                }
//            }
//
//
//            S.addAll(L2);
//
//        }
//
//        for(Map.Entry<T,Integer> entry:slotMap.entrySet()){
//            slotMap.put(entry.getKey(),slot - entry.getValue()) ;
//        }
//        return L ;
//    }

    private void clone_var(){

        nodeMap_clone = new HashMap<T, Vector<T>>();
        for(Map.Entry<T,Vector<T>> entry:this.nodeMap.entrySet() ){
            Vector<T> temp_vector = new Vector<>(entry.getValue());
            nodeMap_clone.put(entry.getKey(),temp_vector);
        }

        indegreeMap_clone = new HashMap<T, Vector<T>>();
        for(Map.Entry<T,Vector<T>> entry:this.indegreeMap.entrySet() ){
            Vector<T> temp_vector = new Vector<>(entry.getValue());
            indegreeMap_clone.put(entry.getKey(),temp_vector);
        }

        noIndegreeNodes_clone = new HashSet<>(noIndegreeNodes);
        noOutDegreeNodes_clone = new HashSet<>(noOutDegreeNodes);
    }

    public Vector<T> LogicSort2(){
        this.MakeUsefulItem();
        this.clone_var();
        Vector<T> L = new Vector<>(); // Result

        int slot = 0;
        // no-outdegree nodes;
        Vector<T> S = new Vector<>(this.noOutDegreeNodes_clone);

        while (!S.isEmpty()){
            //sortfor S

            //------------
            L.addAll(S);

            for(T node:S){
                this.slotMap.put(node,slot);

                if(slotMap_verse.get(slot)==null){
                    HashSet<T> tmp = new HashSet<>();
                    tmp.add(node);
                    slotMap_verse.put(slot,tmp);
                }else {
                    slotMap_verse.get(slot).add(node);
                }

                this.removeNode(node);

            }
            slot++;
            S.clear();
            S.addAll(this.noOutDegreeNodes_clone);
        }

        return L;
    }

    private void removeNode(T node){
        if(!this.haveNode(node)){
            return;
        }
        // 找其上司
        for(T upnode: this.indegreeMap_clone.get(node)){
            this.nodeMap_clone.get(upnode).remove(node);
            if(this.nodeMap_clone.get(upnode).isEmpty()){
                this.noOutDegreeNodes_clone.add(upnode);
            }
        }

        // 赵其下属
        for(T downnode: this.nodeMap_clone.get(node)){
            this.indegreeMap_clone.get(downnode).remove(node);
            if(this.indegreeMap_clone.get(downnode).isEmpty()){
                this.noIndegreeNodes_clone.add(downnode);
            }
        }

        this.noOutDegreeNodes_clone.remove(node);
        this.noIndegreeNodes_clone.remove(node);
        this.nodeMap_clone.remove(node);
        this.indegreeMap_clone.remove(node);
    }

    public Map<T,Integer> GetSlotMap(){
        return this.slotMap;
    }
    public Map<Integer,HashSet<T>> GetSlotMap_verse(){
        return this.slotMap_verse;
    }

    public Map<T,Vector<T>> GetNodeMap(){
        return this.nodeMap;
    }

    public HashSet<T> GetNoIndegreeNodes(){
        return this.noIndegreeNodes;
    }

    public HashSet<T> GetNoOutdegreeNodes(){
        return this.noOutDegreeNodes;
    }

    /*
    计算slot中，并行区块的数量
     */
    public String GetParallelSlotNum(){
        int slot_num = 0;
        int block_total_num = 0;
        for(Map.Entry<Integer,HashSet<T>>entry:this.slotMap_verse.entrySet()){
            slot_num += 1;
            block_total_num += entry.getValue().size();
        }
        return slot_num+":"+block_total_num;
    }

    /*
    针对每一条边，计算的边延迟
     */
    public String GetEdgeNum(){
        int edge_latency_num = 0;
        int edge_total_num = 0;

        for(Map.Entry<T,Vector<T>> entry:this.nodeMap.entrySet()){
            for(T node:entry.getValue()){
                //if(slotMap.get(entry.getKey())-slotMap.get(node)>1){
                edge_latency_num += (slotMap.get(entry.getKey())-slotMap.get(node));
                //}
                edge_total_num += 1;
            }
        }

        return edge_latency_num+":"+edge_total_num;
    }

    /*
    计算区块延迟，依赖边延迟
     */
    public String GetNodeLatency(){
        int node_latency_num = 0;
        int node_total_num = 0;

        for(Map.Entry<T,Vector<T>> entry:this.nodeMap.entrySet()){
            T upnode = entry.getKey();
            int latency = 1;
            for(T downnode:entry.getValue()){
                if(slotMap.get(upnode)-slotMap.get(downnode)>latency){
                    latency = slotMap.get(upnode)-slotMap.get(downnode);
                }
            }

            node_latency_num += latency;

            node_total_num += 1;
        }

        return node_latency_num+":"+node_total_num;
    }

    /*
    获取总区块数
     */
    public int GetBlockNumber(){
        return this.nodeMap.size();
    }
}
