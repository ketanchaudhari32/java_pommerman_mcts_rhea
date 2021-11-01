import core.Game;
import players.*;
import players.mcts.*;
import players.rhea.RHEAPlayer;
import players.rhea.utils.Constants;
import players.rhea.utils.RHEAParams;
import utils.*;

import java.util.*;

import static utils.Types.VISUALS;

public class RunAnkur {

    private static void printHelp()
    {
        System.out.println("Usage: java Run [args]");
        System.out.println("\t [arg index = 0] Game Mode. 0: FFA; 1: TEAM");
        System.out.println("\t [arg index = 1] Number of level generation seeds. \"-1\" to execute with the ones from paper (20).");
        System.out.println("\t [arg index = 2] Repetitions per seed [N]. \"1\" for one game only with visuals.");
        System.out.println("\t [arg index = 3] Vision Range [VR]. (0, 1, 2 for PO; -1 for Full Observability)");
        System.out.println("\t [arg index = 4-7] Agents. When in TEAM, agents are mates as indices 4-6, 5-7:");
        System.out.println("\t\t 0 DoNothing");
        System.out.println("\t\t 1 Random");
        System.out.println("\t\t 2 OSLA");
        System.out.println("\t\t 3 SimplePlayer");
        System.out.println("\t\t 4 RHEA 200 itereations, shift buffer, pop size 1, random init, length: 12");
        System.out.println("\t\t 5 MCTS 200 iterations, length: 12");
    }

    public static void main(String[] args) {

        //default
        if(args.length == 0)
            args = new String[]{"0", "1", "1", "-1", "2", "3", "4", "5"};

        if(args.length != 8) {
            printHelp();
            return;
        }

        try {

            Random rnd = new Random();

            // Create players
            ArrayList<Player> players = new ArrayList<>();
            int playerID = Types.TILETYPE.AGENT0.getKey();
            int boardSize = Types.BOARD_SIZE;

            Types.GAME_MODE gMode = Types.GAME_MODE.FFA;
            if(Integer.parseInt(args[0]) == 1)
                gMode = Types.GAME_MODE.TEAM;

            int S = Integer.parseInt(args[1]);
            int N = Integer.parseInt(args[2]);
            Types.DEFAULT_VISION_RANGE = Integer.parseInt(args[3]);

            long seeds[];

            if (S == -1)
            {
                //Special case, these seeds are fixed for the experiments in the paper:
                seeds = new long[] {93988, 19067, 64416, 83884, 55636, 27599, 44350, 87872, 40815,
                        11772, 58367, 17546, 75375, 75772, 58237, 30464, 27180, 23643, 67054, 19508};
            }else
            {
                if(S <= 0)
                    S = 1;

                //Otherwise, all seeds are random
                seeds = new long[S];
                for(int i = 0; i < S; i++)
                    seeds[i] = rnd.nextInt(100000);
            }

            long seed = 0;

            String[] playerStr = new String[4];

            for(int i = 4; i <= 7; ++i) {
                int agentType = Integer.parseInt(args[i]);
                Player p = null;


                switch(agentType) {
                    case 0:
                        p = new DoNothingPlayer(playerID++);
                        playerStr[i-4] = "DoNothing";
                        break;
                    case 1:
                        p = new RandomPlayer(seed, playerID++);
                        playerStr[i-4] = "Random";
                        break;
                    case 2:
                        p = new OSLAPlayer(seed, playerID++);
                        playerStr[i-4] = "OSLA";
                        break;
                    case 3:
                        p = new SimplePlayer(seed, playerID++);
                        playerStr[i-4] = "RuleBased";
                        break;
                    case 4:
                        RHEAParams rheaParams = new RHEAParams();
                        rheaParams.budget_type = Constants.ITERATION_BUDGET;
                        rheaParams.iteration_budget = 200;
                        rheaParams.individual_length = 12;
                        rheaParams.heurisic_type = Constants.ADVANCED_HEURISTIC;
                        rheaParams.mutation_rate = 0.5;

                        p = new RHEAPlayer(seed, playerID++, rheaParams);
                        playerStr[i-4] = "RHEA";
                        break;
                    case 5:
                        MCTSParams mctsParams = new MCTSParams();
                        mctsParams.stop_type = mctsParams.STOP_ITERATIONS;
                        mctsParams.num_iterations = 400;
                        mctsParams.rollout_depth = 20;

                        mctsParams.heuristic_method = mctsParams.ADVANCED_HEURISTIC;
                        p = new MCTSPlayer(seed, playerID++, mctsParams);
                        playerStr[i-4] = "MCTS";
                        break;

                    case 6:
                        MCTSParams mctsParams6 = new MCTSParams();
                        mctsParams6.stop_type = mctsParams6.STOP_ITERATIONS;
                        mctsParams6.num_iterations = 400;
                        mctsParams6.rollout_depth = 20;

                        mctsParams6.heuristic_method = mctsParams6.ADVANCED_HEURISTIC;
                        p = new MCTSPlayerWithVariance(seed, playerID++, mctsParams6);
                        playerStr[i-4] = "MCTSPlayerWithVariance";
                        break;

                    case 7:
                        MCTSParams mctsParams7 = new MCTSParams();
                        mctsParams7.stop_type = mctsParams7.STOP_ITERATIONS;
                        mctsParams7.num_iterations = 400;
                        mctsParams7.rollout_depth = 20;

                        mctsParams7.heuristic_method = mctsParams7.CUSTOM_HEURISTIC;
                        p = new MCTSPlayerWithBias(seed, playerID++, mctsParams7);
                        playerStr[i-4] = "MCTSPlayerWithBias";
                        break;

                    case 8:
                        MCTSParams mctsParams8 = new MCTSParams();
                        mctsParams8.stop_type = mctsParams8.STOP_ITERATIONS;
                        mctsParams8.num_iterations = 400;
                        mctsParams8.rollout_depth = 20;

                        mctsParams8.heuristic_method = mctsParams8.ADVANCED_HEURISTIC;
                        p = new MCTSPlayerWithDecayingReward(seed, playerID++, mctsParams8);
                        playerStr[i-4] = "MCTSPlayerWithDecayingReward";
                        break;
                    case 9:
                        MCTSParams mctsParams9 = new MCTSParams();
                        mctsParams9.stop_type = mctsParams9.STOP_ITERATIONS;
                        mctsParams9.num_iterations = 400;
                        mctsParams9.rollout_depth = 20;

                        mctsParams9.heuristic_method = mctsParams9.ADVANCED_HEURISTIC;
                        p = new MCTSPlayerWithConK(seed, playerID++, mctsParams9);
                        playerStr[i-4] = "MCTSPlayerWithConK";
                        break;
                    case 10:
                        MCTSParams mctsParams10 = new MCTSParams();
                        mctsParams10.stop_type = mctsParams10.STOP_ITERATIONS;
                        mctsParams10.num_iterations = 400;
                        mctsParams10.rollout_depth = 20;

                        mctsParams10.heuristic_method = mctsParams10.ADVANCED_HEURISTIC;
                        p = new MCTSPlayerWithBiasKDecay(seed, playerID++, mctsParams10);
                        playerStr[i-4] = "MCTSPlayerWithBiasKDecay";
                        break;
                    case 11:
                        MCTSParams mctsParams11 = new MCTSParams();
                        mctsParams11.stop_type = mctsParams11.STOP_ITERATIONS;
                        mctsParams11.num_iterations = 400;
                        mctsParams11.rollout_depth = 20;

                        mctsParams11.heuristic_method = mctsParams11.ADVANCED_HEURISTIC;
                        p = new MCTSPlayerWithBiasKDecay(seed, playerID++, mctsParams11);
                        playerStr[i-4] = "MCTSPlayerWithvarianceKDecay";
                    case 12:
                        MCTSParams mctsParams12 = new MCTSParams();
                        mctsParams12.stop_type = mctsParams12.STOP_ITERATIONS;
                        mctsParams12.num_iterations = 400;
                        mctsParams12.rollout_depth = 20;

                        mctsParams12.heuristic_method = mctsParams12.ADVANCED_HEURISTIC;
                        p = new MCTSPlayerWithBiasPrunning(seed, playerID++, mctsParams12);
                        playerStr[i-4] = "MCTSPlayerWithvarianceKDecay";
                        break;
                    default:
                        System.out.println("WARNING: Invalid agent ID: " + agentType );
                }

                players.add(p);
            }

            String gameIdStr = "";
            for(int i = 0; i <= 7; ++i) {
                gameIdStr += args[i];
                if(i != 7)
                    gameIdStr+="-";
            }

            Game game = new Game(seeds[0], boardSize, gMode, gameIdStr);

            // Make sure we have exactly NUM_PLAYERS players
            assert players.size() == Types.NUM_PLAYERS;
            game.setPlayers(players);

            System.out.print(gameIdStr + " [");
            for(int i = 0; i < playerStr.length; ++i) {
                System.out.print(playerStr[i]);
                if(i != playerStr.length-1)
                    System.out.print(',');

            }
            System.out.println("]");

            runGames(game, seeds, N, false);
        } catch(Exception e) {
            e.printStackTrace();
            printHelp();
        }
    }

    /**
     * Runs 1 game.
     * @param g - game to run
     * @param ki1 - primary key controller
     * @param ki2 - secondary key controller
     * @param separateThreads - if separate threads should be used for the agents or not.
     */
    public static void runGame(Game g, KeyController ki1, KeyController ki2, boolean separateThreads) {
        WindowInput wi = null;
        GUI frame = null;
        if (VISUALS) {
            frame = new GUI(g, "Java-Pommerman", ki1, false, true);
            wi = new WindowInput();
            wi.windowClosed = false;
            frame.addWindowListener(wi);
            frame.addKeyListener(ki1);
            frame.addKeyListener(ki2);
        }

        g.run(frame, wi, separateThreads);
    }

    public static void runGames(Game g, long seeds[], int repetitions, boolean useSeparateThreads){
        int numPlayers = g.getPlayers().size();
        int[] winCount = new int[numPlayers];
        int[] tieCount = new int[numPlayers];
        int[] lossCount = new int[numPlayers];

        int[] overtimeCount = new int[numPlayers];

        int numSeeds = seeds.length;
        int totalNgames = numSeeds * repetitions;

        for(int s = 0; s<numSeeds; s++) {
            long seed = seeds[s];

            for (int i = 0; i < repetitions; i++) {
                long playerSeed = System.currentTimeMillis();

                System.out.print( playerSeed + ", " + seed + ", " + (s*repetitions + i) + "/" + totalNgames + ", ");

                g.reset(seed);
                EventsStatistics.REP = i;
                GameLog.REP = i;

                // Set random seed for players and reset them
                ArrayList<Player> players = g.getPlayers();
                for (int p = 0; p < g.nPlayers(); p++) {
                    players.get(p).reset(playerSeed, p + Types.TILETYPE.AGENT0.getKey());
                }
                Types.RESULT[] results = g.run(useSeparateThreads);

                for (int pIdx = 0; pIdx < numPlayers; pIdx++) {
                    switch (results[pIdx]) {
                        case WIN:
                            winCount[pIdx]++;
                            break;
                        case TIE:
                            tieCount[pIdx]++;
                            break;
                        case LOSS:
                            lossCount[pIdx]++;
                            break;
                    }
                }

                int[] overtimes = g.getPlayerOvertimes();
                for(int j = 0; j < overtimes.length; ++j)
                    overtimeCount[j] += overtimes[j];

            }
        }

        //Done, show stats
        System.out.println("N \tWin \tTie \tLoss \tPlayer (overtime average)");
        for (int pIdx = 0; pIdx < numPlayers; pIdx++) {
            String player = g.getPlayers().get(pIdx).getClass().toString().replaceFirst("class ", "");

            double winPerc = winCount[pIdx] * 100.0 / (double)totalNgames;
            double tiePerc = tieCount[pIdx] * 100.0 / (double)totalNgames;
            double lossPerc = lossCount[pIdx] * 100.0 / (double)totalNgames;
            double overtimesAvg = overtimeCount[pIdx] / (double)totalNgames;

            System.out.println(totalNgames + "\t" + winPerc + "%\t" + tiePerc + "%\t" + lossPerc + "%\t" + player + " (" + overtimesAvg + ")" );
        }
    }
}
