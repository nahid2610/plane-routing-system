import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PlaneRoutingSystemGUI extends JFrame {

    static final int V = 6;
    int[][] graph = {
            {0, 45, 50, 40, 44, 60},
            {45, 0, 60, 175, 130, 155},
            {50, 60, 0, 225, 180, 255},
            {40, 175, 225, 0, 145, 150},
            {44, 130, 180, 145, 0, 160},
            {60, 155, 255, 150, 160, 0}
    };
    String[] airportsName = {"Dhaka", "Jashore", "Barishal", "Sylhet", "Chattogram", "Cox-Bazar"};
    int[] parent = new int[V];
    int[] dist = new int[V];

    JTextArea outputArea;
    JComboBox<String> fromBox1;   // used for "airports available from" and "distance from"
    JComboBox<String> fromBox2;   // source for shortest path
    JComboBox<String> toBox2;     // destination for shortest path

    public PlaneRoutingSystemGUI() {
        setTitle("Plane Routing System");
        setSize(720, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildControls(), BorderLayout.WEST);
        add(buildOutputPanel(), BorderLayout.CENTER);

        showAllAirports(); // greet with the airport list on launch
    }

    private JComponent buildHeader() {
        JLabel title = new JLabel("---- WELCOME TO PLANE ROUTING SYSTEM ----", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(12, 10, 12, 10));
        return title;
    }

    private JComponent buildControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(260, 0));

        // Option 1: See all airports
        JButton btnAll = new JButton("1: See All Airports");
        btnAll.addActionListener(this::onShowAll);

        // Option 2: Airports available from nearest airport
        fromBox1 = new JComboBox<>(airportsName);
        JButton btnNearby = new JButton("2: Airports Available From");
        btnNearby.addActionListener(this::onNearby);

        // Option 3: Shortest path
        fromBox2 = new JComboBox<>(airportsName);
        toBox2 = new JComboBox<>(airportsName);
        toBox2.setSelectedIndex(1);
        JButton btnPath = new JButton("3: Find Shortest Path");
        btnPath.addActionListener(this::onShortestPath);

        // Option 4: Distance table from a location
        JButton btnDistances = new JButton("4: Distances From Location");
        btnDistances.addActionListener(this::onDistances);

        JButton btnExit = new JButton("0: Exit");
        btnExit.addActionListener(e -> System.exit(0));

        panel.add(sectionLabel("Option 1"));
        panel.add(btnAll);
        panel.add(Box.createVerticalStrut(15));

        panel.add(sectionLabel("Option 2 — From airport"));
        panel.add(fromBox1);
        panel.add(btnNearby);
        panel.add(Box.createVerticalStrut(15));

        panel.add(sectionLabel("Option 3 — From / To"));
        panel.add(fromBox2);
        panel.add(toBox2);
        panel.add(btnPath);
        panel.add(Box.createVerticalStrut(15));

        panel.add(sectionLabel("Option 4 — From airport"));
        panel.add(btnDistances);
        panel.add(Box.createVerticalStrut(25));

        panel.add(btnExit);

        return panel;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JComponent buildOutputPanel() {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(new EmptyBorder(10, 0, 10, 10));
        return scroll;
    }

    // ---------- Button handlers ----------

    private void onShowAll(ActionEvent e) {
        showAllAirports();
    }

    private void onNearby(ActionEvent e) {
        int idx = fromBox1.getSelectedIndex();
        StringBuilder sb = new StringBuilder();
        sb.append("Airports available from ").append(airportsName[idx]).append(":\n\n");
        for (int i = 0; i < V; i++) {
            if (graph[idx][i] != 0) {
                sb.append("  - ").append(airportsName[i])
                  .append(" (Distance: ").append(graph[idx][i]).append(" km)\n");
            }
        }
        outputArea.setText(sb.toString());
    }

    private void onShortestPath(ActionEvent e) {
        int src = fromBox2.getSelectedIndex();
        int des = toBox2.getSelectedIndex();
        dijkstra(src);

        StringBuilder sb = new StringBuilder();
        sb.append("Shortest path from ").append(airportsName[src])
          .append(" to ").append(airportsName[des]).append(":\n\n  ");
        sb.append(buildPath(src, des));
        sb.append("\n\nTotal distance: ").append(dist[des]).append(" KM");
        outputArea.setText(sb.toString());
    }

    private void onDistances(ActionEvent e) {
        int idx = fromBox1.getSelectedIndex();
        dijkstra(idx);

        StringBuilder sb = new StringBuilder();
        sb.append("Distances from ").append(airportsName[idx]).append(":\n\n");
        sb.append(String.format("%-15s %-15s %10s%n", "From", "To", "Distance"));
        sb.append("--------------------------------------------\n");
        for (int i = 0; i < V; i++) {
            if (i != idx) {
                sb.append(String.format("%-15s %-15s %8d KM%n",
                        airportsName[idx], airportsName[i], dist[i]));
            }
        }
        outputArea.setText(sb.toString());
    }

    // ---------- Core logic (same as console version) ----------

    private void showAllAirports() {
        StringBuilder sb = new StringBuilder("All Airports:\n\n");
        for (int i = 0; i < V; i++) {
            sb.append("  ").append(i + 1).append(": ").append(airportsName[i]).append("\n");
        }
        outputArea.setText(sb.toString());
    }

    private int minNode(int[] dist, boolean[] spSet) {
        int min = Integer.MAX_VALUE, minNode = -1;
        for (int v = 0; v < V; v++) {
            if (!spSet[v] && dist[v] < min) {
                min = dist[v];
                minNode = v;
            }
        }
        return minNode;
    }

    private void dijkstra(int sourceNode) {
        boolean[] spSet = new boolean[V];
        for (int i = 0; i < V; i++) {
            dist[i] = Integer.MAX_VALUE;
            spSet[i] = false;
        }
        dist[sourceNode] = 0;
        parent[sourceNode] = -1;

        for (int count = 0; count < V - 1; count++) {
            int u = minNode(dist, spSet);
            spSet[u] = true;
            for (int v = 0; v < V; v++) {
                if (graph[u][v] != 0 && !spSet[v] && dist[u] + graph[u][v] < dist[v]) {
                    parent[v] = u;
                    dist[v] = dist[u] + graph[u][v];
                }
            }
        }
    }

    private String buildPath(int src, int des) {
        if (src == des) {
            return airportsName[src];
        }
        if (parent[des] == -1) {
            return "No path exists between " + airportsName[src] + " and " + airportsName[des];
        }
        return buildPath(src, parent[des]) + " -> " + airportsName[des];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlaneRoutingSystemGUI().setVisible(true));
    }
}