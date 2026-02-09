package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExperimentRunner {
    
    private static class RunStatistics {
        double totalUtility;
        int totalSteps, totalActions;
        double agent1Utility, agent2Utility;
        int agent1Steps, agent2Steps;
        int agent1Actions, agent2Actions;
        boolean wasDeadlock;
        
        public RunStatistics(double totalUtility, int totalSteps, int totalActions,
                           double agent1Utility, int agent1Steps, int agent1Actions,
                           double agent2Utility, int agent2Steps, int agent2Actions,
                           boolean wasDeadlock) {
            this.totalUtility = totalUtility;
            this.totalSteps = totalSteps;
            this.totalActions = totalActions;
            this.agent1Utility = agent1Utility;
            this.agent1Steps = agent1Steps;
            this.agent1Actions = agent1Actions;
            this.agent2Utility = agent2Utility;
            this.agent2Steps = agent2Steps;
            this.agent2Actions = agent2Actions;
            this.wasDeadlock = wasDeadlock;
        }
    }
    
    private List<RunStatistics> allRuns = new ArrayList<>();
    private int successfulRuns = 0;
    private int deadlockRuns = 0;
    private int maxRuns;
    private long startTime;
    private String mode;
    private PrintWriter detailedLogWriter;
    private PrintWriter csvWriter;
    
    public ExperimentRunner(int maxRuns, String mode) {
        this.maxRuns = maxRuns;
        this.mode = mode;
        this.startTime = System.currentTimeMillis();
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        try {
            detailedLogWriter = new PrintWriter(new FileWriter("detailed_log_" + mode + "_" + timestamp + ".txt"), true);
            detailedLogWriter.println("MULTI-AGENT EXPERIMENT START: " + new Date());
            detailedLogWriter.println("Mode: " + mode + " | Max Runs: " + maxRuns);
            detailedLogWriter.println("=".repeat(80));
            
            csvWriter = new PrintWriter(new FileWriter("results_" + mode + "_" + timestamp + ".csv"), true);
            csvWriter.println("Run,TotalUtility,TotalSteps,TotalActions,Agent1Utility,Agent1Steps,Agent1Actions,Agent2Utility,Agent2Steps,Agent2Actions,WasDeadlock");
        } catch (IOException e) {
            System.err.println("Error creating logs: " + e.getMessage());
        }
    }
    
    public void recordRun(double totalUtility, int totalSteps, int totalActions,
                         double agent1Utility, int agent1Steps, int agent1Actions,
                         double agent2Utility, int agent2Steps, int agent2Actions,
                         boolean wasDeadlock) {
        
        allRuns.add(new RunStatistics(totalUtility, totalSteps, totalActions,
                                     agent1Utility, agent1Steps, agent1Actions,
                                     agent2Utility, agent2Steps, agent2Actions,
                                     wasDeadlock));
        
        if (wasDeadlock) {
            deadlockRuns++;
        } else {
            successfulRuns++;
        }
        
        // CSV output
        if (csvWriter != null) {
            csvWriter.printf(Locale.US, "%d,%.4f,%d,%d,%.4f,%d,%d,%.4f,%d,%d,%s%n",
                allRuns.size(), totalUtility, totalSteps, totalActions,
                agent1Utility, agent1Steps, agent1Actions,
                agent2Utility, agent2Steps, agent2Actions,
                wasDeadlock ? "DEADLOCK" : "SUCCESS");
        }
        
        String statusMsg = wasDeadlock ? "[DEADLOCK]" : "[SUCCESS]";
        System.out.println(String.format(Locale.US, 
            "Run %d Completed %s | Total Utility: %.4f | Total Steps: %d | Total Actions: %d",
            allRuns.size(), statusMsg, totalUtility, totalSteps, totalActions));
        
        if (allRuns.size() % 10 == 0 || allRuns.size() >= maxRuns) {
            printProgressStatistics();
        }
        
        // Check total runs and finish immediately
        if (allRuns.size() >= maxRuns) {
            finishExperiment();
            // Give time for cleanup
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Force system exit to stop any remaining runs
            System.exit(0);
        }
    }
    
    public boolean isComplete() {
        // Check total runs
        return allRuns.size() >= maxRuns;
    }
    
    public int getCurrentRun() {
        return allRuns.size() + 1;
    }
    
    public void logEvent(String message) {
        if (detailedLogWriter != null) {
            detailedLogWriter.println(message);
        }
    }
    
    private void printProgressStatistics() {
        double avgUtility = getAverageSuccessful("totalUtility");
        double avgSteps = getAverageSuccessful("totalSteps");
        double avgActions = getAverageSuccessful("totalActions");
        
        double successRate = (successfulRuns * 100.0) / allRuns.size();
        
        String msg = String.format(Locale.US, 
            ">>> PROGRESS [%d/%d] | Success Rate: %.2f%% | Avg Utility: %.4f | Avg Steps: %.2f | Avg Actions: %.2f",
            allRuns.size(), maxRuns, successRate, avgUtility, avgSteps, avgActions);
        
        System.out.println(msg);
        logEvent("-".repeat(80));
        logEvent(msg);
        logEvent("-".repeat(80));
    }
    
    private void finishExperiment() {
        long duration = System.currentTimeMillis() - startTime;
        saveSummaryReport(duration);
        
        if (detailedLogWriter != null) {
            detailedLogWriter.println("\nEXPERIMENT END. Duration: " + (duration/1000.0) + "s");
            detailedLogWriter.close();
        }
        if (csvWriter != null) {
            csvWriter.close();
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EXPERIMENT FINISHED");
        System.out.println("=".repeat(80));
        printFinalStatistics();
    }
    
    private void saveSummaryReport(long duration) {
        String filename = "summary_" + mode + "_" + System.currentTimeMillis() + ".txt";
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("MULTI-AGENT EXPERIMENT SUMMARY\n");
            writer.write("=".repeat(50) + "\n");
            writer.write(String.format(Locale.US, "Mode: %s | Total Runs: %d | Duration: %.2fs\n\n", 
                                      mode, allRuns.size(), duration/1000.0));
            
            writer.write(String.format("Successful Runs: %d\n", successfulRuns));
            writer.write(String.format("Deadlock Runs:   %d\n", deadlockRuns));
            writer.write(String.format("Success Rate:    %.2f%%\n\n", 
                (successfulRuns * 100.0) / allRuns.size()));
            
            writer.write("OVERALL METRICS (ALL RUNS)\n");
            writer.write("-".repeat(50) + "\n");
            writeMetric(writer, "Total Utility", "totalUtility", false);
            writeMetric(writer, "Total Steps", "totalSteps", false);
            writeMetric(writer, "Total Actions", "totalActions", false);
            
            writer.write("\nSUCCESSFUL RUNS ONLY\n");
            writer.write("-".repeat(50) + "\n");
            writeMetric(writer, "Total Utility", "totalUtility", true);
            writeMetric(writer, "Total Steps", "totalSteps", true);
            writeMetric(writer, "Total Actions", "totalActions", true);
            
            writer.write("\nAGENT 1 METRICS (SUCCESSFUL RUNS)\n");
            writer.write("-".repeat(50) + "\n");
            writeMetric(writer, "Agent1 Utility", "agent1Utility", true);
            writeMetric(writer, "Agent1 Steps", "agent1Steps", true);
            writeMetric(writer, "Agent1 Actions", "agent1Actions", true);
            
            writer.write("\nAGENT 2 METRICS (SUCCESSFUL RUNS)\n");
            writer.write("-".repeat(50) + "\n");
            writeMetric(writer, "Agent2 Utility", "agent2Utility", true);
            writeMetric(writer, "Agent2 Steps", "agent2Steps", true);
            writeMetric(writer, "Agent2 Actions", "agent2Actions", true);
            
            System.out.println("Summary saved to: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving summary: " + e.getMessage());
        }
    }
    
    private void writeMetric(FileWriter w, String label, String field, boolean successfulOnly) throws IOException {
        w.write(String.format(Locale.US, "%-20s: Avg=%.4f | Min=%.4f | Max=%.4f | StdDev=%.4f\n",
            label, 
            successfulOnly ? getAverageSuccessful(field) : getAverage(field),
            successfulOnly ? getMinSuccessful(field) : getMin(field),
            successfulOnly ? getMaxSuccessful(field) : getMax(field),
            successfulOnly ? getStdDevSuccessful(field) : getStdDev(field)));
    }
    
    private void printFinalStatistics() {
        System.out.println("FINAL STATISTICS:");
        System.out.println("-".repeat(80));
        System.out.printf("Total Runs:         %d%n", allRuns.size());
        System.out.printf("Successful Runs:    %d%n", successfulRuns);
        System.out.printf("Deadlock Runs:      %d%n", deadlockRuns);
        System.out.printf(Locale.US, "Success Rate:       %.2f%%%n", 
            (successfulRuns * 100.0) / allRuns.size());
        
        System.out.println("METRICS FOR SUCCESSFUL RUNS ONLY:");
        System.out.println("-".repeat(80));
        System.out.printf(Locale.US, "Average Total Utility:  %.4f%n", getAverageSuccessful("totalUtility"));
        System.out.printf(Locale.US, "Average Total Steps:    %.2f%n", getAverageSuccessful("totalSteps"));
        System.out.printf(Locale.US, "Average Total Actions:  %.2f%n", getAverageSuccessful("totalActions"));
        System.out.printf(Locale.US, "Average Agent1 Utility: %.4f%n", getAverageSuccessful("agent1Utility"));
        System.out.printf(Locale.US, "Average Agent2 Utility: %.4f%n", getAverageSuccessful("agent2Utility"));
        System.out.println("=".repeat(80));
    }
    
    private double getAverage(String metric) {
        if (allRuns.isEmpty()) return 0.0;
        return allRuns.stream().mapToDouble(r -> getValue(r, metric)).average().orElse(0.0);
    }
    
    private double getAverageSuccessful(String metric) {
        if (successfulRuns == 0) return 0.0;
        return allRuns.stream()
            .filter(r -> !r.wasDeadlock)
            .mapToDouble(r -> getValue(r, metric))
            .average().orElse(0.0);
    }
    
    private double getMin(String metric) {
        return allRuns.stream().mapToDouble(r -> getValue(r, metric)).min().orElse(0.0);
    }
    
    private double getMinSuccessful(String metric) {
        return allRuns.stream()
            .filter(r -> !r.wasDeadlock)
            .mapToDouble(r -> getValue(r, metric)).min().orElse(0.0);
    }
    
    private double getMax(String metric) {
        return allRuns.stream().mapToDouble(r -> getValue(r, metric)).max().orElse(0.0);
    }
    
    private double getMaxSuccessful(String metric) {
        return allRuns.stream()
            .filter(r -> !r.wasDeadlock)
            .mapToDouble(r -> getValue(r, metric)).max().orElse(0.0);
    }
    
    private double getStdDev(String metric) {
        if (allRuns.isEmpty()) return 0.0;
        double mean = getAverage(metric);
        double sumSq = allRuns.stream().mapToDouble(r -> Math.pow(getValue(r, metric) - mean, 2)).sum();
        return Math.sqrt(sumSq / allRuns.size());
    }
    
    private double getStdDevSuccessful(String metric) {
        if (successfulRuns == 0) return 0.0;
        double mean = getAverageSuccessful(metric);
        double sumSq = allRuns.stream()
            .filter(r -> !r.wasDeadlock)
            .mapToDouble(r -> Math.pow(getValue(r, metric) - mean, 2))
            .sum();
        return Math.sqrt(sumSq / successfulRuns);
    }
    
    private double getValue(RunStatistics run, String metric) {
        switch(metric) {
            case "totalUtility": return run.totalUtility;
            case "totalSteps": return run.totalSteps;
            case "totalActions": return run.totalActions;
            case "agent1Utility": return run.agent1Utility;
            case "agent1Steps": return run.agent1Steps;
            case "agent1Actions": return run.agent1Actions;
            case "agent2Utility": return run.agent2Utility;
            case "agent2Steps": return run.agent2Steps;
            case "agent2Actions": return run.agent2Actions;
            default: return 0.0;
        }
    }
}