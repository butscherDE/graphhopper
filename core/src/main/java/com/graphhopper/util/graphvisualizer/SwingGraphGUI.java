package com.graphhopper.util.graphvisualizer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class SwingGraphGUI {
	public static final Color BACKGROUND = Color.WHITE;
	public static final Color EDGES = Color.BLACK;
	public static final Color NODES = new Color(0, 0, 255);
	public static final int MARGIN = 10;
	public static final int NODE_SIZE = 4;


//	public static void main(String[] args) throws IOException {
//		final List<double[]> coords = new ArrayList<>();
//		final List<int[]> neighbors = new ArrayList<>();
//		System.out.println("Working Directory = " +
//                           System.getProperty("user.dir"));
//		try (BufferedReader r = Files.newBufferedReader(Paths.get("rome_coords.csv"))) {
//			for (String line; (line = r.readLine()) != null;) {
//				final String[] parts = line.split("\t");
//				coords.add(new double[] {Double.valueOf(parts[0]), Double.valueOf(parts[1]) });
//				neighbors.add(Arrays.stream(parts[2].split(",")).mapToInt(Integer::parseInt).toArray());
//			}
//		}
//		visualizeGraph(neighbors.toArray(new int[neighbors.size()][]), coords.toArray(new double[coords.size()][]));
//	}

	public static void visualizeGraph(final List<Node> nodes, final List<Edge> edges, final int startEdgeID) {
		// compute ranges of X and Y coordinates
		double[] minMax = extractMinMax(nodes);
		final double spreadX = minMax[1] - minMax[0];
		final double spreadY = minMax[3] - minMax[2];
		final double originalRatio = spreadX / spreadY;

		// create window
		final JFrame frame = new JFrame(SwingGraphGUI.class.getSimpleName());

		// panel which contains the actual graph
		final JPanel main = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				// white background
				g.setColor(BACKGROUND);
				g.fillRect(0, 0, this.getWidth(), this.getHeight());

				// enable antialiasing
				final Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				// leave some gap around the edges
				final int w = this.getWidth() - 2 * MARGIN;
				final int h = this.getHeight() - 2 * MARGIN;
				final double ratio = 1.0 * w / h;

				// scale the coordinates so that they fit into the remaining rectangle
				final double padX;
				final double padY;
				final double scale;
				if (originalRatio < ratio) {
					scale = h / spreadY;
					padX = (w - scale * spreadX) / 2 + MARGIN;
					padY = MARGIN;
				} else {
					scale = w / spreadX;
					padX = MARGIN;
					padY = (h - scale * spreadY) / 2 + MARGIN;
				}

				// draw the nodes
				final Map<Integer, Node> nodesIndex = new HashMap<>();
				g2d.setColor(NODES);
				for (Node node : nodes) {
					nodesIndex.put(node.id, node);

					final double x = (node.longitude - minMax[0]) * scale + padX;
					final double y = (node.latitude - minMax[2]) * scale + padY;
					g2d.fill(new Ellipse2D.Double(x - NODE_SIZE / 2, y - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE));
					g2d.drawString(Integer.toString(node.id), (float) x, (float) y);
				}

				// draw the edges
				g2d.setColor(EDGES);
				for (Edge edge : edges) {
					if (edge.id == startEdgeID) {
						g2d.setColor(Color.RED);
					}
					final Node baseNode = nodesIndex.get(edge.baseNode);
					final Node adjNode = nodesIndex.get(edge.adjNode);

					final double xi = (baseNode.longitude - minMax[0]) * scale + padX;
					final double yi = (baseNode.latitude - minMax[2]) * scale + padY;
					final double xj = (adjNode.longitude - minMax[0]) * scale + padX;
					final double yj = (adjNode.latitude - minMax[2]) * scale + padY;
					g2d.draw(new Line2D.Double(xi, yi, xj, yj));
					g2d.setColor(EDGES);
				}
			}
		};

		main.setPreferredSize(new Dimension(1000, 1000));
		frame.setContentPane(main);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private static double[] extractMinMax(List<Node> nodes) {
		double[] minMax = {Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE };

		for (Node node : nodes) {
			minMax[0] = Math.min(minMax[0], node.longitude);
			minMax[1] = Math.max(minMax[1], node.longitude);
			minMax[2] = Math.min(minMax[2], node.latitude);
			minMax[3] = Math.max(minMax[3], node.latitude);
		}

		return minMax;
	}
}
