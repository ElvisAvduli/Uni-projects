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

public class GridEnvironment extends Environment {
    
    private Logger logger = Logger.getLogger("intelligent_agent." + GridEnvironment.class.getName());
    
    // Grid state
    private int agentX = 1, agentY = 1;
    private Set<String> inventory = new HashSet<>();
    private boolean tablePainted = false;
    private boolean chairPainted = false;
    private boolean doorOpen = false;
    private double totalReward = 0;
    private int steps = 0;
    private int totalActions = 0;
    
    // DYNAMIC ENVIRONMENT - Î¡Î¥Î˜ÎœÎ™Î–Î•Î¤Î‘Î™ ÎœÎ• Î Î‘Î¡Î‘ÎœÎ•Î¤Î¡ÎŸ
    private int movementStepsSinceLastChange = 0;
    private static final int STEPS_BETWEEN_CHANGES = 3;
    private boolean dynamicMode = false;
    private int totalEnvironmentChanges = 0;
    
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
    
    // Goal completion tracking
    private int stepsToCompleteTable = -1; private int actionsToCompleteTable = -1;
    private int stepsToCompleteChair = -1; private int actionsToCompleteChair = -1;
    private int stepsToCompleteDoor = -1;  private int actionsToCompleteDoor = -1;
    
    @Override
    public void init(String[] args) {
        super.init(args);
        
        // Î Î‘Î¡Î‘ÎœÎ•Î¤Î¡ÎŸÎ£: "dynamic" Î® "static" (default: static)
        dynamicMode = (args.length > 0 && args[0].equals("dynamic"));
        
        if (experimentRunner == null) {
            experimentRunner = new ExperimentRunner(100, dynamicMode ? "dynamic" : "static");
        }
        
        initializePositions();
        updatePercepts();
        
        // Log Header
        String separator = "=".repeat(80);
        String msg = String.format("%s\nSTARTING RUN #%d | Mode: %s\n%s", 
            separator, experimentRunner.getCurrentRun(), (dynamicMode ? "DYNAMIC" : "STATIC"), separator);
        logger.info(msg);
        experimentRunner.logEvent(msg);
    }
    
    private void initializePositions() {
        itemPositions.put("brush", new int[]{1, 5});
        itemPositions.put("key", new int[]{1, 4});
        itemPositions.put("color", new int[]{5, 5});
        itemPositions.put("code", new int[]{3, 5});
        
        if (experimentRunner.getCurrentRun() == 1) {
            experimentRunner.logEvent("[CONFIG] Fixed Items: Brush(1,5), Key(1,4), Color(5,5), Code(3,5)");
        }
        randomizeGoalPositions(true);
    }
    
    private void randomizeGoalPositions(boolean isInitial) {
        doorPosition = getRandomValidPosition();
        tablePosition = getRandomValidPosition();
        chairPosition = getRandomValidPosition();
        
        while (positionsOverlap(doorPosition, tablePosition) || 
               positionsOverlap(doorPosition, chairPosition) ||
               positionsOverlap(tablePosition, chairPosition) ||
               (doorPosition[0] == 1 && doorPosition[1] == 1) ||
               (tablePosition[0] == 1 && tablePosition[1] == 1) ||
               (chairPosition[0] == 1 && chairPosition[1] == 1)) {
            doorPosition = getRandomValidPosition();
            tablePosition = getRandomValidPosition();
            chairPosition = getRandomValidPosition();
        }
        
        if (!isInitial) {
            totalEnvironmentChanges++;
            String line = "-".repeat(60);
            String msg = String.format("\n%s\n[ENV CHANGE #%d] Goals Relocated!\n   Door->(%d,%d) Table->(%d,%d) Chair->(%d,%d)\n%s", 
                line, totalEnvironmentChanges, 
                doorPosition[0], doorPosition[1], tablePosition[0], tablePosition[1], chairPosition[0], chairPosition[1],
                line);
            logger.warning(msg);
            experimentRunner.logEvent(msg);
        }
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
    public boolean executeAction(String ag, Structure action) {
        String actionName = action.getFunctor();
        boolean result = false;
        totalActions++;
        
        try {
            switch(actionName) {
                case "move":
                    String direction = action.getTerm(0).toString().replace("\"", "");
                    result = move(direction);
                    break;
                case "pickup":
                    String pickupItem = action.getTerm(0).toString().replace("\"", "");
                    result = pickup(pickupItem);
                    if(result) logMajorAction("PICKUP", pickupItem);
                    break;
                case "drop":
                    String dropItem = action.getTerm(0).toString().replace("\"", "");
                    result = drop(dropItem);
                    if(result) logMajorAction("DROP", dropItem);
                    break;
                case "paint":
                    String paintObject = action.getTerm(0).toString().replace("\"", "");
                    result = paint(paintObject);
                    break;
                case "open":
                    result = openDoor();
                    break;
                default:
                    logger.warning("Unknown action: " + actionName);
                    return false;
            }
            
            if (result) {
                if (dynamicMode && !isGoalComplete() && actionName.equals("move")) {
                    movementStepsSinceLastChange++;
                    if (movementStepsSinceLastChange >= STEPS_BETWEEN_CHANGES) {
                        randomizeGoalPositions(false);
                        movementStepsSinceLastChange = 0;
                        addPercept(Literal.parseLiteral("environment_changed"));
                    }
                }
                updatePercepts();
                checkAndRecordCompletion();
            }
            return result;
            
        } catch (Exception e) {
            logger.severe("Error executing action " + actionName + ": " + e.getMessage());
            return false;
        }
    }
    
    // VISUAL LOGGING HELPER
    private void logMajorAction(String type, String detail) {
        String line = "-".repeat(50);
        String msg = String.format("%s\n[ACTION] %-8s | %-10s | At: (%d,%d) | Inv: %s\n%s", 
            line, type, detail, agentX, agentY, inventory.toString(), line);
        experimentRunner.logEvent(msg);
    }

    // Î”Î™ÎŸÎ¡Î˜Î©ÎœÎ•ÎÎŸ: Î£Ï‰ÏƒÏ„ÏŒÏ‚ Ï…Ï€Î¿Î»Î¿Î³Î¹ÏƒÎ¼ÏŒÏ‚ ÎºÏŒÏƒÏ„Î¿Ï…Ï‚
    private boolean move(String direction) {
        int newX = agentX, newY = agentY;
        switch(direction) {
            case "right": newX++; break;
            case "left": newX--; break;
            case "up": newY++; break;
            case "down": newY--; break;
        }
        
        if (isValidPosition(newX, newY)) {
            agentX = newX;
            agentY = newY;
            steps++;
            
            // Î£Î©Î£Î¤ÎŸÎ£ Î¥Î ÎŸÎ›ÎŸÎ“Î™Î£ÎœÎŸÎ£ ÎšÎŸÎ£Î¤ÎŸÎ¥Î£
            // Base cost: -0.01 (Ï‡Ï‰ÏÎ¯Ï‚ items)
            double baseCost = 0.01;
            
            // Î¥Ï€Î¿Î»Î¿Î³Î¯Î¶Î¿Ï…Î¼Îµ compatible items (Ï‡ÏÎµÎ¹Î¬Î¶Î¿Î½Ï„Î±Î¹ Î³Î¹Î± Ï„Î¿Ï…Ï‚ ÏƒÏ„ÏŒÏ‡Î¿Ï…Ï‚)
            int incompatibleCount = countIncompatibleItems();
            int compatibleCount = inventory.size() - incompatibleCount;
            
            // Compatible items: -0.02 Î­ÎºÎ±ÏƒÏ„Î¿
            double compatibleCost = compatibleCount * 0.02;
            
            // Incompatible items: -0.03 Î­ÎºÎ±ÏƒÏ„Î¿ (Î£Î¥ÎÎŸÎ›Î™ÎšÎ‘, ÏŒÏ‡Î¹ Ï€ÏÏŒÏƒÎ¸ÎµÏ„Î±)
            double incompatibleCost = incompatibleCount * 0.03;
            
            double totalCost = baseCost + compatibleCost + incompatibleCost;
            totalReward -= totalCost;
            
            // Detailed step logging
            String stepLog = String.format("   -> Step %-4d | Move: %-5s | Pos: (%d,%d) | " +
                "Items: %d (compat:%d incompat:%d) | Cost: -%.4f | Util: %.4f", 
                steps, direction, agentX, agentY, 
                inventory.size(), compatibleCount, incompatibleCount,
                totalCost, totalReward);
            experimentRunner.logEvent(stepLog);
            
            return true;
        }
        experimentRunner.logEvent("   -> BLOCKED | Move: " + direction + " | Pos: (" + agentX + "," + agentY + ")");
        return false;
    }
    
    private boolean pickup(String item) {
        int[] pos = itemPositions.get(item);
        if (pos != null && agentX == pos[0] && agentY == pos[1] && inventory.size() < 3) {
            inventory.add(item);
            return true;
        }
        return false;
    }
    
    private boolean drop(String item) {
        if (inventory.contains(item)) {
            inventory.remove(item);
            return true;
        }
        return false;
    }
    
    private boolean paint(String object) {
        if (!inventory.contains("brush") || !inventory.contains("color")) return false;
        
        if (object.equals("table") && agentX == tablePosition[0] && agentY == tablePosition[1]) {
            tablePainted = true;
            totalReward += 1.0;
            stepsToCompleteTable = steps; actionsToCompleteTable = totalActions;
            logGoalCompletion("TABLE PAINTED");
            return true;
        }
        
        if (object.equals("chair") && agentX == chairPosition[0] && agentY == chairPosition[1]) {
            chairPainted = true;
            totalReward += 1.0;
            stepsToCompleteChair = steps; actionsToCompleteChair = totalActions;
            logGoalCompletion("CHAIR PAINTED");
            return true;
        }
        return false;
    }
    
    private boolean openDoor() {
        if (!inventory.contains("key") || !inventory.contains("code")) return false;
        
        if (agentX == doorPosition[0] && agentY == doorPosition[1]) {
            doorOpen = true;
            totalReward += 0.8;
            stepsToCompleteDoor = steps; actionsToCompleteDoor = totalActions;
            logGoalCompletion("DOOR OPENED");
            return true;
        }
        return false;
    }
    
    private void logGoalCompletion(String goalName) {
        String line = "=".repeat(60);
        String msg = String.format("\n%s\n    GOAL COMPLETE: %s \n   Reward: %.4f | Total Steps: %d\n%s\n", 
            line, goalName, totalReward, steps, line);
        logger.info(msg);
        experimentRunner.logEvent(msg);
    }
    
    public static boolean isValidPosition(int x, int y) {
        return x >= 1 && x <= GRID_WIDTH && y >= 1 && y <= GRID_HEIGHT && !isObstacle(x, y);
    }
    
    public static boolean isObstacle(int x, int y) {
        return (x == 2 && y == 1) || (x == 2 && y == 2) || (x == 4 && y == 4) || (x == 4 && y == 5);
    }
    
    // Î”Î™ÎŸÎ¡Î˜Î©ÎœÎ•ÎÎŸ: ÎšÎ±Î»ÏÏ„ÎµÏÎ· Î»Î¿Î³Î¹ÎºÎ® Î³Î¹Î± incompatible items
    private int countIncompatibleItems() {
        int count = 0;
        for (String item : inventory) {
            boolean isCompatible = false;
            
            // Painting items are compatible if ANY painting goal remains
            if (item.equals("brush") || item.equals("color")) {
                isCompatible = (!tablePainted || !chairPainted);
            }
            // Door items are compatible if door goal remains
            else if (item.equals("key") || item.equals("code")) {
                isCompatible = !doorOpen;
            }
            
            if (!isCompatible) {
                count++;
            }
        }
        return count;
    }
    
    public boolean isGoalComplete() {
        return tablePainted && chairPainted && doorOpen;
    }
    
    private void checkAndRecordCompletion() {
        if (isGoalComplete()) {
            String line = "#".repeat(80);
            String msg = String.format("\n%s\nRUN #%d COMPLETED | Utility: %.4f | Steps: %d | Perf: %.4f\n%s\n",
                line, experimentRunner.getCurrentRun(), totalReward, steps, getPerformance(), line);
            
            logger.info(msg);
            experimentRunner.logEvent(msg);
            
            experimentRunner.recordRun(totalReward, steps, totalActions, getPerformance(),
                                      stepsToCompleteTable, actionsToCompleteTable,
                                      stepsToCompleteChair, actionsToCompleteChair,
                                      stepsToCompleteDoor, actionsToCompleteDoor);
            
            if (!experimentRunner.isComplete()) {
                reset();
            }
        }
    }
    
    public void reset() {
        agentX = 1; agentY = 1;
        inventory.clear();
        tablePainted = false; chairPainted = false; doorOpen = false;
        totalReward = 0; steps = 0; totalActions = 0;
        movementStepsSinceLastChange = 0; totalEnvironmentChanges = 0;
        stepsToCompleteTable = -1; stepsToCompleteChair = -1; stepsToCompleteDoor = -1;
        actionsToCompleteTable = -1; actionsToCompleteChair = -1; actionsToCompleteDoor = -1;
        
        randomizeGoalPositions(true);
        updatePercepts();
    }
    
    private void updatePercepts() {
        clearPercepts();
        addPercept(Literal.parseLiteral("position(" + agentX + "," + agentY + ")"));
        addPercept(Literal.parseLiteral("carrying(" + inventory.size() + ")"));
        for (String item : inventory) addPercept(Literal.parseLiteral("carrying_item(" + item + ")"));
        if (tablePainted) addPercept(Literal.parseLiteral("colored(table)"));
        if (chairPainted) addPercept(Literal.parseLiteral("colored(chair)"));
        if (doorOpen) addPercept(Literal.parseLiteral("open(door)"));
        
        for (Map.Entry<String, int[]> entry : itemPositions.entrySet()) {
            int[] pos = entry.getValue();
            addPercept(Literal.parseLiteral("item_at(" + entry.getKey() + "," + pos[0] + "," + pos[1] + ")"));
        }
        
        addPercept(Literal.parseLiteral("table_at(" + tablePosition[0] + "," + tablePosition[1] + ")"));
        addPercept(Literal.parseLiteral("chair_at(" + chairPosition[0] + "," + chairPosition[1] + ")"));
        addPercept(Literal.parseLiteral("door_at(" + doorPosition[0] + "," + doorPosition[1] + ")"));
        addPercept(Literal.parseLiteral("total_reward(" + totalReward + ")"));
        addPercept(Literal.parseLiteral("steps(" + steps + ")"));
        addPercept(Literal.parseLiteral("max_carry(3)"));
        addPercept(Literal.parseLiteral("goal_complete(" + isGoalComplete() + ")"));
        addPercept(Literal.parseLiteral("dynamic_mode(" + dynamicMode + ")"));
    }
    
    public double getPerformance() {
        return steps > 0 ? totalReward / steps : 0;
    }
}