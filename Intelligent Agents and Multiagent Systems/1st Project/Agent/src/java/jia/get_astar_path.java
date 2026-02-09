package jia;

import java.util.List;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import util.AStarPathfinder;

/**
 * Internal action to get A* path from current position to target
 * Usage in AgentSpeak: jia.get_astar_path(StartX, StartY, GoalX, GoalY, Path)
 */
public class get_astar_path extends DefaultInternalAction {
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        
        // Parse arguments
        if (args.length != 5) {
            throw new IllegalArgumentException("get_astar_path requires 5 arguments: StartX, StartY, GoalX, GoalY, Path");
        }
        
        int startX = (int) ((NumberTerm) args[0]).solve();
        int startY = (int) ((NumberTerm) args[1]).solve();
        int goalX = (int) ((NumberTerm) args[2]).solve();
        int goalY = (int) ((NumberTerm) args[3]).solve();
        
        // Call A* pathfinder directly (no reflection needed since we import it)
        List<String> path = AStarPathfinder.findPath(startX, startY, goalX, goalY);
        
        // Convert to Prolog list
        ListTerm pathList = new ListTermImpl();
        ListTerm tail = pathList;
        
        for (String direction : path) {
            tail = tail.append(new Atom(direction));
        }
        
        // Unify with the last argument
        return un.unifies(args[4], pathList);
    }
}