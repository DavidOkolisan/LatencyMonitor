package com.microservice.monitor.latency.util;

public class Edge implements Comparable<Edge> {

    private Node source;
    private Node destination;
    private int weight;

    public Edge(Node s, Node d, int w) {
        source = s;
        destination = d;
        weight = w;
    }

    public String toString() {
        return String.format("(%s -> %s, %f)", source.getName(), destination.getName(), weight);
    }

    public int compareTo(Edge otherEdge) {
        if (this.weight > otherEdge.weight) {
            return 1;
        }
        else return -1;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
