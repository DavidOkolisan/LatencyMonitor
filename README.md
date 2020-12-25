# Latency monitor


Latency monitor calculate various paremeters based on directional graph structure place in input file (input.txt). Input file is comma separeted file,
where each element represent two services and weights between them, in terms of latency (i.e. AB5 represents latency between A and B,
but not B and A since we use directional graph).


In current setup app calculates:
1. The average latency of the trace A-B-C.
2. The average latency of the trace A-D.
3. The average latency of the trace A-D-C.
4. The average latency of the trace A-E-B-C-D.
5. The average latency of the trace A-E-D.
6. The number of traces originating in service C and ending in service C with a maximum of 3 hops. In the sample data below there are two such traces: C-D-C (2 stops) and C-E-B-C (3 stops).
7. The number of traces originating in A and ending in C with exactly 4 hops. In the sample data below there are three such traces: A to C (via B, C, D); A to C (via D, C, D); and A to C (via D, E, B).
8. The length of the shortest trace (in terms of latency) between A and C.
9. The length of the shortest trace (in terms of latency) between B and B.
10. The number of different traces from C to C with an average latency of less than 30. In the same data, the traces are C-D-C, C-E-B-C, C-E-B-C-D-C, C-D-C-E-B-C, C-D-E-B-C, C-E-B-C-E-B-C, C-E-B-C-E-B-C-E-B-C.


Input.txt
AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7

