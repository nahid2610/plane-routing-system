import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class plane_routing_system extends JFrame {

    static final int V = 6;

    static int[][] graph = {
            {0, 45, 50, 40, 44, 60},
            {45, 0, 60, 175, 130, 155},
            {50, 60, 0, 225, 180, 255},
            {40, 175, 225, 0, 145, 150},
            {44, 130, 180, 145, 0, 160},
            {60, 155, 255, 150, 160, 0}
    };

    static String[] airportsName = {
            "Dhaka", "Jashore", "Barishal", "Sylhet", "Chattogram", "Cox-Bazar"
    };

    static int[] parent = new int[V];
    static int[] dist = new int[V];

    private JComboBox<String> nearbyBox;
    private JComboBox<String> fromBox;
    private JComboBox<String> toBox;
    private JTextArea outputArea;
    private JLabel statusLabel;

    public plane_routing_system() {
        setTitle("Plane Routing System");
        setSize(1000, 650);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainPanel(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        showAllAirports();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 25, 18, 25));
        header.setBackground(new Color(28, 42, 65));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("PLANE ROUTING SYSTEM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
                "Find routes, distances and shortest paths between airports");
        subtitle.setForeground(new Color(205, 215, 230));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JLabel badge = new JLabel("6 AIRPORTS");
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(95, 160, 220)),
                new EmptyBorder(8, 12, 8, 12)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Color.WHITE);
        side.setBorder(new EmptyBorder(20, 18, 20, 18));
        side.setPreferredSize(new Dimension(285, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel menu = new JLabel("ROUTING MENU");
        menu.setFont(new Font("SansSerif", Font.BOLD, 12));
        menu.setForeground(new Color(90, 100, 115));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(menu);
        side.add(Box.createVerticalStrut(15));

        JButton all = createButton("1.  See All Airports");
        all.addActionListener(this::onShowAll);
        side.add(all);
        side.add(Box.createVerticalStrut(10));

        side.add(label("FROM AIRPORT"));
        nearbyBox = new JComboBox<>(airportsName);
        styleCombo(nearbyBox);
        side.add(nearbyBox);
        side.add(Box.createVerticalStrut(8));

        JButton nearby = createButton("2.  Available Airports");
        nearby.addActionListener(this::onNearby);
        side.add(nearby);
        side.add(Box.createVerticalStrut(18));

        side.add(label("SHORTEST ROUTE"));
        fromBox = new JComboBox<>(airportsName);
        toBox = new JComboBox<>(airportsName);
        toBox.setSelectedIndex(1);
        styleCombo(fromBox);
        styleCombo(toBox);
        side.add(fromBox);
        side.add(Box.createVerticalStrut(7));
        side.add(toBox);
        side.add(Box.createVerticalStrut(8));

        JButton path = createButton("3.  Find Shortest Path");
        path.addActionListener(this::onShortestPath);
        side.add(path);
        side.add(Box.createVerticalStrut(18));

        side.add(label("DISTANCE TABLE"));
        JButton distances = createButton("4.  Distances From Airport");
        distances.addActionListener(this::onDistances);
        side.add(distances);

        side.add(Box.createVerticalGlue());

        JButton exit = createButton("0.  Exit");
        exit.addActionListener(e -> System.exit(0));
        exit.setBackground(new Color(145, 55, 60));
        side.add(exit);

        return side;
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 22, 18, 22));

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232)),
                new EmptyBorder(18, 20, 18, 20)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel heading = new JLabel("Routing Results");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setForeground(new Color(35, 45, 60));

        JLabel hint = new JLabel("Dijkstra shortest-path algorithm");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(new Color(100, 110, 125));

        top.add(heading, BorderLayout.WEST);
        top.add(hint, BorderLayout.EAST);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setForeground(new Color(45, 55, 70));
        outputArea.setBackground(new Color(250, 251, 253));
        outputArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(225, 229, 235)));

        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(7, 15, 7, 15));
        bar.setBackground(new Color(28, 42, 65));

        statusLabel = new JLabel("Ready - Select an option from the routing menu");
        statusLabel.setForeground(new Color(220, 228, 238));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel algorithm = new JLabel("Algorithm: Dijkstra");
        algorithm.setForeground(new Color(180, 195, 215));
        algorithm.setFont(new Font("SansSerif", Font.PLAIN, 12));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(algorithm, BorderLayout.EAST);
        return bar;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(95, 105, 120));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        combo.setPreferredSize(new Dimension(240, 38));
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setPreferredSize(new Dimension(240, 40));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(45, 105, 170));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void onShowAll(ActionEvent e) {
        showAllAirports();
        statusLabel.setText("Showing all available airports");
    }

    private void onNearby(ActionEvent e) {
        int idx = nearbyBox.getSelectedIndex();
        StringBuilder sb = new StringBuilder();

        sb.append("AIRPORTS AVAILABLE FROM: ")
                .append(airportsName[idx].toUpperCase()).append("\n");
        sb.append("============================================================\n\n");

        for (int i = 0; i < V; i++) {
            if (graph[idx][i] != 0) {
                sb.append(String.format("  %-18s  %4d km%n",
                        airportsName[i], graph[idx][i]));
            }
        }

        outputArea.setText(sb.toString());
        statusLabel.setText("Showing direct airports from " + airportsName[idx]);
    }

    private void onShortestPath(ActionEvent e) {
        int src = fromBox.getSelectedIndex();
        int des = toBox.getSelectedIndex();

        if (src == des) {
            outputArea.setText(
                    "SOURCE AND DESTINATION ARE THE SAME\n\n"
                    + airportsName[src] + " -> " + airportsName[des]
                    + "\n\nPlease select two different airports.");
            statusLabel.setText("Choose different source and destination airports");
            return;
        }

        dijkstra(src);

        StringBuilder sb = new StringBuilder();
        sb.append("SHORTEST ROUTE\n");
        sb.append("============================================================\n\n");
        sb.append("  From       : ").append(airportsName[src]).append("\n");
        sb.append("  Destination: ").append(airportsName[des]).append("\n\n");
        sb.append("  Path       : ").append(buildPath(src, des)).append("\n");
        sb.append("  Distance   : ").append(dist[des]).append(" KM\n");

        outputArea.setText(sb.toString());
        statusLabel.setText("Shortest path calculated successfully");
    }

    private void onDistances(ActionEvent e) {
        int idx = nearbyBox.getSelectedIndex();
        dijkstra(idx);

        StringBuilder sb = new StringBuilder();
        sb.append("DISTANCES FROM: ")
                .append(airportsName[idx].toUpperCase()).append("\n");
        sb.append("============================================================\n\n");
        sb.append(String.format("  %-18s %10s%n", "Airport", "Distance"));
        sb.append("  --------------------------------------------------\n");

        for (int i = 0; i < V; i++) {
            if (i != idx) {
                sb.append(String.format("  %-18s %8d KM%n",
                        airportsName[i], dist[i]));
            }
        }

        outputArea.setText(sb.toString());
        statusLabel.setText("Distance table calculated from " + airportsName[idx]);
    }

    private void showAllAirports() {
        StringBuilder sb = new StringBuilder();
        sb.append("ALL AIRPORTS\n");
        sb.append("============================================================\n\n");

        for (int i = 0; i < V; i++) {
            sb.append(String.format("  %d. %s%n", i + 1, airportsName[i]));
        }

        sb.append("\n------------------------------------------------------------\n");
        sb.append("Choose an operation from the menu to explore the routes.");

        outputArea.setText(sb.toString());
    }

    private int minNode(int[] distance, boolean[] spSet) {
        int min = Integer.MAX_VALUE;
        int minNode = -1;

        for (int v = 0; v < V; v++) {
            if (!spSet[v] && distance[v] < min) {
                min = distance[v];
                minNode = v;
            }
        }
        return minNode;
    }

    private void dijkstra(int sourceNode) {
        boolean[] spSet = new boolean[V];

        for (int i = 0; i < V; i++) {
            dist[i] = Integer.MAX_VALUE;
            parent[i] = -1;
        }

        dist[sourceNode] = 0;

        for (int count = 0; count < V - 1; count++) {
            int u = minNode(dist, spSet);
            if (u == -1) break;

            spSet[u] = true;

            for (int v = 0; v < V; v++) {
                if (graph[u][v] != 0 && !spSet[v]
                        && dist[u] != Integer.MAX_VALUE
                        && dist[u] + graph[u][v] < dist[v]) {
                    parent[v] = u;
                    dist[v] = dist[u] + graph[u][v];
                }
            }
        }
    }

    private String buildPath(int src, int des) {
        if (src == des) return airportsName[src];

        if (parent[des] == -1) {
            return "No path exists between " + airportsName[src]
                    + " and " + airportsName[des];
        }

        return buildPath(src, parent[des]) + " -> " + airportsName[des];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            new plane_routing_system().setVisible(true);
        });
    }
}
