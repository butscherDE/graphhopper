package com.graphhopper.routing;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeIteratorState;

import java.util.List;

public class PathMerge extends Path {

    public PathMerge(Graph graph, Weighting weighting) {
        super(graph, weighting);
    }

    @Override
    public Path extract() {
        return setFound(true);
    }

    public void addPath(final Path newPath) {
        if (this.edgeIds.size() > 0) {
            addIfThisPathIsntEmpty(newPath);
        } else {
            buildThisPathFromAnotherPath(newPath);
        }
    }

    private void addIfThisPathIsntEmpty(Path newPath) {
        final EdgeIteratorState lastEdgeOfThisPath = this.getFinalEdge();
        final List<EdgeIteratorState> otherPathsEdges = newPath.calcEdges();

        failOnNonAdablePath(lastEdgeOfThisPath, otherPathsEdges);
        mergePaths(newPath, otherPathsEdges);
    }

    private void mergePaths(Path newPath, List<EdgeIteratorState> otherPathsEdges) {
        addOtherPathsEdgesToThisPath(otherPathsEdges);
        this.weight += newPath.getWeight();
        this.distance += newPath.distance;
        this.endNode = newPath.endNode;
    }

    private void failOnNonAdablePath(EdgeIteratorState lastEdgeOfThisPath, List<EdgeIteratorState> otherPathsEdges) {
        if (!lastAndFirstNodeEqual(lastEdgeOfThisPath, otherPathsEdges)) {
            throw new IllegalArgumentException("Paths must end and start with equal node");
        }
    }

    private void addOtherPathsEdgesToThisPath(List<EdgeIteratorState> otherPathsEdges) {
        for (int i = 0; i < otherPathsEdges.size(); i++) {
            EdgeIteratorState edge = otherPathsEdges.get(i);
            this.addEdge(edge.getEdge());
        }
    }

    private boolean lastAndFirstNodeEqual(EdgeIteratorState lastEdgeOfThisPath, List<EdgeIteratorState> otherPathEdges) {
        return lastEdgeOfThisPath.getAdjNode() == otherPathEdges.get(0).getBaseNode();
    }
}
