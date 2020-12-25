package com.microservice.monitor.latency.util;

import java.util.LinkedList;

public class Node {
    private String name;
    private boolean visited;
    private LinkedList<Edge> edges;

    public Node(String name) {
        this.name = name;
        visited = false;
        edges = new LinkedList<>();
    }

    public boolean isVisited() {
        return visited;
    }

    public void visit() {
        visited = true;
    }

    public void unvisit() {
        visited = false;
    }

    public LinkedList<Edge> getEdges() {
        return edges;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Node n) {
        if(n.getName().equalsIgnoreCase(this.name)){
            return true;
        }
        return false;
    }
}
