package players.custom_rhea;

import core.GameState;
import players.optimisers.ParameterizedPlayer;
import players.Player;
import players.custom_rhea.utils.customRHEAParams;
import utils.ElapsedCpuTimer;
import utils.Types;

import java.util.Random;
import static players.rhea.utils.Constants.TIME_BUDGET;

public class customRHEAPlayer extends ParameterizedPlayer {
    private customRollingHorizonPlayer player;
    private customGameInterface gInterface;
    private customRHEAParams params;

    public customRHEAPlayer(long seed, int playerID) {
        this(seed, playerID, new customRHEAParams());
    }

    public customRHEAPlayer(long seed, int playerID, customRHEAParams params) {
        super(seed, playerID, params);
        reset(seed, playerID);
    }

    @Override
    public void reset(long seed, int playerID) {
        super.reset(seed, playerID);

        // Make sure we have parameters
        this.params = (customRHEAParams) getParameters();
        if (this.params == null) {
            this.params = new customRHEAParams();
            super.setParameters(this.params);
        }

        // Set up random generator
        Random randomGenerator = new Random(seed);

        // Create interface with game
        gInterface = new customGameInterface(this.params, randomGenerator, playerID - Types.TILETYPE.AGENT0.getKey());

        // Set up player
        player = new customRollingHorizonPlayer(randomGenerator, this.params, gInterface);
    }

    @Override
    public Types.ACTIONS act(GameState gs) {
        ElapsedCpuTimer elapsedTimer = null;
        if (params.budget_type == TIME_BUDGET) {
            elapsedTimer = new ElapsedCpuTimer();
            elapsedTimer.setMaxTimeMillis(params.time_budget);
        }
        setup(gs, elapsedTimer);
        return gInterface.translate(player.getAction(elapsedTimer, gs.nActions()));
    }

    @Override
    public int[] getMessage() {
        // default message
        return new int[Types.MESSAGE_LENGTH];
    }

    private void setup(GameState rootState, ElapsedCpuTimer elapsedTimer) {
        gInterface.initTick(rootState, elapsedTimer);
    }

    @Override
    public Player copy() {
        return new customRHEAPlayer(seed, playerID, params);
    }
}
