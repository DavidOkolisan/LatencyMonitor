package com.microservice.monitor.latency;

import com.microservice.monitor.latency.util.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphWeightedTest {
    private GraphWeighted gw;
    private Node A;
    private Node B;
    private Node C;
    private Node D;
    private Node E;

    @BeforeEach
    public void setUp(){
        gw = new GraphWeighted();

        A = new Node("A");
        B = new Node("B");
        C = new Node("C");
        D = new Node("D");
        E = new Node("E");
    }


    @Test
    void testAddEdge() {
        gw.addEdge(A, B, 5);
        assertEquals(2, gw.getNodes().size());

    }

    @Test
    void testHasEdge() {
        gw.addEdge(A, B, 5);
        assertEquals(true, gw.hasEdge(A,B));
    }

    @Test
    void testGetPathWeight() {
        setupAllEdges();

        assertEquals(9, gw.getPathWeight("ABC"));
        assertEquals(5, gw.getPathWeight("AD"));
        assertEquals(13, gw.getPathWeight("ADC"));
        assertEquals(22, gw.getPathWeight("AEBCD"));
        assertEquals(0, gw.getPathWeight("AED"));
        assertEquals(0, gw.getPathWeight("ADA"));

    }

    @Test
    void testGetNumberOfTracesByMaxHoops() {
        setupAllEdges();

        // Get number of traces by maximum number of hoops
        // is currently supported only for same source/target node.
        assertEquals(2,gw.getNumberOfTracesBySelection(C,C,Selection.MAX_HOOPS,3));
        assertEquals(6,gw.getNumberOfTracesBySelection(C,Selection.MAX_HOOPS,5));
        assertEquals(0,gw.getNumberOfTracesBySelection(A,Selection.MAX_HOOPS,3));
    }

    @Test
    void testGetNumberOfTracesByExactHoops() {
        setupAllEdges();

        assertEquals(3,gw.getNumberOfTracesBySelection(A,C,Selection.EXACT_HOOPS,4));
        assertEquals(0,gw.getNumberOfTracesBySelection(C,A,Selection.EXACT_HOOPS,4));
        assertEquals(2,gw.getNumberOfTracesBySelection(C,C,Selection.EXACT_HOOPS,4));
        assertEquals(0,gw.getNumberOfTracesBySelection(A,A,Selection.EXACT_HOOPS,4));
    }

    @Test
    void testGetNumberOfTracesByMaxLatency() {
        setupAllEdges();

        // Retrieving number of paths is current supported only for same source - target node
        assertEquals(7,gw.getNumberOfTracesBySelection(C,C,Selection.MAX_LATENCY,30));
        assertEquals(0,gw.getNumberOfTracesBySelection(A,A,Selection.MAX_LATENCY,30));
    }

    @Test
    void testGetShortestPath() {
        setupAllEdges();

        assertEquals(9,  gw.getShortestPathLatency(A,C));
        assertEquals(9,  gw.getShortestPathLatency(B));
        assertEquals(9,  gw.getShortestPathLatency(B,B));
        assertEquals(0,  gw.getShortestPathLatency(A));
        assertEquals(0,  gw.getShortestPathLatency(B,A));
    }

    private void setupAllEdges() {
        gw.addEdge(A, B, 5);
        gw.addEdge(A, D, 5);
        gw.addEdge(A, E, 7);
        gw.addEdge(B, C, 4);
        gw.addEdge(C, D, 8);
        gw.addEdge(C, E, 2);
        gw.addEdge(D, C, 8);
        gw.addEdge(D, E, 6);
        gw.addEdge(E, B, 3);
    }


}