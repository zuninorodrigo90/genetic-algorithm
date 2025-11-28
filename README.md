# Genetic Algorithm for Minimum Vertex Cover

This project implements a **Genetic Algorithm (GA)** in Java to solve the **Minimum Vertex Cover** problem on arbitrary graphs.  
The solution uses a bitstring representation, elitist selection, 2-point crossover, bit-flip mutation, and a fitness function composed of two penalty-based areas.

---

## Problem Overview

Given a graph \(G = (V, E)\), a *vertex cover* is a set of vertices such that **every edge has at least one endpoint that is included in the cover**.

The objective of the Minimum Vertex Cover problem is:

### 👉 Find the smallest possible set of vertices that covers all edges.

This is NP-hard and cannot be solved exactly in polynomial time unless P = NP, making heuristic approaches highly valuable.

---

## Genetic Algorithm Design

### **Representation**
Each individual is a bit vector of length `NUM_VERTICES`:
- `1` → vertex is included in the cover
- `0` → vertex is not selected

Example:  
`[1,0,1,0]` represents the cover `{0,2}`.

---

## Fitness Function (Two Areas)

Because GA maximizes fitness but we want to **minimize the cover**, fitness is negative:

### **Area 1: Uncovered edges penalty**
```
area1 = -PENALTY_UNCOVERED * (# of uncovered edges)
```
If both endpoints of an edge are 0 → UNRESOLVED → penalized heavily.

### Area 2 — penalization of cover size:
```
area2 = -(cover size)
```

More vertices selected → more penalty.

### **Total fitness**
```
fitness = area1 + area2
```


Interpretation:

✔ primary goal: cover all edges  
✔ secondary goal: use as few vertices as possible

---

## Genetic Operators

### Selection
Elitism:
- the best individual always survives to the next generation
- parents for crossover are chosen from the top of the population
---

### Crossover
A **two-point crossover** is used:
```
[p1 genes] 123|45678|90
[p2 genes] ABC|DEFGH|IJ
```
Result:
```
child1 = 123|DEFGH|90
child2 = ABC|45678|IJ
```


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
Fitness = 0.0
Cover size = 7
Genes (bits) = [0,1,0,1,0,1,0,1,0,1,0,1,0,1,0]
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

## How to Use Different Graphs

Place graph files in:
```
/resources
```

Set the input file inside `VertexCoverGA.java`:

```
static final String GRAPH_FILE = "resources/vc-exact_033.gr";
```
The algorithm automatically reads:

- number of vertices
- number of edges
- edge list

---

##  Notes

- Fitness function uses two penalty components
- Selection is elitist
- Crossover is 2-point
- Mutation is bit-flip with probability = 1%
- Genes are binary
- Graph input is read from .gr files
- Code supports very large graphs

---

# Genetic Algorithm for Minimum Vertex Cover - Pseudocode

FUNCTION GeneticAlgorithm():
INPUT:
- POPULATION_SIZE
- MAX_ITERATIONS
- MUTATION_PROB
- NUM_VERTICES
- edges  // list of (u, v)

----------------------------------------------------
Step 1: Generate initial population
----------------------------------------------------
population ← empty list
FOR i = 1 TO POPULATION_SIZE:
individual.genes ← random bits of size NUM_VERTICES
Add individual to population

----------------------------------------------------
Step 2: Iterate evolutionary process
----------------------------------------------------
FOR iter = 1 TO MAX_ITERATIONS:

    ------------------------------------------------
    Step 2.1: Compute fitness of each individual
    ------------------------------------------------
    FOR each individual IN population:

        coverSize ← count of genes == 1

        uncovered ← number of edges where:
            both endpoints = 0

        area1 = -PENALTY_UNCOVERED * uncovered
        area2 = -coverSize

        fitness = area1 + area2

    ------------------------------------------------
    Step 2.2: Sort by fitness descending
    ------------------------------------------------
    Sort population by fitness highest to lowest

    elite ← population[0]

    Print iteration + elite fitness + elite coverSize

    ------------------------------------------------
    Step 2.3: Track global best solution
    ------------------------------------------------
    IF elite is a valid vertex cover AND elite.coverSize < bestCoverSize:
        bestSolution ← copy(elite)
        bestCoverSize ← elite.coverSize

    ------------------------------------------------
    Step 2.4: Create new generation with elitism
    ------------------------------------------------
    newPopulation ← empty list
    Add elite to newPopulation

    ------------------------------------------------
    Step 2.5: Generate the remaining individuals
    ------------------------------------------------
    WHILE size(newPopulation) < POPULATION_SIZE:

        parent1 ← population[0]
        parent2 ← population[1]

        (child1, child2) ← TwoPointCrossover(parent1, parent2)

        Mutate(child1, MUTATION_PROB)
        Mutate(child2, MUTATION_PROB)

        Add child1 to newPopulation
        IF size(newPopulation) < POPULATION_SIZE:
            Add child2 to newPopulation

    population ← newPopulation

----------------------------------------------------
Step 3: Return result
----------------------------------------------------
```
IF bestSolution exists:
    RETURN bestSolution
ELSE:
    RETURN population[0]
```


## Explanation of Each Step (Genetic Algorithm Execution)

### 1. Initial Population
- Several individuals are created.
- Each individual is represented as a bitstring of length equal to the number of vertices.
- Bit:
    - `1` → vertex is included in the vertex cover
    - `0` → vertex is not included
- This ensures genetic diversity in the search space.

---

### 2. Fitness Evaluation
Each individual is evaluated based on two penalty components:

#### **Area 1: Uncovered edges**
- For each edge (u, v):
    - If both genes are `0`, the edge is not covered.
- Each uncovered edge adds a large negative penalty to fitness.
- This ensures solutions that fail to cover edges are heavily penalized.

#### **Area 2: Cover size**
- The number of `1` bits is counted.
- The more vertices included, the more negative the penalty.
- This pushes the algorithm toward smaller covers.

**Priority:**
1. First: cover all edges
2. Second: use as few vertices as possible

---

### 3. Sorting Individuals
- After computing fitness values:
    - The population is sorted in descending order of fitness.
- The individual with the highest fitness becomes the **elite**.
- This is the best candidate for producing offspring.

---

### 4. Tracking Best Valid Solution
- Even if the elite is not perfect, if it:
    - correctly covers all edges
    - and has a smaller cover size than previous best
- It becomes the new **bestSolution**.
- This ensures steady progress toward an optimal cover.

---

### 5. Elitism
- The elite individual is directly copied into the next generation.
- This guarantees:
    - the current best solution will never be lost
    - the evolutionary process cannot regress

---

### 6. Crossover and Mutation
- The other individuals of the next generation are created using:
    - **Two-point crossover**
    - **Bit-flip mutation** (probability 0.01)
- These operations:
    - introduce genetic diversity
    - allow exploration of new candidate solutions
    - reduce premature convergence of the algorithm

---

### 7. Final Result
- After all iterations are completed:
    - If at least one valid vertex cover was discovered during the process:
        - the algorithm returns the smallest valid cover found.
    - Otherwise:
        - it returns the individual with the highest overall fitness,
          even if not a complete valid cover.

---

