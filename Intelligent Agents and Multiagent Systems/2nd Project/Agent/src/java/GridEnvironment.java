import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import util.ExperimentRunner;
import util.GridVisualizer;

public class GridEnvironment extends Environment {
    
    private Logger logger = Logger.getLogger("multi_agent." + GridEnvironment.class.getName());
    
    // Agent states
    private int agent1X = 1, agent1Y = 1;
    private int agent2X = 3, agent2Y = 3;
    private Set<String> inventory1 = new HashSet<>();
    private Set<String> inventory2 = new HashSet<>();
    
    // Goal states
    private boolean tablePainted = false;
    private boolean chairPainted = false;
    private boolean doorOpen = false;
    
    // Rewards tracking
    private double reward1 = 0, reward2 = 0;
    private int steps1 = 0, steps2 = 0;
    private int actions1 = 0, actions2 = 0;
    
    private Random random = new Random();
    
    // Dynamic positions for goals
    private int[] doorPosition = new int[2];
    private int[] tablePosition = new int[2];
    private int[] chairPosition = new int[2];
    
    // Item positions (fixed)
    private Map<String, int[]> itemPositions = new HashMap<>();
    
    // Grid dimensions
    public static final int GRID_WIDTH = 5;
    public static final int GRID_HEIGHT = 5;
    
    private static ExperimentRunner experimentRunner;
    private GridVisualizer visualizer;
    
    // DYNAMIC MODE SUPPORT
    private boolean isDynamicMode = false;
    private int stepsSinceLastChange = 0;
    private static final int STEPS_BEFORE_CHANGE = 3;
    
    // DEADLOCK DETECTION 
    private int stuckCounter = 0;
    private int lastAgent1X = 1, lastAgent1Y = 1;
    private int lastAgent2X = 3, lastAgent2Y = 3;
    private static final int MAX_STUCK_STEPS = 180;  
    private int totalStepsWithoutProgress = 0;
    
    // Track position history to detect oscillation
    private java.util.LinkedList<String> agent1History = new java.util.LinkedList<>();
    private java.util.LinkedList<String> agent2History = new java.util.LinkedList<>();
    private static final int HISTORY_SIZE = 10;
    
    @Override
    public void init(String[] args) {
        super.init(args);
        
        // Check mode: "static" or "dynamic"
        if (args != null && args.length > 0) {
            String mode = args[0].toLowerCase();
            isDynamicMode = mode.equals("dynamic");
            logger.info("Environment mode: " + (isDynamicMode ? "DYNAMIC" : "STATIC"));
        }
        
        if (experimentRunner == null) {
            experimentRunner = new ExperimentRunner(100, "multi-agent-" + (isDynamicMode ? "dynamic" : "static"));
        }
        
        visualizer = GridVisualizer.getInstance();
        
        initializePositions();
        updatePercepts();
        updateVisualization();
        
        String separator = "=".repeat(80);
        String msg = String.format("%s\nSTARTING RUN #%d | Multi-Agent Mode (%s)\n%s", 
            separator, experimentRunner.getCurrentRun(), 
            isDynamicMode ? "DYNAMIC" : "STATIC", separator);
        logger.info(msg);
        experimentRunner.logEvent(msg);
    }
    
    private void initializePositions() {
        itemPositions.put("brush", new int[]{1, 5});
        itemPositions.put("key", new int[]{1, 4});
        itemPositions.put("color", new int[]{5, 5});
        itemPositions.put("code", new int[]{3, 5});
        
        randomizeGoalPositions();
        stepsSinceLastChange = 0;
        stuckCounter = 0;
        totalStepsWithoutProgress = 0;
        lastAgent1X = agent1X;
        lastAgent1Y = agent1Y;
        lastAgent2X = agent2X;
        lastAgent2Y = agent2Y;
        agent1History.clear();
        agent2History.clear();
    }
    
    private void randomizeGoalPositions() {
        int[] oldDoor = doorPosition.clone();
        int[] oldTable = tablePosition.clone();
        int[] oldChair = chairPosition.clone();
        
        do {
            doorPosition = getRandomValidPosition();
            tablePosition = getRandomValidPosition();
            chairPosition = getRandomValidPosition();
        } while (positionsOverlap(doorPosition, tablePosition) || 
                 positionsOverlap(doorPosition, chairPosition) ||
                 positionsOverlap(tablePosition, chairPosition) ||
                 isStartPosition(doorPosition) ||
                 isStartPosition(tablePosition) ||
                 isStartPosition(chairPosition));
        
        // Log position changes in dynamic mode
        if (isDynamicMode && stepsSinceLastChange > 0) {
            String msg = String.format("*** DYNAMIC CHANGE (after %d steps) ***\n" +
                "Door: (%d,%d) -> (%d,%d)\n" +
                "Table: (%d,%d) -> (%d,%d)\n" +
                "Chair: (%d,%d) -> (%d,%d)",
                stepsSinceLastChange,
                oldDoor[0], oldDoor[1], doorPosition[0], doorPosition[1],
                oldTable[0], oldTable[1], tablePosition[0], tablePosition[1],
                oldChair[0], oldChair[1], chairPosition[0], chairPosition[1]);
            logger.warning(msg);
            experimentRunner.logEvent(msg);
        }
    }
    
    private boolean isStartPosition(int[] pos) {
        return (pos[0] == 1 && pos[1] == 1) || (pos[0] == 3 && pos[1] == 3);
    }
    
    private int[] getRandomValidPosition() {
        int x, y;
        do {
            x = random.nextInt(GRID_WIDTH) + 1;
            y = random.nextInt(GRID_HEIGHT) + 1;
        } while (!isValidPosition(x, y));
        return new int[]{x, y};
    }
    
    private boolean positionsOverlap(int[] pos1, int[] pos2) {
        return pos1[0] == pos2[0] && pos1[1] == pos2[1];
    }
    
    @Override
    public boolean executeAction(String agentName, Structure action) {
        // Stop accepting actions if experiment is complete
        if (experimentRunner.isComplete()) {
            logger.info("Experiment complete - ignoring action from " + agentName);
            return false;
        }
        
        String actionName = action.getFunctor();
        boolean result = false;
        
        try {
            boolean isAgent1 = agentName.equals("agent_1");
            
            // Log action attempt
            String actionLog = String.format("[%s] ACTION: %s", agentName, action);
            logger.info(actionLog);
            
            switch(actionName) {
                case "move":
                    String direction = action.getTerm(0).toString().replace("\"", "");
                    result = move(agentName, direction);
                    if (result) {
                        logger.info(String.format("[%s] Moved %s to (%d,%d)", 
                            agentName, direction, 
                            isAgent1 ? agent1X : agent2X,
                            isAgent1 ? agent1Y : agent2Y));
                    } else {
                        logger.warning(String.format("[%s] Move %s FAILED (blocked)", agentName, direction));
                    }
                    break;
                case "pickup":
                    String pickupItem = action.getTerm(0).toString().replace("\"", "");
                    result = pickup(agentName, pickupItem);
                    if (result) {
                        logger.info(String.format("[%s] Picked up %s | Inventory: %s", 
                            agentName, pickupItem, 
                            isAgent1 ? inventory1 : inventory2));
                    } else {
                        logger.warning(String.format("[%s] Pickup %s FAILED", agentName, pickupItem));
                    }
                    break;
                case "drop":
                    String dropItem = action.getTerm(0).toString().replace("\"", "");
                    result = drop(agentName, dropItem);
                    if (result) {
                        logger.info(String.format("[%s] Dropped %s", agentName, dropItem));
                    }
                    break;
                case "paint":
                    String paintObject = action.getTerm(0).toString().replace("\"", "");
                    result = paint(agentName, paintObject);
                    break;
                case "open":
                    result = openDoor(agentName);
                    break;
                default:
                    logger.warning("Unknown action: " + actionName);
                    return false;
            }
            
            if (result) {
                if (isAgent1) actions1++; else actions2++;
                
                // Check for dynamic position changes
                if (isDynamicMode && actionName.equals("move")) {
                    stepsSinceLastChange++;
                    if (stepsSinceLastChange >= STEPS_BEFORE_CHANGE) {
                        randomizeGoalPositions();
                        stepsSinceLastChange = 0;
                    }
                }
                
                updatePercepts();
                updateVisualization();
                checkCompletion();
            }
            
            // ALWAYS check for deadlock, even on failed actions
            checkDeadlock();
            
            return result;
            
        } catch (Exception e) {
            logger.severe("Error executing action " + actionName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void checkDeadlock() {
        // Track position history
        String agent1Pos = agent1X + "," + agent1Y;
        String agent2Pos = agent2X + "," + agent2Y;
        
        agent1History.add(agent1Pos);
        agent2History.add(agent2Pos);
        
        if (agent1History.size() > HISTORY_SIZE) {
            agent1History.removeFirst();
        }
        if (agent2History.size() > HISTORY_SIZE) {
            agent2History.removeFirst();
        }
        
        // Check if agents are oscillating (visiting same few positions repeatedly)
        boolean agent1Oscillating = isOscillating(agent1History);
        boolean agent2Oscillating = isOscillating(agent2History);
        
        // Check if agents haven't made meaningful progress
        boolean agent1Stuck = (agent1X == lastAgent1X && agent1Y == lastAgent1Y);
        boolean agent2Stuck = (agent2X == lastAgent2X && agent2Y == lastAgent2Y);
        
        if ((agent1Stuck && agent2Stuck) || (agent1Oscillating && agent2Oscillating)) {
            stuckCounter++;
        } else {
            // Decrease faster when progress is made
            if (!agent1Stuck && !agent2Stuck) {
                stuckCounter = Math.max(0, stuckCounter - 3); 
            } else if (!agent1Stuck || !agent2Stuck) {
                stuckCounter = Math.max(0, stuckCounter - 2); 
            }
            lastAgent1X = agent1X;
            lastAgent1Y = agent1Y;
            lastAgent2X = agent2X;
            lastAgent2Y = agent2Y;
        }
        
        // Also track total steps without goal completion
        totalStepsWithoutProgress++;
        
        // Force completion if stuck too long or taking too many steps
        if (stuckCounter >= MAX_STUCK_STEPS || totalStepsWithoutProgress > 700) {
            String reason = stuckCounter >= MAX_STUCK_STEPS ? 
                "agents stuck/oscillating for " + stuckCounter + " checks" : 
                "too many steps (" + totalStepsWithoutProgress + ") without progress";
            
            logger.warning("---FORCING RUN COMPLETION - " + reason + "---");
            logger.warning(String.format("Stuck counter: %d, Total steps: %d", 
                stuckCounter, totalStepsWithoutProgress));
            logger.warning(String.format("Agent1 oscillating: %s, Agent2 oscillating: %s",
                agent1Oscillating, agent2Oscillating));
            logger.warning(String.format("Agent1 stuck: %s, Agent2 stuck: %s",
                agent1Stuck, agent2Stuck));
            
            // Apply penalty for not completing
            double penalty = -2.0;
            reward1 += penalty;
            reward2 += penalty;
            
            // Force mark this run as complete
            forceCompletion();
        }
    }
    
    //Check if an agent is oscillating between a small set of positions

    private boolean isOscillating(java.util.LinkedList<String> history) {
        if (history.size() < HISTORY_SIZE) {
            return false; // Not enough data
        }
        
        // Count unique positions in recent history
        java.util.Set<String> uniquePositions = new java.util.HashSet<>();
        synchronized(history) {
            try {
                uniquePositions.addAll(history);
            } catch (Exception e) {
                return false;
            }
        }
        
        // Only if visiting 2 or fewer positions 
        return uniquePositions.size() <= 2;
    }
    
    private void forceCompletion() {
        double totalReward = reward1 + reward2;
        int totalSteps = steps1 + steps2;
        int totalActions = actions1 + actions2;
        
        String line = "#".repeat(80);
        String msg = String.format("\n%s\nRUN #%d FORCED COMPLETION (%s MODE) - DEADLOCK DETECTED\n" +
            "Agent1: Reward=%.4f Steps=%d Actions=%d\n" +
            "Agent2: Reward=%.4f Steps=%d Actions=%d\n" +
            "TOTAL:  Reward=%.4f Steps=%d Actions=%d\n" +
            "Goals: Table=%s Chair=%s Door=%s\n%s\n",
            line, experimentRunner.getCurrentRun(),
            isDynamicMode ? "DYNAMIC" : "STATIC",
            reward1, steps1, actions1, 
            reward2, steps2, actions2,
            totalReward, totalSteps, totalActions,
            tablePainted ? "✓" : "✗",
            chairPainted ? "✓" : "✗",
            doorOpen ? "✓" : "✗",
            line);
        
        logger.warning(msg);
        experimentRunner.logEvent(msg);
        
        experimentRunner.recordRun(totalReward, totalSteps, totalActions, 
            reward1, steps1, actions1, reward2, steps2, actions2, true);  // true = forced completion
        
        if (!experimentRunner.isComplete()) {
            reset();
        } else {
            logger.info("Experiment complete - not resetting for another run");
        }
    }
    
    private boolean move(String agentName, String direction) {
        boolean isAgent1 = agentName.equals("agent_1");
        int currentX = isAgent1 ? agent1X : agent2X;
        int currentY = isAgent1 ? agent1Y : agent2Y;
        
        int newX = currentX, newY = currentY;
        switch(direction) {
            case "right": newX++; break;
            case "left": newX--; break;
            case "up": newY++; break;
            case "down": newY--; break;
        }
        
        // Check validity and collision with other agent
        if (isValidPosition(newX, newY) && !isOccupiedByOtherAgent(newX, newY, agentName)) {
            if (isAgent1) {
                agent1X = newX;
                agent1Y = newY;
                steps1++;
            } else {
                agent2X = newX;
                agent2Y = newY;
                steps2++;
            }
            
            // LOG MOVEMENT TO DETAILED LOG
            String moveLog = String.format("[%s] MOVE: %s from (%d,%d) to (%d,%d)", 
                agentName, direction, currentX, currentY, newX, newY);
            logger.info(moveLog);
            experimentRunner.logEvent(moveLog);
            
            // Calculate cost
            Set<String> inventory = isAgent1 ? inventory1 : inventory2;
            double baseCost = 0.01;
            int incompatibleCount = countIncompatibleItems(inventory);
            int compatibleCount = inventory.size() - incompatibleCount;
            double totalCost = baseCost + (compatibleCount * 0.02) + (incompatibleCount * 0.03);
            
            if (isAgent1) reward1 -= totalCost;
            else reward2 -= totalCost;
            
            return true;
        }
        
        // LOG FAILED MOVEMENT TO DETAILED LOG
        String failLog = String.format("[%s] MOVE FAILED: %s from (%d,%d) - blocked or invalid", 
            agentName, direction, currentX, currentY);
        logger.info(failLog);
        experimentRunner.logEvent(failLog);
        
        return false;
    }

    private boolean isOccupiedByOtherAgent(int x, int y, String agentName) {
        if (agentName.equals("agent_1")) {
            return x == agent2X && y == agent2Y;
        } else {
            return x == agent1X && y == agent1Y;
        }
    }

    private boolean pickup(String agentName, String item) {
        boolean isAgent1 = agentName.equals("agent_1");
        int x = isAgent1 ? agent1X : agent2X;
        int y = isAgent1 ? agent1Y : agent2Y;
        Set<String> inventory = isAgent1 ? inventory1 : inventory2;
        Set<String> otherInventory = isAgent1 ? inventory2 : inventory1;
        
        if (otherInventory.contains(item)) {
            logger.warning(agentName + " tried to pickup " + item + " but other agent already has it");
            return false;
        }
        
        int[] pos = itemPositions.get(item);
        if (pos != null && x == pos[0] && y == pos[1] && inventory.size() < 3) {
            inventory.add(item);
            // Reset progress counter on successful item pickup
            totalStepsWithoutProgress = 0;
            return true;
        }
        return false;
    }

    private boolean drop(String agentName, String item) {
        boolean isAgent1 = agentName.equals("agent_1");
        Set<String> inventory = isAgent1 ? inventory1 : inventory2;
        
        if (inventory.contains(item)) {
            inventory.remove(item);
            return true;
        }
        return false;
    }

    private synchronized boolean paint(String agentName, String object) {
        boolean isAgent1 = agentName.equals("agent_1");
        int x = isAgent1 ? agent1X : agent2X;
        int y = isAgent1 ? agent1Y : agent2Y;
        Set<String> inventory = isAgent1 ? inventory1 : inventory2;
        
        if (!inventory.contains("brush") || !inventory.contains("color")) return false;
        
        if (object.equals("table") && x == tablePosition[0] && y == tablePosition[1] && !tablePainted) {
            tablePainted = true;
            if (isAgent1) reward1 += 1.0; else reward2 += 1.0;
            totalStepsWithoutProgress = 0; // Reset on goal completion
            logGoalCompletion(agentName, "TABLE PAINTED", 1.0);
            return true;
        }
        
        if (object.equals("chair") && x == chairPosition[0] && y == chairPosition[1] && !chairPainted) {
            chairPainted = true;
            if (isAgent1) reward1 += 1.0; else reward2 += 1.0;
            totalStepsWithoutProgress = 0; // Reset on goal completion
            logGoalCompletion(agentName, "CHAIR PAINTED", 1.0);
            return true;
        }
        return false;
    }

    private synchronized boolean openDoor(String agentName) {
        boolean isAgent1 = agentName.equals("agent_1");
        int x = isAgent1 ? agent1X : agent2X;
        int y = isAgent1 ? agent1Y : agent2Y;
        Set<String> inventory = isAgent1 ? inventory1 : inventory2;
        
        if (!inventory.contains("key") || !inventory.contains("code")) return false;
        
        if (x == doorPosition[0] && y == doorPosition[1] && !doorOpen) {
            doorOpen = true;
            if (isAgent1) reward1 += 0.8; else reward2 += 0.8;
            totalStepsWithoutProgress = 0; // Reset on goal completion
            logGoalCompletion(agentName, "DOOR OPENED", 0.8);
            return true;
        }
        return false;
    }
    
    private void logGoalCompletion(String agentName, String goalName, double reward) {
        String line = "=".repeat(60);
        String msg = String.format("\n%s\n[%s] GOAL COMPLETE: %s (+%.2f reward)\n" +
            "Agent Totals: A1=%.4f A2=%.4f | Steps: A1=%d A2=%d\n%s\n", 
            line, agentName, goalName, reward,
            reward1, reward2, steps1, steps2, line);
        logger.info(msg);
        experimentRunner.logEvent(msg);
    }
    
    public static boolean isValidPosition(int x, int y) {
        return x >= 1 && x <= GRID_WIDTH && y >= 1 && y <= GRID_HEIGHT && !isObstacle(x, y);
    }
    
    public static boolean isObstacle(int x, int y) {
        return (x == 2 && y == 1) || (x == 2 && y == 2) || (x == 4 && y == 4) || (x == 4 && y == 5);
    }
    
    private int countIncompatibleItems(Set<String> inventory) {
        int count = 0;
        for (String item : inventory) {
            boolean isCompatible = false;
            if (item.equals("brush") || item.equals("color")) {
                isCompatible = (!tablePainted || !chairPainted);
            } else if (item.equals("key") || item.equals("code")) {
                isCompatible = !doorOpen;
            }
            if (!isCompatible) count++;
        }
        return count;
    }
    
    public boolean isGoalComplete() {
        return tablePainted && chairPainted && doorOpen;
    }
    
    private void checkCompletion() {
        if (isGoalComplete()) {
            double totalReward = reward1 + reward2;
            int totalSteps = steps1 + steps2;
            int totalActions = actions1 + actions2;
            
            String line = "#".repeat(80);
            String msg = String.format("\n%s\nRUN #%d COMPLETED (%s MODE)\n" +
                "Agent1: Reward=%.4f Steps=%d Actions=%d\n" +
                "Agent2: Reward=%.4f Steps=%d Actions=%d\n" +
                "TOTAL:  Reward=%.4f Steps=%d Actions=%d\n%s\n",
                line, experimentRunner.getCurrentRun(),
                isDynamicMode ? "DYNAMIC" : "STATIC",
                reward1, steps1, actions1, 
                reward2, steps2, actions2,
                totalReward, totalSteps, totalActions, line);
            
            logger.info(msg);
            experimentRunner.logEvent(msg);
            
            experimentRunner.recordRun(totalReward, totalSteps, totalActions, 
                reward1, steps1, actions1, reward2, steps2, actions2, false);  // false = normal completion
            
            if (!experimentRunner.isComplete()) {
                reset();
            } else {
                logger.info("Experiment complete - not resetting for another run");
            }
        }
    }
    
    public void reset() {
        agent1X = 1; agent1Y = 1;
        agent2X = 3; agent2Y = 3;
        inventory1.clear();
        inventory2.clear();
        tablePainted = false;
        chairPainted = false;
        doorOpen = false;
        reward1 = 0; reward2 = 0;
        steps1 = 0; steps2 = 0;
        actions1 = 0; actions2 = 0;
        
        randomizeGoalPositions();
        stepsSinceLastChange = 0;
        stuckCounter = 0;
        totalStepsWithoutProgress = 0;
        lastAgent1X = agent1X;
        lastAgent1Y = agent1Y;
        lastAgent2X = agent2X;
        lastAgent2Y = agent2Y;
        agent1History.clear();
        agent2History.clear();
        
        updatePercepts();
        updateVisualization();
    }
    
    private void updatePercepts() {
        clearPercepts();
        clearPercepts("agent_1");
        clearPercepts("agent_2");
        
        // Common percepts
        addPercept(Literal.parseLiteral("table_at(" + tablePosition[0] + "," + tablePosition[1] + ")"));
        addPercept(Literal.parseLiteral("chair_at(" + chairPosition[0] + "," + chairPosition[1] + ")"));
        addPercept(Literal.parseLiteral("door_at(" + doorPosition[0] + "," + doorPosition[1] + ")"));
        
        for (Map.Entry<String, int[]> entry : itemPositions.entrySet()) {
            int[] pos = entry.getValue();
            addPercept(Literal.parseLiteral("item_at(" + entry.getKey() + "," + pos[0] + "," + pos[1] + ")"));
        }
        
        if (tablePainted) addPercept(Literal.parseLiteral("colored(table)"));
        if (chairPainted) addPercept(Literal.parseLiteral("colored(chair)"));
        if (doorOpen) addPercept(Literal.parseLiteral("open(door)"));
        
        addPercept(Literal.parseLiteral("goal_complete(" + isGoalComplete() + ")"));
        
        // Agent1 specific percepts
        addPercept("agent_1", Literal.parseLiteral("position(" + agent1X + "," + agent1Y + ")"));
        addPercept("agent_1", Literal.parseLiteral("carrying(" + inventory1.size() + ")"));
        for (String item : inventory1) {
            addPercept("agent_1", Literal.parseLiteral("carrying_item(" + item + ")"));
        }
        addPercept("agent_1", Literal.parseLiteral("other_agent_at(" + agent2X + "," + agent2Y + ")"));
        for (String item : inventory2) {
            addPercept("agent_1", Literal.parseLiteral("other_agent_has(" + item + ")"));
        }
        
        // Agent2 specific percepts
        addPercept("agent_2", Literal.parseLiteral("position(" + agent2X + "," + agent2Y + ")"));
        addPercept("agent_2", Literal.parseLiteral("carrying(" + inventory2.size() + ")"));
        for (String item : inventory2) {
            addPercept("agent_2", Literal.parseLiteral("carrying_item(" + item + ")"));
        }
        addPercept("agent_2", Literal.parseLiteral("other_agent_at(" + agent1X + "," + agent1Y + ")"));
        for (String item : inventory1) {
            addPercept("agent_2", Literal.parseLiteral("other_agent_has(" + item + ")"));
        }
    }
    
    private void updateVisualization() {
        if (visualizer != null) {
            GridVisualizer.GridState state = new GridVisualizer.GridState();
            state.currentRun = experimentRunner.getCurrentRun();
            state.agent1X = agent1X;
            state.agent1Y = agent1Y;
            state.agent2X = agent2X;
            state.agent2Y = agent2Y;
            state.agent1Reward = reward1;
            state.agent2Reward = reward2;
            state.totalReward = reward1 + reward2;
            state.totalSteps = steps1 + steps2;
            state.totalActions = actions1 + actions2;
            state.agent1Inventory = new HashSet<>(inventory1);
            state.agent2Inventory = new HashSet<>(inventory2);
            state.items = new HashMap<>(itemPositions);
            state.doorPos = doorPosition.clone();
            state.tablePos = tablePosition.clone();
            state.chairPos = chairPosition.clone();
            state.tablePainted = tablePainted;
            state.chairPainted = chairPainted;
            state.doorOpen = doorOpen;
            
            visualizer.updateGrid(state);
        }
    }
}