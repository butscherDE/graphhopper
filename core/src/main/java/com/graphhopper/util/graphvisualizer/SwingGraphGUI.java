package com.graphhopper.util.graphvisualizer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

public class SwingGraphGUI {
	public static final Color BACKGROUND = Color.WHITE;
	public static final Color EDGES = Color.BLACK;
	private static final Color EDGES_HIGHLIGHTET = Color.RED;
	public static final Color NODES = new Color(0, 0, 255);
	public static final int MARGIN = 10;
	public static final int NODE_SIZE = 4;

	private final Collection<Node> nodes;
	private final Collection<Edge> edges;
	private List<Edge> edgesToHighlight;
	private Graphics2D g2d;
	private ScaledCoordinates scaledCoordinates;
	private double[] minMax;
	private Map<Integer, Node> nodesIndex;

	public SwingGraphGUI(Collection<Node> nodes, Collection<Edge> edges) {
		this.nodes = nodes;
		this.edges = edges;
		this.edgesToHighlight = new LinkedList<>();
		this.nodesIndex = new HashMap<>();

		for (Node node : nodes) {
			nodesIndex.put(node.id, node);
		}
	}

	public void addEdgeToHighlight(final Edge edge) {
		this.edgesToHighlight.add(edge);
	}

	public void visualizeGraph() {
		// compute ranges of X and Y coordinates
		minMax = extractMinMax(nodes);
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
				g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				// leave some gap around the edges
				final int w = this.getWidth() - 2 * MARGIN;
				final int h = this.getHeight() - 2 * MARGIN;
				final double ratio = 1.0 * w / h;

				// scale the coordinates so that they fit into the remaining rectangle
				scaledCoordinates = new ScaledCoordinates(w, h, ratio, originalRatio, spreadY, spreadX).invoke();

				final Map<Integer, Node> nodesIndex = drawNodes();
				drawEdges(nodesIndex);

			}
		};

		main.setPreferredSize(new Dimension(1000, 1000));
		frame.setContentPane(main);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private Map<Integer, Node> drawNodes() {
		g2d.setColor(NODES);
		for (Node node : nodes) {
			final double x = (node.longitude - minMax[0]) * scaledCoordinates.scale + scaledCoordinates.padX;
			final double y = (node.latitude - minMax[2]) * scaledCoordinates.scale + scaledCoordinates.padY;
			g2d.fill(new Ellipse2D.Double(x - NODE_SIZE / 2, y - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE));
			g2d.drawString(Integer.toString(node.id), (float) x, (float) y);
		}
		return nodesIndex;
	}

	private void drawEdges(Map<Integer, Node> nodesIndex) {
		drawNormalEdges(nodesIndex);
		drawHighlightedEdges(nodesIndex);
		g2d.setColor(EDGES);
	}

	private void drawNormalEdges(Map<Integer, Node> nodesIndex) {
		for (Edge edge : edges) {
			drawEdge(nodesIndex, edge, EDGES);
		}
	}

	private void drawHighlightedEdges(Map<Integer, Node> nodesIndex) {
		for (Edge edge : edgesToHighlight) {
			drawEdge(nodesIndex, edge, EDGES_HIGHLIGHTET);
		}
	}

	private void drawEdge(Map<Integer, Node> nodesIndex, Edge edge, Color color) {
		g2d.setColor(color);
		final Node baseNode = nodesIndex.get(edge.baseNode);
		final Node adjNode = nodesIndex.get(edge.adjNode);

		final double xi = (baseNode.longitude - minMax[0]) * scaledCoordinates.scale + scaledCoordinates.padX;
		final double yi = (baseNode.latitude - minMax[2]) * scaledCoordinates.scale + scaledCoordinates.padY;
		final double xj = (adjNode.longitude - minMax[0]) * scaledCoordinates.scale + scaledCoordinates.padX;
		final double yj = (adjNode.latitude - minMax[2]) * scaledCoordinates.scale + scaledCoordinates.padY;
		g2d.draw(new Line2D.Double(xi, yi, xj, yj));
	}

	private double[] extractMinMax(Collection<Node> nodes) {
		double[] minMax = {Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE };

		for (Node node : nodes) {
			minMax[0] = Math.min(minMax[0], node.longitude);
			minMax[1] = Math.max(minMax[1], node.longitude);
			minMax[2] = Math.min(minMax[2], node.latitude);
			minMax[3] = Math.max(minMax[3], node.latitude);
		}

		return minMax;
	}

	private static class ScaledCoordinates {
		private int w;
		private int h;
		private double ratio;
		private double originalRatio;
		private double spreadY;
		private double spreadX;
		public double padX;
		public double padY;
		public double scale;

		public ScaledCoordinates(int w, int h, double ratio, double originalRatio, double spreadY, double spreadX) {
			this.w = w;
			this.h = h;
			this.ratio = ratio;
			this.originalRatio = originalRatio;
			this.spreadY = spreadY;
			this.spreadX = spreadX;
		}

		public double getPadX() {
			return padX;
		}

		public double getPadY() {
			return padY;
		}

		public double getScale() {
			return scale;
		}

		public ScaledCoordinates invoke() {
			if (originalRatio < ratio) {
				scale = h / spreadY;
				padX = (w - scale * spreadX) / 2 + MARGIN;
				padY = MARGIN;
			} else {
				scale = w / spreadX;
				padX = MARGIN;
				padY = (h - scale * spreadY) / 2 + MARGIN;
			}
			return this;
		}
	}
}
