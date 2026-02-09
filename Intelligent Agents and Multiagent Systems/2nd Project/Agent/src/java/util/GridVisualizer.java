package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GridVisualizer extends JFrame {
    private static final int CELL_SIZE = 80;
    private static final int GRID_WIDTH = 5;
    private static final int GRID_HEIGHT = 5;
    
    private GridPanel gridPanel;
    private JLabel statusLabel;
    private JLabel agent1Label;
    private JLabel agent2Label;
    private JLabel systemLabel;
    
    private static GridVisualizer instance;
    
    public static synchronized GridVisualizer getInstance() {
        if (instance == null) {
            instance = new GridVisualizer();
        }
        return instance;
    }
    
    private GridVisualizer() {
        setTitle("Multi-Agent Grid Visualization");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Grid panel
        gridPanel = new GridPanel();
        add(gridPanel, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setBackground(new Color(240, 240, 240));
        
        statusLabel = createInfoLabel("Status: Initializing...");
        agent1Label = createInfoLabel("Agent 1: (1,1) | Reward: 0.00");
        agent2Label = createInfoLabel("Agent 2: (3,3) | Reward: 0.00");
        systemLabel = createInfoLabel("System: Steps: 0 | Actions: 0");
        
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(agent1Label);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(agent2Label);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(systemLabel);
        
        add(infoPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                instance = null;
            }
        });
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 12));
        return label;
    }
    
    public void updateGrid(GridState state) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.setState(state);
            
            statusLabel.setText(String.format("Status: Run %d | Goals: T:%s Ch:%s D:%s",
                state.currentRun,
                state.tablePainted ? "✓" : "✗",
                state.chairPainted ? "✓" : "✗",
                state.doorOpen ? "✓" : "✗"));
            
            agent1Label.setText(String.format("Agent 1: (%d,%d) | Reward: %.2f | Inv: %s",
                state.agent1X, state.agent1Y, state.agent1Reward,
                state.agent1Inventory.isEmpty() ? "empty" : String.join(",", state.agent1Inventory)));
            
            agent2Label.setText(String.format("Agent 2: (%d,%d) | Reward: %.2f | Inv: %s",
                state.agent2X, state.agent2Y, state.agent2Reward,
                state.agent2Inventory.isEmpty() ? "empty" : String.join(",", state.agent2Inventory)));
            
            systemLabel.setText(String.format("System: Steps: %d | Actions: %d | Total Reward: %.2f",
                state.totalSteps, state.totalActions, state.totalReward));
            
            gridPanel.repaint();
        });
    }
    
    public static class GridState {
        public int currentRun = 1;
        public int agent1X = 1, agent1Y = 1;
        public int agent2X = 3, agent2Y = 3;
        public double agent1Reward = 0, agent2Reward = 0, totalReward = 0;
        public int totalSteps = 0, totalActions = 0;
        public java.util.Set<String> agent1Inventory = new java.util.HashSet<>();
        public java.util.Set<String> agent2Inventory = new java.util.HashSet<>();
        
        public Map<String, int[]> items = new HashMap<>();
        public int[] doorPos = {0, 0};
        public int[] tablePos = {0, 0};
        public int[] chairPos = {0, 0};
        
        public boolean tablePainted = false;
        public boolean chairPainted = false;
        public boolean doorOpen = false;
    }
    
    class GridPanel extends JPanel {
        private GridState state = new GridState();
        
        public void setState(GridState state) {
            this.state = state;
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(GRID_WIDTH * CELL_SIZE + 50, GRID_HEIGHT * CELL_SIZE + 50);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int offsetX = 25;
            int offsetY = 25;
            
            // Draw grid
            for (int x = 1; x <= GRID_WIDTH; x++) {
                for (int y = 1; y <= GRID_HEIGHT; y++) {
                    int screenX = offsetX + (x - 1) * CELL_SIZE;
                    int screenY = offsetY + (GRID_HEIGHT - y) * CELL_SIZE;
                    
                    // Check if obstacle
                    if (isObstacle(x, y)) {
                        g2d.setColor(new Color(60, 60, 60));
                        g2d.fillRect(screenX, screenY, CELL_SIZE, CELL_SIZE);
                    } else {
                        g2d.setColor(Color.WHITE);
                        g2d.fillRect(screenX, screenY, CELL_SIZE, CELL_SIZE);
                    }
                    
                    // Draw grid lines
                    g2d.setColor(Color.GRAY);
                    g2d.drawRect(screenX, screenY, CELL_SIZE, CELL_SIZE);
                    
                    // Draw coordinates
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2d.drawString(x + "," + y, screenX + 5, screenY + 15);
                }
            }
            
            // Draw items
            for (Map.Entry<String, int[]> entry : state.items.entrySet()) {
                int[] pos = entry.getValue();
                drawItem(g2d, pos[0], pos[1], entry.getKey(), offsetX, offsetY);
            }
            
            // Draw goals
            drawGoal(g2d, state.doorPos[0], state.doorPos[1], "D", 
                state.doorOpen ? Color.GREEN : new Color(139, 69, 19), offsetX, offsetY);
            drawGoal(g2d, state.tablePos[0], state.tablePos[1], "T", 
                state.tablePainted ? Color.GREEN : new Color(139, 69, 19), offsetX, offsetY);
            drawGoal(g2d, state.chairPos[0], state.chairPos[1], "Ch", 
                state.chairPainted ? Color.GREEN : new Color(139, 69, 19), offsetX, offsetY);
            
            // Draw agents
            drawAgent(g2d, state.agent1X, state.agent1Y, "A1", 
                new Color(0, 120, 215), offsetX, offsetY);
            drawAgent(g2d, state.agent2X, state.agent2Y, "A2", 
                new Color(232, 17, 35), offsetX, offsetY);
        }
        
        private void drawItem(Graphics2D g2d, int x, int y, String label, int offsetX, int offsetY) {
            int screenX = offsetX + (x - 1) * CELL_SIZE;
            int screenY = offsetY + (GRID_HEIGHT - y) * CELL_SIZE;
            
            g2d.setColor(new Color(255, 165, 0));
            g2d.fillOval(screenX + 20, screenY + 20, 20, 20);
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = screenX + 30 - fm.stringWidth(label) / 2;
            int textY = screenY + 32;
            g2d.drawString(label, textX, textY);
        }
        
        private void drawGoal(Graphics2D g2d, int x, int y, String label, Color color, int offsetX, int offsetY) {
            int screenX = offsetX + (x - 1) * CELL_SIZE;
            int screenY = offsetY + (GRID_HEIGHT - y) * CELL_SIZE;
            
            g2d.setColor(color);
            g2d.fillRect(screenX + 45, screenY + 15, 30, 30);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = screenX + 60 - fm.stringWidth(label) / 2;
            int textY = screenY + 33;
            g2d.drawString(label, textX, textY);
        }
        
        private void drawAgent(Graphics2D g2d, int x, int y, String label, Color color, int offsetX, int offsetY) {
            int screenX = offsetX + (x - 1) * CELL_SIZE;
            int screenY = offsetY + (GRID_HEIGHT - y) * CELL_SIZE;
            
            // Draw agent circle
            g2d.setColor(color);
            g2d.fillOval(screenX + 15, screenY + 45, 35, 35);
            
            // Draw label
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = screenX + 32 - fm.stringWidth(label) / 2;
            int textY = screenY + 67;
            g2d.drawString(label, textX, textY);
        }
        
        private boolean isObstacle(int x, int y) {
            return (x == 2 && y == 1) || (x == 2 && y == 2) || 
                   (x == 4 && y == 4) || (x == 4 && y == 5);
        }
    }
}