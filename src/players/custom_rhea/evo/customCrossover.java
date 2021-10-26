package players.custom_rhea.evo;

import players.custom_rhea.utils.customRHEAParams;
import utils.Utils;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static players.rhea.utils.Constants.*;

class customCrossover {
    private customRHEAParams params;
    private Random random;

    private int chosenIdx1, chosenIdx2;

    customCrossover(customRHEAParams params, Random random) {
        this.params = params;
        this.random = random;
    }

    customIndividual cross(customIndividual parent1, customIndividual parent2) {
        switch(params.crossover_type) {
            case CROSS_ONE_POINT: return one_point_cross(parent1, parent2);
            case CROSS_TWO_POINT: return two_point_cross(parent1, parent2);
            case CROSS_UNIFORM:
            default: return uniform_cross(parent1, parent2);
        }
    }

    /**
     * Uniform crossover
     * Randomly selects actions from both parents
     */
    private customIndividual uniform_cross(customIndividual parent1, customIndividual parent2) {
        return apply_crossover(parent1, parent2, this::u_bool);
    }

    @SuppressWarnings("unused")
    private boolean u_bool(int i) {
        return random.nextFloat() < 0.5;
    }

    /**
     * 1-point crossover
     * Selects 1 point along the length of the individuals. Selects actions from the first parent up until the chosen
     * point, then fills with actions from the second parent.
     */
    private customIndividual one_point_cross(customIndividual parent1, customIndividual parent2) {
        int length = parent1.get_length();
        chosenIdx1 = 1 + random.nextInt(length - 1);

        return apply_crossover(parent1, parent2, this::op_bool);
    }

    private boolean op_bool(int i) {
        return i < chosenIdx1;
    }

    /**
     * 2-point crossover
     * Selects 2 points along the length of the individuals. Selects actions from the first parent up until the first
     * chosen point, then from the second parent until the second chosen point, then fills with actions from the first
     * parent.
     */
    private customIndividual two_point_cross(customIndividual parent1, customIndividual parent2) {
        int length = parent1.get_length();
        chosenIdx1 = 1 + random.nextInt(length - 2);
        chosenIdx2 = chosenIdx1 + random.nextInt(length - chosenIdx1 - 1);

        return apply_crossover(parent1, parent2, this::tp_bool);
    }

    private boolean tp_bool(int i) {
        return i < chosenIdx1 || i > chosenIdx2;
    }

    /**
     * Function to apply crossover depending on the chosen type.
     * @param function - function to call which computes the correct condition for selecting actions from first parent,
     *                 given current action index.
     */
    private customIndividual apply_crossover(customIndividual parent1, customIndividual parent2, Function<Integer, Boolean> function) {
        int length = parent1.get_length();
        customIndividual ind = new customIndividual(length, random, parent1.get_max_actions());
        int[] actions = new int[length];

        // Make sure the gene size is minimum 1 and maximum individual length
        int gene_size = params.gene_size;
        if (params.gene_size != 1) {
            gene_size = Utils.clamp(1, params.gene_size, params.individual_length);
        }

        int i = 0;
        while (i < length) {
            // Apply the crossover function to find the next chosen action
            boolean functionResult = function.apply(i);

            // Set all actions part of this gene to the action corresponding to the gene's crossover result
            for (int j = 0; j < gene_size && (i + j) < length; j++) {
                if (functionResult) {
                    actions[i + j] = parent1.get_action(i);
                } else {
                    actions[i + j] = parent2.get_action(i);
                }
            }

            // Move to next gene
            i += gene_size;
        }

        ind.actions = actions;
        return ind;
    }


    /**
     * Testing. Test case:
     * 2 individuals of length 10, 0000000000, 1111111111.
     * All crossover types defined in params repeated N times.
     */
    public static void main(String[] args) {
        customRHEAParams params = new customRHEAParams();
        Map<String, Object[]> parameterValues = params.getParameterValues();
        params.individual_length = 10;
        Random random = new Random();
        int max_actions = 2;
        int repetitions = 5;

        customIndividual ind1 = new customIndividual(params.individual_length, random, max_actions);
        ind1.actions = new int[params.individual_length];

        customIndividual ind2 = new customIndividual(params.individual_length, random, max_actions);
        int[] actions = new int[params.individual_length];
        Arrays.fill(actions, 1);
        ind2.actions = actions;

        customCrossover c = new customCrossover(params, random);
        for (int i = 0; i < repetitions; i++) {
            for (Object o1 : parameterValues.get("crossover_type")) {
                params.crossover_type = (int) o1;
                System.out.println(params.interpret("crossover_type", params.crossover_type) + ": " +
                        c.cross(ind1, ind2).toString());
            }
        }
    }
}
