import java.util.*;

public class VertexCoverGA {

    // ====== GA PARAMETERS ======
    static final int POPULATION_SIZE = 40;
    static final int MAX_ITERATIONS = 50;
    static final double MUTATION_PROB = 0.01;

    static final String GRAPH_FILE = "resources/vc-exact_033.gr";
    static int NUM_VERTICES = 0;
    static final int PENALTY_UNCOVERED = 10000;
    static int bestCoverSize = Integer.MAX_VALUE;
    static Individual bestSolution = null;
    static List<Edge> edges = new ArrayList<>();

    // ====== EDGE CLASS ======

    /**
     * Represents an undirected edge between two vertices (u, v).
     */
    static class Edge {
        int u, v;

        /**
         * Creates an edge connecting vertex u and vertex v.
         *
         * @param u first vertex (0-based index)
         * @param v second vertex (0-based index)
         */
        Edge(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    // ====== INDIVIDUAL ======

    /**
     * Represents an individual in the genetic algorithm,
     * storing its genes (vertex selections), fitness, and cover size.
     */
    static class Individual {
        List<Integer> genes; // bits: 1 = in cover, 0 = not in cover
        double fitness;
        int coverSize;

        /**
         * Creates a new individual with the given gene sequence.
         *
         * @param g list of gene values (0 or 1)
         */
        Individual(List<Integer> g) {
            this.genes = new ArrayList<>(g);
        }

        /**
         * Produces a deep copy of the individual,
         * duplicating its genes, fitness, and cover size.
         *
         * @return independent copy of this individual
         */
        Individual copy() {
            Individual c = new Individual(new ArrayList<>(this.genes));
            c.fitness = this.fitness;
            c.coverSize = this.coverSize;
            return c;
        }
    }


    // ===================== MAIN ======================

    /**
     * Runs the vertex cover genetic algorithm:
     * loads the graph, creates the population,
     * evolves it through crossover and mutation,
     * and outputs the best resulting vertex cover.
     *
     */
    static void main(String[] args) {

        loadGraphFromFile(GRAPH_FILE);

        long start = System.currentTimeMillis();

        List<Individual> population =
                generateInitialPopulation();

        for (int iter = 1; iter <= MAX_ITERATIONS; iter++) {

            // 1) Compute fitness
            for (Individual ind : population) {
                double a1 = area1(ind);
                double a2 = area2(ind);
                ind.coverSize = countOnes(ind);
                ind.fitness = a1 + a2;
            }

            // 2) Sort by fitness DESC
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            // elite = best individual this generation
            Individual elite = population.getFirst();

            if (iter % 100 == 0 || iter == 1 || iter == MAX_ITERATIONS) { //print every 100 iterations
                System.out.println("Iteration " + iter +
                        " | fitness = " + elite.fitness +
                        " | coverSize = " + elite.coverSize +
                        " | validCover = " + isValidCover(elite));
            }


            // 3) Update the best solution if cover is valid and smaller
            if (isValidCover(elite) && elite.coverSize < bestCoverSize) {
                bestCoverSize = elite.coverSize;
                bestSolution = elite.copy();
            }

            // 4) New generation (elitism)
            List<Individual> newPop = new ArrayList<>();
            newPop.add(elite.copy());

            // Fill rest with crossover + mutation
            while (newPop.size() < POPULATION_SIZE) {
                Individual p1 = population.get(0);
                Individual p2 = population.get(1);

                Individual[] children = crossover2Point(p1, p2);

                mutate(children[0], MUTATION_PROB);
                mutate(children[1], MUTATION_PROB);

                newPop.add(children[0]);
                if (newPop.size() < POPULATION_SIZE)
                    newPop.add(children[1]);
            }

            population = newPop;
        }

        // If no valid cover was found, fallback to best by fitness
        if (bestSolution == null) {
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));
            bestSolution = population.getFirst().copy();
        }

        long end = System.currentTimeMillis();

        // ===== FINAL OUTPUT =====
        System.out.println("\n===== FINAL BEST SOLUTION =====");
        System.out.println("Fitness        = " + bestSolution.fitness);
        System.out.println("Cover size     = " + bestSolution.coverSize);
        System.out.println("Genes (bits)   = " + bestSolution.genes);
        System.out.println("Is valid cover = " + isValidCover(bestSolution));
        System.out.println("Execution time = " + (end - start) + " ms");
    }


    // ===================== GRAPH ======================
    /**
     * Loads the graph from a .gr file (PACE challenge format),
     * reading the number of vertices and all edges.
     *
     * @param filePath location of the graph file
     */
    static void loadGraphFromFile(String filePath) {
        edges.clear();

        try (Scanner sc = new Scanner(new java.io.File(filePath))) {
            while (sc.hasNext()) {
                String token = sc.next();

                if (token.equals("c")) {
                    sc.nextLine();
                } else if (token.equals("p")) {
                    // p td 200 884
                    sc.next(); // td
                    NUM_VERTICES = sc.nextInt(); // number of vertices
                    int numEdges = sc.nextInt(); // number of edges (not mandatory to store)
                } else {
                    // otherwise it's an edge
                    int u = Integer.parseInt(token) - 1;  // convert to 0-based
                    int v = sc.nextInt() - 1;
                    edges.add(new Edge(u, v));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // ===================== FITNESS AREAS ======================

    // Area 1: penalizes uncovered edges

    /**
     * Computes the penalty for uncovered edges.
     * Each edge whose both endpoints are 0 increases the penalty.
     *
     * @param ind individual to evaluate
     * @return negative score proportional to number of uncovered edges
     */
    static double area1(Individual ind) {
        int uncovered = 0;
        for (Edge e : edges) {
            int u = e.u, v = e.v;
            int gu = ind.genes.get(u);
            int gv = ind.genes.get(v);
            if (gu == 0 && gv == 0)
                uncovered++;
        }
        return -PENALTY_UNCOVERED * uncovered;
    }

    // Area 2: penalizes the size of the cover directly

    /**
     * Penalizes the size of the cover.
     * The more selected vertices (1s), the larger the penalty.
     *
     * @param ind individual to evaluate
     * @return negative value equal to the number of vertices in the cover
     */
    static double area2(Individual ind) {
        int size = countOnes(ind);
        return -size; // smaller cover: less negative fitness
    }


    // ===================== VALIDATION ======================

    /**
     * Checks if the individual is a valid vertex cover.
     * A cover is valid if every edge has at least one endpoint with gene = 1.
     *
     * @param ind individual to evaluate
     * @return true if all edges are covered, false otherwise
     */
    static boolean isValidCover(Individual ind) {
        for (Edge e : edges) {
            if (ind.genes.get(e.u) == 0 && ind.genes.get(e.v) == 0)
                return false;
        }
        return true;
    }


    // ===================== UTILITIES ======================

    /**
     * Counts how many vertices are included in the cover (genes = 1).
     *
     * @param ind individual to evaluate
     * @return number of genes with value 1
     */
    static int countOnes(Individual ind) {
        int c = 0;
        for (int g : ind.genes) if (g == 1) c++;
        return c;
    }


    // ===================== CROSSOVER ======================

    /**
     * Performs 2-point crossover between two parents, producing two children.
     * Genes between two random crossover points are swapped.
     *
     * @param p1 first parent
     * @param p2 second parent
     * @return array of two offspring individuals
     */
    static Individual[] crossover2Point(Individual p1, Individual p2) {
        Random r = new Random();

        int pA = r.nextInt(NUM_VERTICES - 1);
        int pB = pA + 1 + r.nextInt(NUM_VERTICES - pA - 1);

        Individual c1 = p1.copy();
        Individual c2 = p2.copy();

        for (int i = pA; i <= pB; i++) {
            int tmp = c1.genes.get(i);
            c1.genes.set(i, c2.genes.get(i));
            c2.genes.set(i, tmp);
        }

        return new Individual[]{c1, c2};
    }


    // ===================== MUTATION ======================

    /**
     * Applies mutation to an individual by flipping each gene with a given probability.
     *
     * @param ind  individual to mutate
     * @param prob mutation probability for each gene
     */
    static void mutate(Individual ind, double prob) {
        Random r = new Random();
        for (int i = 0; i < ind.genes.size(); i++) {
            if (r.nextDouble() < prob)
                ind.genes.set(i, 1 - ind.genes.get(i)); // flip bit
        }
    }


    // ===================== INITIAL POPULATION ======================

    /**
     * Creates the initial population with random gene assignments (0 or 1).
     *
     * @return list of randomly generated individuals
     */
    static List<Individual> generateInitialPopulation() {
        Random r = new Random();
        List<Individual> pop = new ArrayList<>();

        for (int i = 0; i < VertexCoverGA.POPULATION_SIZE; i++) {
            List<Integer> genes = new ArrayList<>();
            for (int j = 0; j < NUM_VERTICES; j++)
                genes.add(r.nextInt(2)); // random bit
            pop.add(new Individual(genes));
        }
        return pop;
    }
}
