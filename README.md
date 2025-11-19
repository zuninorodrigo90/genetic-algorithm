# Genetic Algorithm for Minimum Vertex Cover

This project implements a **Genetic Algorithm (GA)** in Java to solve the **Minimum Vertex Cover** problem on arbitrary graphs.  
The solution uses a bitstring representation, elitist selection, 2‑point crossover, bit‑flip mutation, and a fitness function composed of **two penalty-based areas**.

---

## Problem Overview

Given a graph \(G = (V, E)\), a *vertex cover* is a set of vertices such that **every edge is incident to at least one selected vertex**.

The objective of the *Minimum Vertex Cover* problem is:

### **Find the smallest possible set of vertices covering all edges.**

This is an NP-hard optimization problem, which makes it ideal for metaheuristics such as Genetic Algorithms.

---

## Genetic Algorithm Design

### **Representation**
Each individual is a list of bits (0/1) of length equal to the number of vertices:
- `1` → vertex is included in the cover
- `0` → vertex is not selected

Example:  
`[1,0,1,0]` represents the cover `{0,2}`.

---

## Fitness Function (Two Areas)

Since the GA *maximizes* fitness but the VC problem is *minimization*, the fitness is implemented using **negative penalties**:

### **Area 1: Uncovered edges penalty**
```
area1 = -1000 * (# of uncovered edges)
```

This forces the GA to prioritize valid covers.

### **Area 2: Deviation from target size**
```
area2 = -10 * abs(coverSize - TARGET_COVER_SIZE)
```

This guides the GA to find solutions with exactly 7 nodes (or other target values depending on the graph).

### **Total fitness**
```
fitness = area1 + area2
```

The ideal fitness is **0**, which means:
- all edges are covered
- coverSize == TARGET_COVER_SIZE

Any negative value indicates a suboptimal solution.

---

## Genetic Operators

### **Selection**
Elitist selection:
- The best individual always survives
- Parents chosen from top of population

### **Crossover**
A **2‑point crossover** swaps segments between two parents.

### **Mutation**
Simple **bit‑flip** mutation:
```
0 → 1
1 → 0
```
with probability `0.01`.

---

## Example: 5×3 Grid Graph (15 nodes)

The graph:

```
0 — 1 — 2
|   |    |
3 — 4 — 5
|   |    |
6 — 7 — 8
|   |    |
9 — 10 — 11
|   |    |
12 — 13 — 14
```

The minimum vertex cover is known to be **7 nodes**.

---

## Execution Output Example

```
Iteration 6 | fitness = 0.0 | coverSize = 7
Perfect Vertex Cover (size 7) found at iteration 6

===== FINAL BEST SOLUTION =====
Fitness        = 0.0
Cover size     = 7
Genes (bits)   = [0,1,0,1,0,1,0,1,0,1,0,1,0,1,0]
Is valid cover = true
Execution time = 12 ms
```

---

---

## How to Run

Compile:

```
javac VertexCoverGA.java
```

Run:

```
java VertexCoverGA
```

---

##  Notes

- You can swap the graph in `initGraph()` to test other structures.
- Adjust `TARGET_COVER_SIZE` according to the known optimum.
- The penalty values can be tuned depending on the difficulty.

---