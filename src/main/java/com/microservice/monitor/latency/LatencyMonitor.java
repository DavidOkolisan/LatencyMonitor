package com.microservice.monitor.latency;


import com.microservice.monitor.latency.util.Node;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class LatencyMonitor {
    final static Logger logger = Logger.getLogger(LatencyMonitor.class);

    public static void main(String args[]) throws IOException {
        GraphWeighted graphWeighted = new GraphWeighted();
        Map<String, Node> nodes = loadData(args, graphWeighted);
        printResults(graphWeighted, nodes);
    }

    /**
     * Method that prints results as required in task,
     * results will be printed in logs as well as result file
     * @param graphWeighted
     * @param nodes
     */
    private static void printResults(GraphWeighted graphWeighted, Map<String, Node> nodes) throws IOException {
        // The average latency for A-B-C.
        int ABC = graphWeighted.getPathWeight("ABC");

        // The average latency for A-D.
        int AD = graphWeighted.getPathWeight("AD");

        // The average latency for A-D-C.
        int ADC = graphWeighted.getPathWeight("ADC");

        // The average latency for A-E-B-C-D.
        int AEBCD = graphWeighted.getPathWeight("AEBCD");

        // The average latency for  A-E-D.
        int AED = graphWeighted.getPathWeight("AED");

        // The number of traces between C-C with maximum 3 hoops
        int traceCCByMaxHoops = graphWeighted.getNumberOfTracesBySelection(nodes.get(NodeEnum.C.name()),Selection.MAX_HOOPS,3);

        // The number of traces between A-C with exact 3 hoops
        int traceAC = graphWeighted
                .getNumberOfTracesBySelection(nodes.get(NodeEnum.A.name()), nodes.get(NodeEnum.C.name()),Selection.EXACT_HOOPS,4);

        // The latency of shortest trace between A-C
        int latencyShortestAC = graphWeighted.getShortestPathLatency(nodes.get(NodeEnum.A.name()), nodes.get(NodeEnum.C.name()));

        // The latency of shortest trace between B-B
        int latencyShortestBB = graphWeighted.getShortestPathLatency(nodes.get(NodeEnum.B.name()));

        // The number of traces between C-C with maximum latency less than 30
        int traceCCByLatency = graphWeighted.getNumberOfTracesBySelection(nodes.get(NodeEnum.C.name()), Selection.MAX_LATENCY, 30);

        logger.info("Write results to file.....");
        String file = "./tmp/results.txt";
        if(new File(file).delete()) {
            logger.info("Delete previous results file");
        }
        FileWriter fileWriter = new FileWriter("./tmp/results.txt" );
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("1. " + (ABC != 0.0 ? ABC : "NO SUCH TRACE"));
        printWriter.println("2. " + (AD != 0.0 ? AD : "NO SUCH TRACE"));
        printWriter.println("3. " + (ADC != 0.0 ? ADC : "NO SUCH TRACE"));
        printWriter.println("4. " + (AEBCD != 0.0 ? AEBCD : "NO SUCH TRACE"));
        printWriter.println("5. " + (AED != 0.0 ? AED : "NO SUCH TRACE"));
        printWriter.println("6. " + traceCCByMaxHoops);
        printWriter.println("7. " + traceAC);
        printWriter.println("8. " + latencyShortestAC);
        printWriter.println("9. " + latencyShortestBB);
        printWriter.println("10. " + traceCCByLatency);

        printWriter.close();
    }

    /**
     * Method that reads input file (comma separated graph) and loads the graph
     * @param args
     * @param graphWeighted
     * @return
     */
    private static Map<String, Node> loadData(String[] args, GraphWeighted graphWeighted) {
        Map<String, Node> nodes = new HashMap<>();
        try {
            String inputFile = args[0];
            if(args.length != 1 ) {
                throw new IllegalArgumentException("Exactly one parameter required - file name !");
            }
            logger.info("File name provided: " + inputFile);
            logger.info("Try to read the file.");

            BufferedReader csvReader = new BufferedReader(new FileReader(inputFile));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                for (String s: data) {
                    validate(s);
                    String source = s.substring(0,1);
                    String edge = s.substring(1,2);
                    int weight = Integer.parseInt(s.substring(2));
                    if(nodes.get(source) == null) {
                        nodes.put(source, new Node(source));
                    }
                    if(nodes.get(edge) == null) {
                        nodes.put(edge, new Node(edge));
                    }
                    graphWeighted.addEdge(nodes.get(source), nodes.get(edge), weight);
                }
            }
            csvReader.close();
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Input file not found. Please provide file name!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    /**
     * Method to validate each member - connection of input file, in case that
     * node name is greater than one letter this method should be refactored.
     * @param s
     */
    private static void validate(String s){
        try {
            String nodes = s.substring(0,2);
            if(!nodes.chars().allMatch(Character::isLetter)) {
                throw new IOException();
            }
            Integer.parseInt(s.substring(2));
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
