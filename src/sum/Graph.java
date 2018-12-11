package sum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class Graph<T> {
    private Map<T, Node> nodes;

    public Graph() {
	nodes = new HashMap<T, Node>();
    }

    public Graph(int capacity) {
	nodes = new HashMap<T, Node>(capacity);
    }

    /**
     * Adds a given node into the graph
     * 
     * @param key
     *            the key for the node
     * @param value
     *            the value of the node
     * @return
     */
    public Node add(T key, Node value) {
	return nodes.put(key, value);
    }

    /**
     * Gets a given node from the graph
     * 
     * @param key
     *            the key for the node
     * @return the value associated with the given key
     */
    public Node get(T key) {
	return nodes.get(key);
    }

    /**
     * Links the nodes within the graph with each other
     */
    @SuppressWarnings("unchecked")
    public void linkNodes() {
	Node[] n = (Node[]) nodes.values().toArray();
	for (int i = 0; i < n.length; i++) {
	    Node n1 = n[i];
	    for (int j = i + 1; j < n.length; j++) {
		Node n2 = n[j];
		link(n1, n2);
	    }
	}
    }

    /**
     * Link two nodes within the graph
     * 
     * @param n1
     *            the first node to link
     * @param n2
     *            the second node to link
     */
    public void link(Node n1, Node n2) {
	double relation = n1.calculateRelationScore(n2);
	if (relation > 0.0) {
	    n1.addNeighbor(n2, relation);
	    n2.addNeighbor(n1, relation);
	}
    }

    /**
     * @return the graph as a stream of Nodes
     */
    public Stream<Node> getNodeStream() {
	return nodes.values().stream();
    }

    public double getThreshold(int pos) {
	return getRankedNodes().limit(pos).toArray(Graph.Node[]::new)[pos >= nodes
		.size() ? nodes.size() - 1 : pos - 1].getRank();
    }

    /**
     * Performs an action on each node in the graph
     * 
     * @param action
     *            the action to perform
     */
    public void forEach(BiConsumer<T, Node> action) {
	nodes.forEach(action);
    }

    /**
     * @return Stream of Nodes in ranked order
     */
    public Stream<Node> getRankedNodes() {
	return getNodeStream().sorted();
    }

    public class Edge {
	private double weight;
	private Node target;

	public Edge(Node t, double w) {
	    target = t;
	    weight = w;
	}

	/**
	 * @return the weight of this edge
	 */
	public double getWeight() {
	    return weight;
	}

	/**
	 * @return the target of this edge
	 */
	public Node getTarget() {
	    return target;
	}
    }

    public abstract class Node implements Comparable<Node> {
	protected Set<Edge> neighbors;
	protected final T content;
	protected double rank = 1.0;

	public Node(T c) {
	    neighbors = new HashSet<>();
	    content = c;
	}

	/**
	 * @return the content of this node
	 */
	public T getContent() {
	    return content;
	}

	/**
	 * @param n
	 *            the node to associate with this node
	 * @param weight
	 *            weight of the edge
	 */
	public void addNeighbor(Node n, double weight) {
	    neighbors.add(new Edge(n, weight));
	}

	/**
	 * @param n
	 *            other node
	 * @return is this node a neighbor of the other node
	 */
	public boolean hasNeighbor(Node n) {
	    return neighbors.contains(n);
	}

	/**
	 * @return the rank of this node
	 */
	public double getRank() {
	    return rank;
	}

	/**
	 * @return a set of edges of the neighbors of this node
	 */
	public Set<Edge> getNeighbors() {
	    return neighbors;
	}

	@Override
	public int compareTo(Node n) {
	    return Double.compare(n.rank, rank);
	}

	@Override
	public String toString() {
	    return rank + ": " + content;
	}

	public abstract boolean calculateRank();

	public abstract double calculateRelationScore(Node n);
    }
}
