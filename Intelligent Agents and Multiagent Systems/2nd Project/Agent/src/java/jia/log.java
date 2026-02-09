package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import util.MetricsLogger;

// Logging internal action for debugging and metrics
public class log extends DefaultInternalAction {
    
    // Log a message with agent context
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("log requires at least 1 argument");
        }
        
        String agentName = ts.getAgArch().getAgName();
        StringBuilder message = new StringBuilder();
        
        for (int i = 0; i < args.length; i++) {
            if (i > 0) message.append(" ");
            message.append(args[i].toString());
        }
        
        String logMessage = "[" + agentName + "] " + message.toString();
        System.out.println(logMessage);
        
        // Log to metrics logger if available
        MetricsLogger.logEvent(logMessage);
        
        return true;
    }
}