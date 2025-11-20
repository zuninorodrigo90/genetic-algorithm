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

# Genetic Algorithm for Minimum Vertex Cover — Pseudocode (with Explanations)

---------------------------------------------------------
PSEUDOCODE
---------------------------------------------------------

FUNCTION GeneticAlgorithm():
INPUT:
- populationSize
- maxIterations
- mutationProbability
- targetCoverSize   // e.g., 7
- graphEdges        // list of (u, v) edges
- numberOfVertices

```

    // ----------------------------------------------------
    // Step 1: Generate initial population
    // ----------------------------------------------------
    population ← GenerateRandomBitstrings(populationSize, numberOfVertices)

    FOR each iteration FROM 1 TO maxIterations:

        // ------------------------------------------------
        // Step 2: Evaluate fitness of each individual
        // ------------------------------------------------
        FOR each individual IN population:
            coverSize ← CountOnes(individual)
            uncoveredEdges ← CountUncoveredEdges(individual, graphEdges)

            // AREA 1: penalize uncovered edges
            area1 ← -1000 * uncoveredEdges

            // AREA 2: penalize deviation from target cover size
            area2 ← -10 * ABS(coverSize - targetCoverSize)

            fitness(individual) ← area1 + area2

        // ----------------------------------------------
        // Step 3: Sort individuals by descending fitness
        // ----------------------------------------------
        Sort population by fitness (highest first)

        elite ← population[0]    // the best individual

        PRINT("Iteration", iteration, "fitness =", elite.fitness)

        // ------------------------------------------------
        // Step 4: Check stopping condition
        // ------------------------------------------------
        IF elite.fitness == 0:
            RETURN elite   // perfect vertex cover found

        // ------------------------------------------------
        // Step 5: Generate new population (elitism)
        // ------------------------------------------------
        newPopulation ← empty list
        Add elite to newPopulation

        // ------------------------------------------------
        // Step 6: Fill population with crossover + mutation
        // ------------------------------------------------
        WHILE size(newPopulation) < populationSize:
            parent1 ← population[0]
            parent2 ← population[1]

            // two-point crossover
            (child1, child2) ← TwoPointCrossover(parent1, parent2)

            // bit-flip mutation
            Mutate(child1, mutationProbability)
            Mutate(child2, mutationProbability)

            Add child1 to newPopulation
            IF size(newPopulation) < populationSize:
                Add child2 to newPopulation

        population ← newPopulation

    // ----------------------------------------------------
    // Step 7: If no perfect solution found, return best
    // ----------------------------------------------------
    RETURN population[0]
```


---------------------------------------------------------
EXPLANATION OF EACH STEP
---------------------------------------------------------

1. Initial Population
    - Create several individuals, each represented as a bitstring of length equal to the number of vertices.
    - Bit '1' means the vertex is included in the vertex cover.
    - Bit '0' means the vertex is not selected.
    - This provides genetic diversity for the algorithm to explore.

2. Fitness Evaluation
    - Each individual is evaluated based on two “areas”:
      AREA 1: uncovered edges (penalized with a large negative value)
      AREA 2: deviation from the target cover size (penalized with a smaller negative value)
    - A perfect solution has:
        * no uncovered edges
        * coverSize = targetCoverSize
          -> fitness = 0

3. Sorting Individuals
    - The population is sorted in descending order of fitness.
    - The best individual is chosen as the “elite”.

4. Stopping Condition
    - If an individual reaches fitness = 0, it means:
        * all edges are covered
        * the cover uses exactly the target number of vertices
    - This is an optimal vertex cover under the defined constraints.

5. Elitism
    - The elite individual is copied directly into the next generation.
    - This ensures the best solution is never lost.

6. Crossover and Mutation
    - Parents are selected (elitist selection).
    - Two-point crossover combines sections of parent bitstrings.
    - Mutation flips random bits (0→1, 1→0) with small probability.
    - This introduces diversity and prevents premature convergence.

7. Final Result
    - If no perfect solution is found after all iterations,
      the best individual seen so far is returned.
