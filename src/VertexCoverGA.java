import java.util.*;

public class VertexCoverGA {

    // ====== GA PARAMETERS ======
    static final int NUM_VERTICES = 15;
    static final int POPULATION_SIZE = 40;
    static final int MAX_ITERATIONS = 50;
    static final double MUTATION_PROB = 0.01;
    static final int TARGET_COVER_SIZE = 7;   // optimum = 7
    static final int PENALTY_UNCOVERED = 1000;
    static final int PENALTY_DEVIATION = 10;

    static Individual bestSolution = null;
    static List<Edge> edges = new ArrayList<>();

    // ====== EDGE CLASS ======
    static class Edge {
        int u, v;
        Edge(int u, int v) { this.u = u; this.v = v; }
    }

    // ====== INDIVIDUAL ======
    static class Individual {
        List<Integer> genes; // bits: 1 = in cover, 0 = not in cover
        double fitness;
        int coverSize;

        Individual(List<Integer> g) {
            this.genes = new ArrayList<>(g);
        }

        Individual copy() {
            Individual c = new Individual(new ArrayList<>(this.genes));
            c.fitness = this.fitness;
            c.coverSize = this.coverSize;
            return c;
        }
    }


    // ===================== MAIN ======================
    public static void main(String[] args) {

        initGraph();  // build 5×3 grid graph

        long start = System.currentTimeMillis();

        List<Individual> population =
                generateInitialPopulation(POPULATION_SIZE);

        for (int iter = 1; iter <= MAX_ITERATIONS; iter++) {

            // 1) Compute fitness
            for (Individual ind : population) {
                double a1 = area1(ind);
                double a2 = area2(ind);
                ind.coverSize = countOnes(ind);
                ind.fitness = a1 + a2;
            }

            // 2) Sort by fitness DESC
            population.sort((a,b) -> Double.compare(b.fitness, a.fitness));

            // elite = best individual this generation
            Individual elite = population.getFirst();

            System.out.println("Iteration " + iter +
                    " | fitness = " + elite.fitness +
                    " | coverSize = " + elite.coverSize);

            // 3) STOP when fitness == 0 (perfect vertex cover of size 7)
            if (elite.fitness == 0) {
                bestSolution = elite.copy();
                System.out.println("Perfect Vertex Cover (size 7) found at iteration " + iter);
                break;
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

        // If optimum never reached, pick the best at the end
        if (bestSolution == null) {
            population.sort((a,b)->Double.compare(b.fitness,a.fitness));
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
    static void initGraph() {
        edges.clear();

        // Horizontal edges (5 rows × 2)
        edges.add(new Edge(0,1));  edges.add(new Edge(1,2));
        edges.add(new Edge(3,4));  edges.add(new Edge(4,5));
        edges.add(new Edge(6,7));  edges.add(new Edge(7,8));
        edges.add(new Edge(9,10)); edges.add(new Edge(10,11));
        edges.add(new Edge(12,13)); edges.add(new Edge(13,14));

        // Vertical edges (3 columns × 4)
        edges.add(new Edge(0,3));   edges.add(new Edge(3,6));
        edges.add(new Edge(6,9));   edges.add(new Edge(9,12));

        edges.add(new Edge(1,4));   edges.add(new Edge(4,7));
        edges.add(new Edge(7,10));  edges.add(new Edge(10,13));

        edges.add(new Edge(2,5));   edges.add(new Edge(5,8));
        edges.add(new Edge(8,11));  edges.add(new Edge(11,14));
    }


    // ===================== FITNESS AREAS ======================

    // Area 1: penalizes uncovered edges
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

    // Area 2: penalizes cover size different from 7
    static double area2(Individual ind) {
        int size = countOnes(ind);
        return -PENALTY_DEVIATION * Math.abs(size - TARGET_COVER_SIZE);
    }


    // ===================== VALIDATION ======================
    static boolean isValidCover(Individual ind) {
        for (Edge e : edges) {
            if (ind.genes.get(e.u) == 0 && ind.genes.get(e.v) == 0)
                return false;
        }
        return true;
    }


    // ===================== UTILITIES ======================
    static int countOnes(Individual ind) {
        int c = 0;
        for (int g : ind.genes) if (g == 1) c++;
        return c;
    }


    // ===================== CROSSOVER ======================
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
    static void mutate(Individual ind, double prob) {
        Random r = new Random();
        for (int i = 0; i < ind.genes.size(); i++) {
            if (r.nextDouble() < prob)
                ind.genes.set(i, 1 - ind.genes.get(i)); // flip bit
        }
    }


    // ===================== INITIAL POPULATION ======================
    static List<Individual> generateInitialPopulation(int size) {
        Random r = new Random();
        List<Individual> pop = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            List<Integer> genes = new ArrayList<>();
            for (int j = 0; j < NUM_VERTICES; j++)
                genes.add(r.nextInt(2)); // random bit
            pop.add(new Individual(genes));
        }
        return pop;
    }
}
