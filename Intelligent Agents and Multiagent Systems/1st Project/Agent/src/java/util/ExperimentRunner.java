package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExperimentRunner {
    
    private static class RunStatistics {
        double utility, performance;
        int movementSteps, totalActions;
        int stepsToTable, actionsToTable;
        int stepsToChair, actionsToChair;
        int stepsToDoor, actionsToDoor;
        
        public RunStatistics(double utility, double performance, int movementSteps, int totalActions,
                           int stepsToTable, int actionsToTable,
                           int stepsToChair, int actionsToChair,
                           int stepsToDoor, int actionsToDoor) {
            this.utility = utility;
            this.performance = performance;
            this.movementSteps = movementSteps;
            this.totalActions = totalActions;
            this.stepsToTable = stepsToTable;
            this.actionsToTable = actionsToTable;
            this.stepsToChair = stepsToChair;
            this.actionsToChair = actionsToChair;
            this.stepsToDoor = stepsToDoor;
            this.actionsToDoor = actionsToDoor;
        }
    }
    
    private List<RunStatistics> allRuns = new ArrayList<>();
    private int maxRuns;
    private long startTime;
    private String mode;
    private PrintWriter detailedLogWriter;
    private PrintWriter simpleLogWriter;
    private String detailedLogFilename;
    private String simpleLogFilename;
    
    public ExperimentRunner(int maxRuns, String mode) {
        this.maxRuns = maxRuns;
        this.mode = mode;
        this.startTime = System.currentTimeMillis();
        
        detailedLogFilename = "detailed_log_" + mode + "_" + System.currentTimeMillis() + ".txt";
        simpleLogFilename = "simple_log_" + mode + "_" + System.currentTimeMillis() + ".csv";
        
        try {
            detailedLogWriter = new PrintWriter(new FileWriter(detailedLogFilename), true);
            detailedLogWriter.println("EXPERIMENT START: " + new java.util.Date());
            detailedLogWriter.println("Mode: " + mode + " | Max Runs: " + maxRuns);
            detailedLogWriter.println("================================================================================");
            
            simpleLogWriter = new PrintWriter(new FileWriter(simpleLogFilename), true);
            // SIMPLIFIED CSV HEADER
            simpleLogWriter.println("Run,Utility,Steps,Actions,Performance");
        } catch (IOException e) {
            System.err.println("Error creating logs: " + e.getMessage());
        }
    }
    
    public void recordRun(double utility, int movementSteps, int totalActions, double performance,
                         int stepsToTable, int actionsToTable,
                         int stepsToChair, int actionsToChair,
                         int stepsToDoor, int actionsToDoor) {
        allRuns.add(new RunStatistics(utility, performance, movementSteps, totalActions,
                                     stepsToTable, actionsToTable,
                                     stepsToChair, actionsToChair,
                                     stepsToDoor, actionsToDoor));
        
        // CSV OUTPUT (Run, Utility, Steps, Actions, Performance)
        if (simpleLogWriter != null) {
            simpleLogWriter.printf(Locale.US, "%d,%.4f,%d,%d,%.4f%n",
                allRuns.size(), utility, movementSteps, totalActions, performance);
        }

        System.out.println("Run " + allRuns.size() + " Completed. Utility: " + String.format(Locale.US, "%.4f", utility));
        
        if (allRuns.size() % 10 == 0) printProgressStatistics();
        if (allRuns.size() >= maxRuns) finishExperiment();
    }
    
    public boolean isComplete() { return allRuns.size() >= maxRuns; }
    public int getCurrentRun() { return allRuns.size() + 1; }
    
    public void logEvent(String message) {
        if (detailedLogWriter != null) {
            detailedLogWriter.println(message);
        }
    }
    
    private void printProgressStatistics() {
        String msg = String.format(Locale.US, ">>> PROGRESS [%d/%d] | Avg Util: %.4f | Avg Steps: %.2f", 
            allRuns.size(), maxRuns, getAverage(allRuns, "utility"), getAverage(allRuns, "movementSteps"));
        System.out.println(msg);
        logEvent("--------------------------------------------------------------------------------");
        logEvent(msg);
        logEvent("--------------------------------------------------------------------------------");
    }
    
    private void finishExperiment() {
        long duration = System.currentTimeMillis() - startTime;
        saveSummaryReport(duration);
        
        if (detailedLogWriter != null) {
            detailedLogWriter.println("\nEXP END. Duration: " + (duration/1000.0) + "s");
            detailedLogWriter.close();
        }
        if (simpleLogWriter != null) simpleLogWriter.close();
        
        System.out.println("\nExperiment Finished. Summary saved to files.");
    }
    
    private void saveSummaryReport(long duration) {
        String filename = "summary_report_" + mode + "_" + System.currentTimeMillis() + ".txt";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("EXPERIMENT SUMMARY REPORT\n");
            writer.write("=========================\n");
            writer.write(String.format(Locale.US, "Mode: %s | Runs: %d | Time: %.2fs\n\n", mode, maxRuns, duration/1000.0));
            
            writer.write("METRICS (AVG | MIN | MAX | STDDEV)\n");
            writer.write("----------------------------------\n");
            writeMetric(writer, "Utility", "utility");
            writeMetric(writer, "Steps", "movementSteps");
            writeMetric(writer, "Actions", "totalActions");
            writeMetric(writer, "Performance", "performance");
            
            System.out.println("Summary Report generated: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving summary: " + e.getMessage());
        }
    }
    
    private void writeMetric(FileWriter w, String label, String field) throws IOException {
        w.write(String.format(Locale.US, "%-12s: %.4f | %.4f | %.4f | %.4f\n", label,
            getAverage(allRuns, field), getMin(allRuns, field), getMax(allRuns, field), getStdDev(allRuns, field)));
    }
    
    // Helpers
    private double getAverage(List<RunStatistics> runs, String metric) {
        if (runs.isEmpty()) return 0.0;
        return runs.stream().mapToDouble(r -> getValue(r, metric)).average().orElse(0.0);
    }
    private double getMin(List<RunStatistics> runs, String metric) {
        return runs.stream().mapToDouble(r -> getValue(r, metric)).min().orElse(0.0);
    }
    private double getMax(List<RunStatistics> runs, String metric) {
        return runs.stream().mapToDouble(r -> getValue(r, metric)).max().orElse(0.0);
    }
    private double getStdDev(List<RunStatistics> runs, String metric) {
        if (runs.isEmpty()) return 0.0;
        double mean = getAverage(runs, metric);
        double sumSq = runs.stream().mapToDouble(r -> Math.pow(getValue(r, metric) - mean, 2)).sum();
        return Math.sqrt(sumSq / runs.size());
    }
    
    private double getValue(RunStatistics run, String metric) {
        switch(metric) {
            case "utility": return run.utility;
            case "performance": return run.performance;
            case "movementSteps": return run.movementSteps;
            case "totalActions": return run.totalActions;
            case "stepsToTable": return run.stepsToTable;
            case "actionsToTable": return run.actionsToTable;
            case "stepsToChair": return run.stepsToChair;
            case "actionsToChair": return run.actionsToChair;
            case "stepsToDoor": return run.stepsToDoor;
            case "actionsToDoor": return run.actionsToDoor;
            default: return 0.0;
        }
    }
}