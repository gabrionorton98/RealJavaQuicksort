// QuicksortVisualizer.java
// A Java Swing application to visually demonstrate the Quicksort algorithm with color coding and controls.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class QuicksortVisualizer extends JFrame {
    private static final int ARRAY_SIZE = 50;
    private static final int BAR_WIDTH = 12;
    private static final int PANEL_HEIGHT = 400;
    private static final int PANEL_WIDTH = ARRAY_SIZE * BAR_WIDTH + 40;

    private int[] array = new int[ARRAY_SIZE];
    private int[] states = new int[ARRAY_SIZE]; // 0: normal, 1: pivot, 2: compared, 3: sorted
    private VisualPanel panel;
    private JButton startBtn, pauseBtn, resetBtn;
    private JSlider speedSlider;
    private volatile boolean running = false, paused = false;
    private Thread sortThread;

    public QuicksortVisualizer() {
        setTitle("Quicksort Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        panel = new VisualPanel();
        panel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        add(panel, BorderLayout.CENTER);
        JPanel controls = new JPanel();
        startBtn = new JButton("Start");
        pauseBtn = new JButton("Pause");
        resetBtn = new JButton("Reset");
        speedSlider = new JSlider(1, 100, 30);
        speedSlider.setToolTipText("Speed");
        controls.add(startBtn);
        controls.add(pauseBtn);
        controls.add(resetBtn);
        controls.add(new JLabel("Speed:"));
        controls.add(speedSlider);
        add(controls, BorderLayout.SOUTH);
        startBtn.addActionListener(e -> startSort());
        pauseBtn.addActionListener(e -> togglePause());
        resetBtn.addActionListener(e -> resetArray());
        resetArray();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void resetArray() {
        if (sortThread != null && sortThread.isAlive()) {
            running = false;
            try { sortThread.join(); } catch (InterruptedException ignored) {}
        }
        Random rand = new Random();
        for (int i = 0; i < ARRAY_SIZE; i++) {
            array[i] = rand.nextInt(PANEL_HEIGHT - 40) + 20;
            states[i] = 0;
        }
        panel.repaint();
        running = false;
        paused = false;
    }

    private void startSort() {
        if (running) return;
        running = true;
        paused = false;
        sortThread = new Thread(() -> {
            quicksort(0, array.length - 1);
            for (int i = 0; i < ARRAY_SIZE; i++) states[i] = 3;
            panel.repaint();
            running = false;
        });
        sortThread.start();
    }

    private void togglePause() {
        if (!running) return;
        paused = !paused;
        pauseBtn.setText(paused ? "Resume" : "Pause");
    }

    private void quicksort(int low, int high) {
        if (!running) return;
        if (low < high) {
            int pi = partition(low, high);
            quicksort(low, pi - 1);
            quicksort(pi + 1, high);
        }
    }

    private int partition(int low, int high) {
        int pivot = array[high];
        states[high] = 1;
        int i = low - 1;
        for (int j = low; j < high; j++) {
            states[j] = 2;
            panel.repaint();
            sleep();
            if (array[j] < pivot) {
                i++;
                swap(i, j);
            }
            states[j] = 0;
            waitIfPaused();
        }
        swap(i + 1, high);
        states[high] = 0;
        return i + 1;
    }

    private void swap(int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        panel.repaint();
        sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(110 - speedSlider.getValue());
        } catch (InterruptedException ignored) {}
    }

    private void waitIfPaused() {
        while (paused) {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
    }

    class VisualPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < ARRAY_SIZE; i++) {
                switch (states[i]) {
                    case 1: g.setColor(Color.RED); break; // Pivot
                    case 2: g.setColor(Color.ORANGE); break; // Compared
                    case 3: g.setColor(Color.GREEN); break; // Sorted
                    default: g.setColor(Color.BLUE); break; // Normal
                }
                g.fillRect(20 + i * BAR_WIDTH, PANEL_HEIGHT - array[i], BAR_WIDTH - 2, array[i]);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuicksortVisualizer::new);
    }
}
