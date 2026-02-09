package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

//A* Pathfinding service for grid navigation
public class PathFindingService {
    
    public static class Node implements Comparable<Node> {
        int x, y;
        int g; 
        int h; 
        Node parent;
        
        public Node(int x, int y, int g, int h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }
        
        public int f() {
            return g + h;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f(), other.f());
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    // Find shortest path using A* algorithm
    public static List<String> findPath(int startX, int startY, int goalX, int goalY) {
        if (startX == goalX && startY == goalY) {
            return new ArrayList<>();
        }
        
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        
        Node startNode = new Node(startX, startY, 0, manhattan(startX, startY, goalX, goalY), null);
        openSet.add(startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.x == goalX && current.y == goalY) {
                return reconstructPath(current);
            }
            
            closedSet.add(current);
            
            for (int[] dir : new int[][]{{1,0}, {-1,0}, {0,1}, {0,-1}}) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];
                
                if (!isValidPosition(newX, newY)) {
                    continue;
                }
                
                Node neighbor = new Node(newX, newY, current.g + 1, 
                                        manhattan(newX, newY, goalX, goalY), current);
                
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                boolean found = false;
                for (Node open : openSet) {
                    if (open.equals(neighbor) && open.g <= neighbor.g) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    openSet.removeIf(n -> n.equals(neighbor));
                    openSet.add(neighbor);
                }
            }
        }
        
        return new ArrayList<>();
    }
    
    // Get path length (number of steps)
    public static int getPathLength(int startX, int startY, int goalX, int goalY) {
        List<String> path = findPath(startX, startY, goalX, goalY);
        if (path.isEmpty() && (startX != goalX || startY != goalY)) {
            return manhattan(startX, startY, goalX, goalY);
        }
        return path.size();
    }
    
    private static boolean isValidPosition(int x, int y) {
        return x >= 1 && x <= 5 && y >= 1 && y <= 5 && !isObstacle(x, y);
    }
    
    private static boolean isObstacle(int x, int y) {
        return (x == 2 && y == 1) || 
               (x == 2 && y == 2) ||
               (x == 4 && y == 4) ||
               (x == 4 && y == 5);
    }
    
    private static int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    private static List<String> reconstructPath(Node goal) {
        List<String> path = new ArrayList<>();
        Node current = goal;
        
        while (current.parent != null) {
            int dx = current.x - current.parent.x;
            int dy = current.y - current.parent.y;
            
            if (dx == 1) path.add(0, "right");
            else if (dx == -1) path.add(0, "left");
            else if (dy == 1) path.add(0, "up");
            else if (dy == -1) path.add(0, "down");
            
            current = current.parent;
        }
        
        return path;
    }
}