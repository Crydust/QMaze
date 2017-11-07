package qmaze.Agent;

import java.util.ArrayList;
import java.util.Random;
import qmaze.Environment.Coordinates;

/**
 * Q(S(t), A(t)) ← Q(S(t), A(t)) + α [ R(t+1) + γ max Q(S(t+1), a) − Q(S(t), A(t)) ].
 * 
 * @author katharine
 * I know about:
 *  - My memory of learned rewards and possible actions
 *  - My learning parameters
 * I am told about:
 *  - The surrounding open rooms.
 *  - If there is a reward in this room.
 *   and use them to make decisions about which room to go in next.
 * I don't know:
 *  - How many episodes I am trained for
 * I don't control:
 *  - My movements overall - instead I am told to move at each step
 *  and given information about the environment.
 */
public class Agent {
    
    private final AgentMemory memory;
    private AgentLearningParameters learningParameters;
    
    public Agent(AgentLearningParameters learningParameters) {
        this.learningParameters = learningParameters;
        this.memory = new AgentMemory();
    }
    
    public Coordinates location() {
        return memory.getCurrentState();
    }
    
    public void start(Coordinates startingState) {
        memory.setStartingState(startingState);
    }
    
    public void move(Coordinates nextState) {
        memory.move(nextState);
    }
        
    public Coordinates chooseAction(ArrayList<Coordinates> nextAvailableActions) throws NoWhereToGoException {
         //What if there are no available actions?
        // Should this validation happen further up...
        if (nextAvailableActions.isEmpty()) {
            throw new NoWhereToGoException(memory.getCurrentState());
        }
        
        //CODE TO SELECT NEXT ACTION
        
        final Random random = new Random();
        
        // decide wether we explore(a random move) or exploit(use the best move)
        final Coordinates currentLocation = location();
        final double maxQ = nextAvailableActions
                .stream()
                .mapToDouble(action->memory.rewardFromAction(currentLocation, action))
                .max()
                .orElse(0.0);
        final double epsilon = getLearningParameters().getEpsilon();
        final double randomNumber = random.nextDouble();
        final boolean nothingInMemory = maxQ < 0.000_1;
        final boolean exploring = (randomNumber < epsilon) || (nothingInMemory);
        
        final Coordinates nextAction;
        if (exploring) {
            // explore(a random move)
            if (nextAvailableActions.size() > 1) {
                nextAvailableActions.remove(memory.getPreviousState());
            }
            nextAction = nextAvailableActions.get(random.nextInt(nextAvailableActions.size()));
        } else {
            // exploit(use the best move)
            nextAction = nextAvailableActions.stream()
                    .sorted((Coordinates o1, Coordinates o2) -> -1 * Double.compare(
                            memory.rewardFromAction(currentLocation, o1),
                            memory.rewardFromAction(currentLocation, o2)))
                    .findFirst()
                    .get();
        }
        
        return nextAction;
    }
    
    public void takeAction(Coordinates actionTaken, double reward) {

        final Coordinates currentLocation = location();
        // Q(S(t), A(t)) ← Q(S(t), A(t)) + α [ R(t+1) + γ max Q(S(t+1), a) − Q(S(t), A(t)) ].
        //                 ^^^^^^^^^^^^^^^ this part is done in updateMemory
        final double currentQ = memory.rewardFromAction(currentLocation, actionTaken);
        final double maxQ = memory.actionsForState(actionTaken)
                .stream()
                .mapToDouble(action -> memory.rewardFromAction(actionTaken, action))
                .max()
                .orElse(0.0);
        final double alpha = learningParameters.getLearningRate();
        final double gamma = learningParameters.getGamma();
        final double qValue = alpha * (reward + (gamma  * maxQ) - currentQ);

        memory.updateMemory(actionTaken, qValue);
        memory.move(actionTaken);
    }
    
    
    public AgentMemory getMemory() {
        return memory;
    }
    
    public AgentLearningParameters getLearningParameters() {
        return learningParameters;
    }
    
    public void setLearningParameters(AgentLearningParameters parameters) {
        this.learningParameters = parameters;
    }
    
    public void introduceSelf(Coordinates startingState) {
        double alpha = learningParameters.getLearningRate();
        double gamma = learningParameters.getGamma();
        double epsilon = learningParameters.getEpsilon();
        System.out.println("I'm training with epsilon: " + epsilon + " gamma: " 
                + gamma + " and alpha: " + alpha + "\nStaring at " + startingState.toString());
    }
    
}
