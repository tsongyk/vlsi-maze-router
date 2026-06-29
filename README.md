# Grid Maze Router

A grid-based **maze router** that connects pairs of points on a rectangular grid with
non-crossing, obstacle-avoiding paths while minimizing total path length. Given a grid
with obstacle regions and a set of endpoint pairs, the router links every pair without
overlapping any other path.

This is the same class of algorithm used in the routing stage of VLSI and PCB physical
design: connect each connection through free tracks, never overlap two routes, never run
through a blocked region, and keep the aggregate length small.

## Algorithm

The router combines three classic ideas from physical-design routing:

1. **Lee maze routing (BFS).** Each net is routed with a breadth-first search over the
   4-connected grid, which yields a shortest obstacle- and net-avoiding path for that
   net (optimal for an individual net on an unweighted grid).

2. **Shortest-first net ordering.** Nets are routed in increasing order of
   source-to-target distance. Shorter nets have fewer feasible routes, so committing
   them first reduces the chance that a constrained net is blocked by a longer, more
   flexible one.

3. **Rip-up and reroute.** When a net cannot be routed because earlier nets are in the
   way, the router rips up the conflicting neighboring nets, routes the blocked net,
   then re-routes the ripped-up nets — recursing as needed. This is the standard
   conflict-resolution technique used by production routers to escape greedy ordering
   failures.

Routing a net marks every grid cell it occupies with the net's ID, which both records
the path and prevents later nets from overlapping it.

## Input format

```
<height>                 # number of rows
<width>                  # number of columns
<num_obstacles>
<r1> <c1> <r2> <c2>      # one line per obstacle: upper-left and lower-right corners
...
<num_nets>
<sr> <sc> <dr> <dc>      # one line per net: source (row,col) and destination (row,col)
...
```

Obstacles are filled rectangular regions marked `-1` on the grid. A net may have the
same source and destination. See [`test/inputs`](test/inputs) for examples.

## Build and run

Requires a JDK (developed against OpenJDK). JUnit jars are vendored in [`lib`](lib).

```bash
# Compile sources and tests
javac -cp "lib/junit-platform-console-standalone-1.8.1.jar" -d build src/*.java test/*.java

# Run the test suite
java -jar lib/junit-platform-console-standalone-1.8.1.jar -cp build --scan-classpath
```

## Layout

```
src/
  Board.java       # grid model: obstacles, net placement, adjacency, validation
  Coord.java       # (row, column) grid coordinate
  Endpoints.java   # a net: id + source/destination coordinates
  Wire.java        # a routed path (ordered list of coordinates)
  Routing.java     # the router: BFS, net ordering, rip-up-and-reroute
test/
  RoutingTest.java # JUnit tests
  Utilities.java   # input parsing and path-correctness checks
  inputs/          # sample grids
```

## Complexity

- **Per-net BFS:** `O(V)` over grid cells (`V = height * width`), 4 edges per cell.
- **Net ordering:** `O(n log n)` for `n` nets.
- **Rip-up and reroute** is a heuristic search; its cost depends on how much
  re-routing a grid forces.

## Limitations

- Routing all nets with globally minimum total wirelength is NP-hard, so the
  shortest-first + rip-up-and-reroute strategy is a heuristic: it minimizes each net's
  length individually but does not guarantee a globally optimal layout.
- The rip-up-and-reroute search is a best-effort heuristic: it is not guaranteed to
  route every satisfiable instance. It terminates cleanly in all cases — when a net
  cannot be placed, the ripped-up wires are restored and the net is reported as
  unrouted rather than recursing indefinitely.
