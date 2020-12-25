package com.microservice.monitor.latency;

import com.microservice.monitor.latency.util.Edge;
import com.microservice.monitor.latency.util.Node;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class GraphWeighted {

    final static Logger logger = Logger.getLogger(GraphWeighted.class);

    private Set<Node> nodes;
    private boolean directed;
    private int temp = 0;
    private HashMap<String, Integer> paths = new HashMap<>();
    private Node startNode = null;
    private Node endNode=null;

    public GraphWeighted() {
        this.directed = true;
        nodes = new HashSet<>();
    }

    public GraphWeighted(boolean directed) {
        this.directed = directed;
        nodes = new HashSet<>();
    }

    /**
     * Method that sets new edge on the node
     * @param source
     * @param destination
     * @param weight
     */
    public void addEdge(Node source, Node destination, int weight) {
        nodes.add(source);
        nodes.add(destination);
        checkEdgeExistance(source, destination, weight);

        if (!directed && source != destination) {
            checkEdgeExistance(destination, source, weight);
        }
    }

    /**
     * Helper method that sets weight and edge
     * @param a
     * @param b
     * @param weight
     */
    private void checkEdgeExistance(Node a, Node b, int weight) {
        for (Edge edge : a.getEdges()) {
            if (edge.getSource() == a && edge.getDestination() == b) {
                edge.setWeight(weight);
                return;
            }
        }
        a.getEdges().add(new Edge(a, b, weight));
    }

    /**
     * Method that checks if source node has edge node
     * @param source
     * @param destination
     * @return
     */
    public boolean hasEdge(Node source, Node destination) {
        LinkedList<Edge> edges = source.getEdges();
        for (Edge edge : edges) {
            if (edge.getDestination() == destination) {
                return true;
            }
        }
        return false;
    }


    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * Method that returns weight between two nodes
     * @param source
     * @param target
     * @return
     */
    private int getWeight(Node source, Node target) {
        LinkedList<Edge> edges = source.getEdges();
        for (Edge edge : edges) {
            if (edge.getDestination() == target) {
                return edge.getWeight();
            }
        }
        return 0;
    }

    /**
     * Method that returns node by node name
     * @param name
     * @return
     */
    private Node getNode(String name) {
        for(Iterator<Node> i = nodes.iterator(); i.hasNext();) {
            Node n = i.next();
             if(n.getName().equalsIgnoreCase(name)){
                return n;
             };
        }
        return null;
    }

    /**
     * Method that returns weight of some path
     * i.e. If there is path A-B and B-C, method for input parameter will return total weight
     * AB + BC
     * @param path
     * @return
     */
    public int getPathWeight(String path) {
        List<Character> list = path.chars().mapToObj(l -> (char) l).collect(Collectors.toList());
        if(list.size()==2) {
            // If we have only two nodes on our path - return existing edge weight
            return getWeight(getNode(list.get(0).toString()), getNode(list.get(1).toString()));
        } else {
            // If we have more than two nodes on our path
            int w = 0;
            for(int i=0; i<list.size()-1; i++) {
                int localWeight = getWeight(getNode(list.get(i).toString()), getNode(list.get(i + 1).toString()));
                // Handle case of non existing connection
                if(localWeight == 0) {
                    logger.error("NO SUCH TRACE: " + list.get(i).toString() + "-" + list.get(i + 1).toString());
                    return 0;
                }
                w = w + getWeight(getNode(list.get(i).toString()), getNode(list.get(i + 1).toString()));
            }
            return w;
        }

    }

    public int getNumberOfTracesBySelection(Node a, Node b, Selection selection, int i){
        if(a.equals(b)) {
            return getNumberOfTracesBySelection(a,selection,i);
        }
        return getAllPaths(a,b,selection,i).size();
    }

    public int getNumberOfTracesBySelection(Node a, Selection selection, int i){
        return getAllPaths(a,selection,i).size();
    }

    /**
     * Method that returns paths for same node excluding to itself connection,
     * so all possible paths from ie A-A with some nodes/edges in midst.
     * Currently this method returns paths for max number of hoops betweend nodes
     * or for max latency between same node.
     * @param a
     * @param selection
     * @param i
     * @return
     */
    private HashMap<String, Integer> getAllPaths(Node a, Selection selection, int i) {
        if(isEdge(a)) {
            return getAllPaths(a,a,selection,i);
        }
        logger.error("NO SUCH TRACE: " + a.getName() + "-" + a.getName());
        return new HashMap<>();
    }

    /**
     * Method used for obtaining path from different nodes, so start and stop node cant be the same node
     * use getAllPaths(Node a, Selection selection, int i) for that scenario instead.
     * Currently this method returns paths only for exact number of hoops between two different nodes.
     * @param start
     * @param end
     * @param selection
     * @param value
     * @return
     */
    private HashMap<String, Integer> getAllPaths(Node start, Node end, Selection selection, int value) {
        //cleanup paths
        paths.clear();
        temp=0;

        //reset start and end node
        startNode = start;
        endNode = end;

        switch (selection) {
            case MAX_LATENCY:
                getPathsForSameNodeAndMaxLatency(end, value, end.getName());
                break;
            case MAX_HOOPS:
                getPathsForSameNodeAndMaxNumberOfSteps(end, value, start.getName());
                break;
            case EXACT_HOOPS:
                getPathsWithExactNumberOfSteps(start, end, value, start.getName());
                break;
        }

        //if no paths found throw message
        if(paths.size()==0){
            System.out.println("NO SUCH TRACE: " + start.getName() + "-" + end.getName());
        }

        return paths;
    }

    /**
     * Method that returns all paths with weights, for same source and target nodes,
     * with maximum number of connections lower than maxLatency excluding
     * direct connection to itself.
     * @param target
     * @param maxNrHoops
     * @param prefix
     */
    private void getPathsForSameNodeAndMaxNumberOfSteps(Node target, int maxNrHoops, String prefix) {
        LinkedList<Edge> edges = target.getEdges();

        for (Edge e : edges) {
            String newPrefix = prefix + e.getDestination().getName();
            if(!e.getDestination().getName().equalsIgnoreCase(startNode.getName()) && prefix.length()<maxNrHoops){
                getPathsForSameNodeAndMaxNumberOfSteps(e.getDestination(),maxNrHoops,newPrefix);
            } else if(e.getDestination().getName().equalsIgnoreCase(startNode.getName())){
                temp=getPathWeight(newPrefix);
                paths.put(newPrefix, temp);
                if(newPrefix.length()<maxNrHoops) {
                    getPathsForSameNodeAndMaxNumberOfSteps(e.getDestination(),maxNrHoops,newPrefix);
                }
            }
        }

    }

    /**
     * Method that returns all paths with weights, for same source and target nodes, with latency lower
     * than maxLatency excluding direct connection to itself.
     *
     * @param start
     * @param target
     * @param exactNrHoops
     * @param prefix
     */
    private void getPathsWithExactNumberOfSteps(Node start, Node target, int exactNrHoops, String prefix) {

        LinkedList<Edge> edges = start.getEdges();

        for (Edge e : edges) {
            String newPrefix = prefix + e.getDestination().getName();

            if(!e.getDestination().getName().equalsIgnoreCase(endNode.getName()) && newPrefix.length()<=exactNrHoops) {
                getPathsWithExactNumberOfSteps(e.getDestination(), target, exactNrHoops, newPrefix);
            }
            else if(e.getDestination().getName().equalsIgnoreCase(endNode.getName())){
                if(newPrefix.length()-1==exactNrHoops)  {
                    temp = getPathWeight(newPrefix);
                    paths.put(newPrefix, temp);
                }
                getPathsWithExactNumberOfSteps(e.getDestination(), target, exactNrHoops, newPrefix);
            }

        }
    }

    /**
     * Method that returns all paths with weights, for same source and target nodes, with latency lower
     * than maxLatency excluding direct connection to itself.
     *
     * @param target
     * @param maxLatency
     * @param prefix
     */
    private void getPathsForSameNodeAndMaxLatency(Node target, int maxLatency, String prefix) {
        LinkedList<Edge> edges = target.getEdges();
        for (Edge e : edges) {
            String newPrefix = prefix + e.getDestination().getName();
            temp = getPathWeight(newPrefix);
            if(temp<maxLatency) {
                if (e.getDestination().getName().equalsIgnoreCase(endNode.getName())) {
                    paths.put(newPrefix, temp);
                }
                getPathsForSameNodeAndMaxLatency(e.getDestination(),maxLatency,newPrefix);
            }
        }
    }

    /**
     * Method that returns shorthest path between two different nodes with weight
     * @param start
     * @param end
     * @return
     */
    private HashMap<String, Integer> getShortestPathBetweenDifferentNodes(Node start, Node end) {
        HashMap<Node, Node> parentChildMap = new HashMap<>();
        parentChildMap.put(start, null);

        // Shortest path between nodes
        HashMap<Node, Integer> shortestPathMap = new HashMap<>();

        for (Node node : getNodes()) {
            if (node == start)
                shortestPathMap.put(start, 0);
            else shortestPathMap.put(node, Integer.MAX_VALUE);
        }

        for (Edge edge : start.getEdges()) {
            shortestPathMap.put(edge.getDestination(), edge.getWeight());
            parentChildMap.put(edge.getDestination(), start);
        }

        while (true) {
            Node currentNode = closestUnvisitedNeighbour(shortestPathMap);

            if (currentNode == null) {
                return null;
            }

            // Save path to nearest unvisited node
            if (currentNode == end) {
                Node child = end;
                String path = end.getName();
                while (true) {
                    Node parent = parentChildMap.get(child);
                    if (parent == null) {
                        break;
                    }

                    // Create path using previous(parent) and current(child) node
                    path = parent.getName() + "" + path;
                    child = parent;
                }
                HashMap<String,Integer> hm= new HashMap<>();
                hm.put(path, shortestPathMap.get(end));
                return hm;
            }
            currentNode.visit();

            // Go trough edges and find nearest
            for (Edge edge : currentNode.getEdges()) {
                if (edge.getDestination().isVisited())
                    continue;

                if (shortestPathMap.get(currentNode) + edge.getWeight() < shortestPathMap.get(edge.getDestination())) {
                    shortestPathMap.put(edge.getDestination(), shortestPathMap.get(currentNode) + edge.getWeight());
                    parentChildMap.put(edge.getDestination(), currentNode);
                }
            }
        }
    }

    /**
     * Method that returns nearest edge node
     * @param shortestPathMap
     * @return
     */
    private Node closestUnvisitedNeighbour(HashMap <Node, Integer > shortestPathMap){
        int shortestDistance = Integer.MAX_VALUE;
        Node closestReachableNode = null;
        for (Node node : getNodes()) {
            if (node.isVisited())
                continue;

            int currentDistance = shortestPathMap.get(node);
            if (currentDistance == Integer.MAX_VALUE)
                continue;

            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance;
                closestReachableNode = node;
            }
        }
        return closestReachableNode;
    }

    /**
     * Method that returns shortest path for same node. For simplicity sake previously
     * written methods are used
     * @param node
     * @param latency
     * @return
     */
    private HashMap<String, Integer> getShortestPathBetweenSameNodes(Node node, int latency) {
        HashMap<String,Integer> allPaths = getAllPaths(node,Selection.MAX_LATENCY,latency);
        if(allPaths.isEmpty() && isEdge(node)) {
            allPaths = getShortestPathBetweenSameNodes(node,latency * 2);
        }

        String key = Collections.min(allPaths.entrySet(), Map.Entry.comparingByValue()).getKey();
        int value = Collections.min(allPaths.entrySet(), Map.Entry.comparingByValue()).getValue();
        allPaths.clear();
        allPaths.put(key,value);

        return allPaths;
    }

    /**
     * Method that returns shortest path latency for same node
     * @param node
     * @return
     */
    public int getShortestPathLatency(Node node) {
        HashMap<String, Integer> pathWeight = getShortestPath(node);
        if(pathWeight!=null && pathWeight.size()==1) {
            return pathWeight.entrySet().iterator().next().getValue();
        }
        return 0;
    }

    /**
     * Method that returns shortest path latency for different nodes
     * @param a
     * @param b
     * @return
     */
    public int getShortestPathLatency(Node a, Node b) {
        HashMap<String,Integer> pathWeight = getShortestPath(a,b);

        if(pathWeight!=null && pathWeight.size()==1) {
            return pathWeight.entrySet().iterator().next().getValue();
        }
        return 0;
    }

    /**
     * Mathod that returns shortest path for same nodes
     * @param node
     * @return
     */
    private HashMap<String, Integer> getShortestPath(Node node){
        if(isEdge(node)) {
            return getShortestPathBetweenSameNodes(node, 30);
        }
        logger.info("NO SUCH TRACE: " + node.getName() + "-" + node.getName());
        return null;
    }

    /**
     * Method that returns shortest path beteween two nodes
     * @param start
     * @param end
     * @return
     */
    private HashMap<String, Integer> getShortestPath(Node start, Node end){
        if(start.equals(end)) {
            return getShortestPath(start);
        }
        return getShortestPathBetweenDifferentNodes(start,end);
    }

    /**
     * Method that checks if there is at least one relation
     * from any other node to input node
     *
     * @param node
     * @return
     */
    private boolean isEdge(Node node) {
        for (Node n: nodes){
            if(!node.equals(n)) {
                for(Edge e:n.getEdges()){
                    if(e.getDestination().equals(node)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
