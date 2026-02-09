package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MetricsLogger {
    
    private static PrintWriter eventLogWriter;
    private static Map<String, Integer> eventCounts = new HashMap<>();
    private static List<String> recentEvents = new ArrayList<>();
    private static final int MAX_RECENT_EVENTS = 100;
    
    static {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            eventLogWriter = new PrintWriter(new FileWriter("events_" + timestamp + ".log"), true);
            eventLogWriter.println("EVENT LOG START: " + new Date());
            eventLogWriter.println("=".repeat(80));
        } catch (IOException e) {
            System.err.println("Error creating event log: " + e.getMessage());
        }
    }
    
    public static synchronized void logEvent(String event) {
        if (eventLogWriter != null) {
            String timestampedEvent = String.format("[%tT] %s", System.currentTimeMillis(), event);
            eventLogWriter.println(timestampedEvent);
            
            // Track recent events
            if (recentEvents.size() >= MAX_RECENT_EVENTS) {
                recentEvents.remove(0);
            }
            recentEvents.add(timestampedEvent);
        }
    }
    
    public static synchronized void logEvent(String category, String event) {
        String fullEvent = "[" + category + "] " + event;
        logEvent(fullEvent);
        
        // Count events by category
        eventCounts.put(category, eventCounts.getOrDefault(category, 0) + 1);
    }
    
    public static synchronized void logMetric(String metricName, double value) {
        logEvent("METRIC", metricName + " = " + String.format(Locale.US, "%.4f", value));
    }
    
    public static synchronized void logAction(String agentName, String action, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        logEvent("ACTION", agentName + " - " + action + " - " + status);
    }
    
    public static synchronized void logNegotiation(String agentName, String task, double utility) {
        logEvent("NEGOTIATION", 
                String.format(Locale.US, "%s proposes task=%s utility=%.4f", 
                             agentName, task, utility));
    }
    
    public static synchronized List<String> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }
    
    public static synchronized Map<String, Integer> getEventCounts() {
        return new HashMap<>(eventCounts);
    }
    
    public static synchronized void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EVENT STATISTICS");
        System.out.println("=".repeat(80));
        
        for (Map.Entry<String, Integer> entry : eventCounts.entrySet()) {
            System.out.printf("%-20s: %d events%n", entry.getKey(), entry.getValue());
        }
        
        System.out.println("=".repeat(80));
    }
    
    
    // Close logger
    
    public static synchronized void close() {
        if (eventLogWriter != null) {
            eventLogWriter.println("\nEVENT LOG END: " + new Date());
            printStatistics();
            eventLogWriter.close();
        }
    }
}