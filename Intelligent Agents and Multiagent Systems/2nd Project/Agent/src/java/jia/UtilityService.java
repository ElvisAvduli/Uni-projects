package jia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import util.PathFindingService;

//Utility calculation and task selection service for multi-agent coordination
public class UtilityService extends DefaultInternalAction {
    
    private static final double TABLE_REWARD = 1.0;
    private static final double CHAIR_REWARD = 1.0;
    private static final double DOOR_REWARD = 0.8;
    

    // Main execution - handles multiple utility-related operations
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // Determine which method to call based on arity
        if (args.length == 1) {
            // get_available_tasks(Tasks)
            return getAvailableTasks(ts, un, args);
        } else if (args.length == 5) {
            // select_best_task(X, Y, Tasks, BestTask, BestUtility)
            return selectBestTask(ts, un, args);
        } else if (args.length == 3) {
            // calculate_utility(X, Y, Task) -> returns utility value
            return calculateSingleTaskUtility(ts, un, args);
        }
        
        throw new IllegalArgumentException("UtilityService called with invalid arguments");
    }
    
    // Get available tasks that are not yet completed
    private Object getAvailableTasks(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        List<Term> availableTasks = new ArrayList<>();
        
        // Check table
        if (!hasBelief(ts, "colored", "table")) {
            availableTasks.add(new Atom("table"));
        }
        
        // Check chair
        if (!hasBelief(ts, "colored", "chair")) {
            availableTasks.add(new Atom("chair"));
        }
        
        // Check door
        if (!hasBelief(ts, "open", "door")) {
            availableTasks.add(new Atom("door"));
        }
        
        // Convert to ListTerm
        ListTerm taskList = new ListTermImpl();
        ListTerm tail = taskList;
        for (Term task : availableTasks) {
            tail = tail.append(task);
        }
        
        return un.unifies(args[0], taskList);
    }
    

    // Select best task based on utility calculation
    private Object selectBestTask(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        int currentX = (int) ((NumberTerm) args[0]).solve();
        int currentY = (int) ((NumberTerm) args[1]).solve();
        ListTerm tasks = (ListTerm) args[2];
        
        String bestTask = "none";
        double bestUtility = -1.0;
        
        // Get inventory state
        boolean hasBrush = hasBelief(ts, "carrying_item", "brush");
        boolean hasColor = hasBelief(ts, "carrying_item", "color");
        boolean hasKey = hasBelief(ts, "carrying_item", "key");
        boolean hasCode = hasBelief(ts, "carrying_item", "code");
        
        // Evaluate each task
        for (Term taskTerm : tasks) {
            String task = taskTerm.toString().replace("\"", "");
            double utility = 0.0;
            
            if (task.equals("table")) {
                int[] tablePos = getPosition(ts, "table_at");
                if (tablePos != null) {
                    utility = calculateTaskUtility(currentX, currentY, tablePos[0], tablePos[1],
                                                   TABLE_REWARD, hasBrush, hasColor);
                }
            } else if (task.equals("chair")) {
                int[] chairPos = getPosition(ts, "chair_at");
                if (chairPos != null) {
                    utility = calculateTaskUtility(currentX, currentY, chairPos[0], chairPos[1],
                                                   CHAIR_REWARD, hasBrush, hasColor);
                }
            } else if (task.equals("door")) {
                int[] doorPos = getPosition(ts, "door_at");
                if (doorPos != null) {
                    utility = calculateTaskUtility(currentX, currentY, doorPos[0], doorPos[1],
                                                   DOOR_REWARD, hasKey, hasCode);
                }
            }
            
            System.out.println("[UTILITY] Task: " + task + " = " + String.format("%.4f", utility));
            
            if (utility > bestUtility) {
                bestUtility = utility;
                bestTask = task;
            }
        }
        
        return un.unifies(args[3], new Atom(bestTask)) && 
               un.unifies(args[4], new NumberTermImpl(bestUtility));
    }
    

    // Calculate utility for a specific task
    private Object calculateSingleTaskUtility(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        int currentX = (int) ((NumberTerm) args[0]).solve();
        int currentY = (int) ((NumberTerm) args[1]).solve();
        String task = args[2].toString().replace("\"", "");
        
        boolean hasBrush = hasBelief(ts, "carrying_item", "brush");
        boolean hasColor = hasBelief(ts, "carrying_item", "color");
        boolean hasKey = hasBelief(ts, "carrying_item", "key");
        boolean hasCode = hasBelief(ts, "carrying_item", "code");
        
        double utility = 0.0;
        
        if (task.equals("table")) {
            int[] pos = getPosition(ts, "table_at");
            if (pos != null) {
                utility = calculateTaskUtility(currentX, currentY, pos[0], pos[1],
                                              TABLE_REWARD, hasBrush, hasColor);
            }
        } else if (task.equals("chair")) {
            int[] pos = getPosition(ts, "chair_at");
            if (pos != null) {
                utility = calculateTaskUtility(currentX, currentY, pos[0], pos[1],
                                              CHAIR_REWARD, hasBrush, hasColor);
            }
        } else if (task.equals("door")) {
            int[] pos = getPosition(ts, "door_at");
            if (pos != null) {
                utility = calculateTaskUtility(currentX, currentY, pos[0], pos[1],
                                              DOOR_REWARD, hasKey, hasCode);
            }
        }
        
        return new NumberTermImpl(utility);
    }
    
    
    // Calculate task utility = reward / (distance_to_goal + collection_cost)
    private double calculateTaskUtility(int currentX, int currentY, int goalX, int goalY,
                                       double reward, boolean hasItem1, boolean hasItem2) {
        int goalDistance = PathFindingService.getPathLength(currentX, currentY, goalX, goalY);
        
        if (goalDistance == 0) goalDistance = 1; // Avoid division by zero
        
        // If we have both required items, just use goal distance
        if (hasItem1 && hasItem2) {
            return reward / goalDistance;
        }
        
        // Otherwise, estimate collection cost
        int collectionCost = 0;
        if (!hasItem1) collectionCost += 3; // Average distance estimate
        if (!hasItem2) collectionCost += 3; // Average distance estimate
        
        int totalDistance = goalDistance + collectionCost;
        return totalDistance > 0 ? reward / totalDistance : 0.0;
    }
    
    // Check if agent has a specific belief
    private boolean hasBelief(TransitionSystem ts, String functor, String term) {
        Iterator<Literal> it = ts.getAg().getBB().iterator();
        while (it.hasNext()) {
            Literal belief = it.next();
            if (belief.getFunctor().equals(functor)) {
                if (term == null) return true;
                if (belief.getArity() > 0) {
                    String termStr = belief.getTerm(0).toString();
                    if (termStr.equals(term) || termStr.equals("\"" + term + "\"")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // Get position from belief 
    private int[] getPosition(TransitionSystem ts, String functor) {
        Iterator<Literal> it = ts.getAg().getBB().iterator();
        while (it.hasNext()) {
            Literal belief = it.next();
            if (belief.getFunctor().equals(functor) && belief.getArity() >= 2) {
                try {
                    int x = (int) ((NumberTerm) belief.getTerm(0)).solve();
                    int y = (int) ((NumberTerm) belief.getTerm(1)).solve();
                    return new int[]{x, y};
                } catch (Exception e) {
                    // Continue searching
                }
            }
        }
        return null;
    }
}