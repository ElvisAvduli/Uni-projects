package jia;

import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import util.PathFindingService;

// Detects when agents are blocking each other's paths
public class DeadlockDetector extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        
        // Expects: agent1X, agent1Y, agent1GoalX, agent1GoalY, 
        //          agent2X, agent2Y, agent2GoalX, agent2GoalY, Result
        if (args.length != 9) {
            throw new IllegalArgumentException(
                "DeadlockDetector requires 9 arguments: " +
                "A1X, A1Y, A1GX, A1GY, A2X, A2Y, A2GX, A2GY, Result");
        }
        
        int a1X = (int) ((NumberTerm) args[0]).solve();
        int a1Y = (int) ((NumberTerm) args[1]).solve();
        int a1GoalX = (int) ((NumberTerm) args[2]).solve();
        int a1GoalY = (int) ((NumberTerm) args[3]).solve();
        
        int a2X = (int) ((NumberTerm) args[4]).solve();
        int a2Y = (int) ((NumberTerm) args[5]).solve();
        int a2GoalX = (int) ((NumberTerm) args[6]).solve();
        int a2GoalY = (int) ((NumberTerm) args[7]).solve();
        
        boolean deadlock = detectDeadlock(
            a1X, a1Y, a1GoalX, a1GoalY,
            a2X, a2Y, a2GoalX, a2GoalY
        );
        
        // Unify with result parameter
        return un.unifies(args[8], new Atom(String.valueOf(deadlock)));
    }

    // Detect if agents are in a deadlock situation
    private boolean detectDeadlock(
        int a1X, int a1Y, int a1GoalX, int a1GoalY,
        int a2X, int a2Y, int a2GoalX, int a2GoalY
    ) {
        // Check 1: Are agents adjacent?
        if (!areAdjacent(a1X, a1Y, a2X, a2Y)) {
            return false;
        }
        
        // Check 2: Is agent2 blocking agent1's path?
        boolean a2BlocksA1 = isBlockingPath(a2X, a2Y, a1X, a1Y, a1GoalX, a1GoalY);
        
        // Check 3: Is agent1 blocking agent2's path?
        boolean a1BlocksA2 = isBlockingPath(a1X, a1Y, a2X, a2Y, a2GoalX, a2GoalY);
        
        // Deadlock if both are blocking each other OR
        // if one is blocking and the other is trying to reach same area
        if (a2BlocksA1 && a1BlocksA2) {
            return true;
        }
        
        // Check 4: Head-on collision (moving towards each other)
        if (areHeadingTowardsEachOther(a1X, a1Y, a1GoalX, a1GoalY, 
                                       a2X, a2Y, a2GoalX, a2GoalY)) {
            return true;
        }
        return false;
    }
    
    // Check if two positions are adjacent (manhattan distance = 1)
    private boolean areAdjacent(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
    
    
    // Check if blockerPos is on the optimal path from startPos to goalPos
    private boolean isBlockingPath(int blockerX, int blockerY,
                                   int startX, int startY,
                                   int goalX, int goalY) {
        // If already at goal, no blocking issue
        if (startX == goalX && startY == goalY) {
            return false;
        }
        
        // Get optimal path without considering the blocker
        List<String> path = PathFindingService.findPath(startX, startY, goalX, goalY);
        
        if (path == null || path.isEmpty()) {
            return false; // No path exists anyway
        }
        
        // Simulate path and check if blocker is on it
        int currentX = startX;
        int currentY = startY;
        
        for (String direction : path) {
            // Move in the specified direction
            switch (direction) {
                case "up": currentY++; break;
                case "down": currentY--; break;
                case "left": currentX--; break;
                case "right": currentX++; break;
            }
            
            // Check if blocker is at this position
            if (currentX == blockerX && currentY == blockerY) {
                return true;
            }
            
            // Early exit if we reached goal
            if (currentX == goalX && currentY == goalY) {
                break;
            }
        }
        
        return false;
    }
    
    // Detect head-on collision: agents moving towards each other
    private boolean areHeadingTowardsEachOther(
        int a1X, int a1Y, int a1GoalX, int a1GoalY,
        int a2X, int a2Y, int a2GoalX, int a2GoalY
    ) {
        // If agents are adjacent
        if (!areAdjacent(a1X, a1Y, a2X, a2Y)) {
            return false;
        }
        
        // Calculate next intended positions for both agents
        int a1NextX = a1X;
        int a1NextY = a1Y;
        int a2NextX = a2X;
        int a2NextY = a2Y;
        
        // Agent 1's next move
        if (a1GoalX > a1X) a1NextX++;
        else if (a1GoalX < a1X) a1NextX--;
        else if (a1GoalY > a1Y) a1NextY++;
        else if (a1GoalY < a1Y) a1NextY--;
        
        // Agent 2's next move
        if (a2GoalX > a2X) a2NextX++;
        else if (a2GoalX < a2X) a2NextX--;
        else if (a2GoalY > a2Y) a2NextY++;
        else if (a2GoalY < a2Y) a2NextY--;
        
        boolean a1TowardsA2 = (a1NextX == a2X && a1NextY == a2Y);
        boolean a2TowardsA1 = (a2NextX == a1X && a2NextY == a1Y);
        
        return a1TowardsA2 && a2TowardsA1;
    }
}