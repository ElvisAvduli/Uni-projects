package jia;

import java.util.Iterator;
import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import util.AStarPathfinder;

/**
 * Î’Î•Î›Î¤Î™Î©ÎœÎ•ÎÎ— Î•ÎšÎ”ÎŸÎ£Î— Î¼Îµ collection cost estimation
 * Î¥Ï€Î¿Î»Î¿Î³Î¯Î¶ÎµÎ¹ utility = reward / total_distance
 * ÏŒÏ€Î¿Ï… total_distance = distance_to_goal + collection_cost
 */
public class calculate_goal_utilities extends DefaultInternalAction {
    
    private static final double TABLE_REWARD = 1.0;
    private static final double CHAIR_REWARD = 1.0;
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        
        if (args.length != 3) {
            throw new IllegalArgumentException("calculate_goal_utilities requires 3 arguments: CurrentX, CurrentY, BestGoal");
        }
        
        int currentX = (int) ((NumberTerm) args[0]).solve();
        int currentY = (int) ((NumberTerm) args[1]).solve();
        
        // Get agent beliefs
        boolean isTablePainted = hasBelief(ts, "colored", "table");
        boolean isChairPainted = hasBelief(ts, "colored", "chair");
        boolean hasBrush = hasBelief(ts, "carrying_item", "brush");
        boolean hasColor = hasBelief(ts, "carrying_item", "color");
        boolean canPaintNow = hasBrush && hasColor;
        
        String bestGoal = "none";
        double bestUtility = -1.0;
        
        // Î‘Î½ Î­Ï‡Î¿Ï…Î¼Îµ Î®Î´Î· Ï„Î± items, Ï…Ï€Î¿Î»Î¿Î³Î¯Î¶Î¿Ï…Î¼Îµ Î±Ï€Î»Î¬
        if (canPaintNow) {
            // Check table
            if (!isTablePainted) {
                int[] tablePos = getPosition(ts, "table_at");
                if (tablePos != null) {
                    double utility = calculateGoalUtility(currentX, currentY, 
                                                         tablePos[0], tablePos[1], 
                                                         TABLE_REWARD, 0);
                    
                    System.out.println("[UTILITY] Table utility: " + String.format("%.4f", utility) + 
                                     " (distance: " + getPathLength(currentX, currentY, tablePos[0], tablePos[1]) + ")");
                    
                    if (utility > bestUtility) {
                        bestUtility = utility;
                        bestGoal = "table";
                    }
                }
            }
            
            // Check chair
            if (!isChairPainted) {
                int[] chairPos = getPosition(ts, "chair_at");
                if (chairPos != null) {
                    double utility = calculateGoalUtility(currentX, currentY, 
                                                         chairPos[0], chairPos[1], 
                                                         CHAIR_REWARD, 0);
                    
                    System.out.println("[UTILITY] Chair utility: " + String.format("%.4f", utility) + 
                                     " (distance: " + getPathLength(currentX, currentY, chairPos[0], chairPos[1]) + ")");
                    
                    if (utility > bestUtility) {
                        bestUtility = utility;
                        bestGoal = "chair";
                    }
                }
            }
        } 
        // Î‘Î½ Î”Î•Î Î­Ï‡Î¿Ï…Î¼Îµ items, Ï…Ï€Î¿Î»Î¿Î³Î¯Î¶Î¿Ï…Î¼Îµ Î¼Îµ collection cost
        else {
            System.out.println("[UTILITY] Need to collect items first. Estimating total cost...");
            
            if (!isTablePainted) {
                int[] tablePos = getPosition(ts, "table_at");
                if (tablePos != null) {
                    int collectionCost = estimateCollectionCost(ts, currentX, currentY, hasBrush, hasColor);
                    double utility = calculateGoalUtility(currentX, currentY, 
                                                         tablePos[0], tablePos[1], 
                                                         TABLE_REWARD, collectionCost);
                    
                    int directDistance = getPathLength(currentX, currentY, tablePos[0], tablePos[1]);
                    System.out.println("[UTILITY] Table utility: " + String.format("%.4f", utility) + 
                                     " (direct: " + directDistance + ", collection: " + collectionCost + 
                                     ", total: " + (directDistance + collectionCost) + ")");
                    
                    if (utility > bestUtility) {
                        bestUtility = utility;
                        bestGoal = "table";
                    }
                }
            }
            
            if (!isChairPainted) {
                int[] chairPos = getPosition(ts, "chair_at");
                if (chairPos != null) {
                    int collectionCost = estimateCollectionCost(ts, currentX, currentY, hasBrush, hasColor);
                    double utility = calculateGoalUtility(currentX, currentY, 
                                                         chairPos[0], chairPos[1], 
                                                         CHAIR_REWARD, collectionCost);
                    
                    int directDistance = getPathLength(currentX, currentY, chairPos[0], chairPos[1]);
                    System.out.println("[UTILITY] Chair utility: " + String.format("%.4f", utility) + 
                                     " (direct: " + directDistance + ", collection: " + collectionCost + 
                                     ", total: " + (directDistance + collectionCost) + ")");
                    
                    if (utility > bestUtility) {
                        bestUtility = utility;
                        bestGoal = "chair";
                    }
                }
            }
        }
        
        System.out.println("[DECISION] Best goal: " + bestGoal + " with utility: " + String.format("%.4f", bestUtility));
        return un.unifies(args[2], new Atom(bestGoal));
    }
    
    /**
     * Î•ÎºÏ„Î¯Î¼Î·ÏƒÎ· ÎºÏŒÏƒÏ„Î¿Ï…Ï‚ Î³Î¹Î± ÏƒÏ…Î»Î»Î¿Î³Î® Î±Ï€Î±ÏÎ±Î¯Ï„Î·Ï„Ï‰Î½ items
     * Î¥Ï€Î¿Î»Î¿Î³Î¯Î¶ÎµÎ¹ Ï„Î¿ Ï€ÏÎ±Î³Î¼Î±Ï„Î¹ÎºÏŒ Î¼Î®ÎºÎ¿Ï‚ path Î³Î¹Î± ÎºÎ¬Î¸Îµ item Ï€Î¿Ï… Î»ÎµÎ¯Ï€ÎµÎ¹
     */
    private int estimateCollectionCost(TransitionSystem ts, int currentX, int currentY,
                                      boolean hasBrush, boolean hasColor) {
        int totalCost = 0;
        int fromX = currentX;
        int fromY = currentY;
        
        // Î‘Î½ Î´ÎµÎ½ Î­Ï‡Î¿Ï…Î¼Îµ brush, Ï€ÏÎ­Ï€ÎµÎ¹ Î½Î± Ï€Î¬Î¼Îµ Î½Î± Ï„Î¿Î½ Ï€Î¬ÏÎ¿Ï…Î¼Îµ
        if (!hasBrush) {
            int[] brushPos = getItemPosition(ts, "brush");
            if (brushPos != null) {
                int pathLength = getPathLength(fromX, fromY, brushPos[0], brushPos[1]);
                totalCost += pathLength;
                fromX = brushPos[0];
                fromY = brushPos[1];
            }
        }
        
        // Î‘Î½ Î´ÎµÎ½ Î­Ï‡Î¿Ï…Î¼Îµ color, Ï€ÏÎ­Ï€ÎµÎ¹ Î½Î± Ï€Î¬Î¼Îµ Î½Î± Ï„Î¿Î½ Ï€Î¬ÏÎ¿Ï…Î¼Îµ
        if (!hasColor) {
            int[] colorPos = getItemPosition(ts, "color");
            if (colorPos != null) {
                int pathLength = getPathLength(fromX, fromY, colorPos[0], colorPos[1]);
                totalCost += pathLength;
            }
        }
        
        return totalCost;
    }
    
    /**
     * Î¥Ï€Î¿Î»Î¿Î³Î¹ÏƒÎ¼ÏŒÏ‚ utility = reward / (goal_distance + collection_cost)
     */
    private double calculateGoalUtility(int currentX, int currentY, 
                                       int goalX, int goalY, 
                                       double reward, int collectionCost) {
        int goalDistance = getPathLength(currentX, currentY, goalX, goalY);
        int totalDistance = goalDistance + collectionCost;
        
        return totalDistance > 0 ? reward / totalDistance : reward;
    }
    
    /**
     * Î’ÏÎ¯ÏƒÎºÎµÎ¹ Ï„Î¿ Ï€ÏÎ±Î³Î¼Î±Ï„Î¹ÎºÏŒ Î¼Î®ÎºÎ¿Ï‚ path Ï‡ÏÎ·ÏƒÎ¹Î¼Î¿Ï€Î¿Î¹ÏŽÎ½Ï„Î±Ï‚ A*
     */
    private int getPathLength(int x1, int y1, int x2, int y2) {
        List<String> path = AStarPathfinder.findPath(x1, y1, x2, y2);
        
        if (path != null && !path.isEmpty()) {
            return path.size();
        }
        
        // Fallback: Manhattan distance
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * ÎˆÎ»ÎµÎ³Ï‡Î¿Ï‚ Î±Î½ Ï…Ï€Î¬ÏÏ‡ÎµÎ¹ belief Î¼Îµ ÏƒÏ…Î³ÎºÎµÎºÏÎ¹Î¼Î­Î½Î¿ functor ÎºÎ±Î¹ term
     */
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
    
    /**
     * Î’ÏÎ¯ÏƒÎºÎµÎ¹ Î¸Î­ÏƒÎ· Î±Ï€ÏŒ belief Ï„ÏÏ€Î¿Ï… table_at(X,Y) Î® chair_at(X,Y)
     */
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
    
    /**
     * Î’ÏÎ¯ÏƒÎºÎµÎ¹ Î¸Î­ÏƒÎ· item Î±Ï€ÏŒ belief Ï„ÏÏ€Î¿Ï… item_at(ItemName, X, Y)
     */
    private int[] getItemPosition(TransitionSystem ts, String itemName) {
        Iterator<Literal> it = ts.getAg().getBB().iterator();
        while (it.hasNext()) {
            Literal belief = it.next();
            if (belief.getFunctor().equals("item_at") && belief.getArity() >= 3) {
                try {
                    String item = belief.getTerm(0).toString().replace("\"", "");
                    if (item.equals(itemName)) {
                        int x = (int) ((NumberTerm) belief.getTerm(1)).solve();
                        int y = (int) ((NumberTerm) belief.getTerm(2)).solve();
                        return new int[]{x, y};
                    }
                } catch (Exception e) {
                    // Continue searching
                }
            }
        }
        return null;
    }
}