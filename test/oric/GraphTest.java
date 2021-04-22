package oric;

import oric.Graph;
import org.junit.Test;

import java.util.Vector;


public class GraphTest {

    @Test
    public void test1(){
        Graph graph = new Graph();
        String a = "a";
        String b = "b";
        String c = "c";
        String d = "d";
        String e = "e";
        String f = "f";
        graph.AddNode(a);
        graph.AddNode(b);
        graph.AddNode(c);
        graph.AddNode(d);
        graph.AddNode(e);
        graph.AddNode(f);
        graph.AddEdge(a,b);
        graph.AddEdge(b,c);
        graph.AddEdge(c,d);
        graph.AddEdge(a,e);
        graph.AddEdge(e,f);
        graph.AddEdge(d,f);

        graph.SetRoot(a);
        //graph.RemoveNode(f);
        //graph.RemoveNode(c);
        //graph.LogicSort2();

        System.out.println(graph.GetNodeMap());
        System.out.println(graph.GetNoIndegreeNodes());
        System.out.println(graph.GetNoOutdegreeNodes());

        Vector L = graph.LogicSort2();
        System.out.println(L);
        System.out.println(graph.GetSlotMap());

        Vector L2 = graph.LogicSort2();
        System.out.println(L2);
        System.out.println(graph.GetSlotMap());
        System.out.println(graph.GetSlotMap_verse());
    }
}
