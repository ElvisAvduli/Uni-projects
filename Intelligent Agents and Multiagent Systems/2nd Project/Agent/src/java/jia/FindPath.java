package jia;

import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import util.PathFindingService;

// A* Pathfinding internal action for agent navigation
public class FindPath extends DefaultInternalAction {
    

    // Main execution method - routes to appropriate sub-method
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (args.length == 5) {
            // get_path(X, Y, TargetX, TargetY, Path)
            return getPath(ts, un, args);
        } else if (args.length == 5 && args[2].toString().contains("next")) {
            // next_position(X, Y, Direction, NewX, NewY)
            return getNextPosition(ts, un, args);
        }
        
        throw new IllegalArgumentException("FindPath requires 5 arguments");
    }
    
    // Get A* path from start to goal
    private Object getPath(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        int startX = (int) ((NumberTerm) args[0]).solve();
        int startY = (int) ((NumberTerm) args[1]).solve();
        int goalX = (int) ((NumberTerm) args[2]).solve();
        int goalY = (int) ((NumberTerm) args[3]).solve();
        
        // Get path using basic A* (collision avoidance handled in agent logic)
        List<String> path = PathFindingService.findPath(startX, startY, goalX, goalY);
        
        // Convert to Prolog list
        ListTerm pathList = new ListTermImpl();
        ListTerm tail = pathList;
        
        for (String direction : path) {
            tail = tail.append(new Atom(direction));
        }
        
        return un.unifies(args[4], pathList);
    }
    
    // Calculate next position given current position and direction
    private Object getNextPosition(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        int x = (int) ((NumberTerm) args[0]).solve();
        int y = (int) ((NumberTerm) args[1]).solve();
        String direction = args[2].toString().replace("\"", "");
        
        int newX = x, newY = y;
        
        switch(direction) {
            case "right": newX++; break;
            case "left": newX--; break;
            case "up": newY++; break;
            case "down": newY--; break;
        }
        
        return un.unifies(args[3], new NumberTermImpl(newX)) &&
               un.unifies(args[4], new NumberTermImpl(newY));
    }
}