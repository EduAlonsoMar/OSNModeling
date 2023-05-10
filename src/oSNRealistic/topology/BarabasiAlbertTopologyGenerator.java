package oSNRealistic.topology;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.collections.IndexedIterable;

public class BarabasiAlbertTopologyGenerator extends TopologyCreator {
	
	private Network<Object> net;
	private int numberOfNodeEdges;
	private int numberOfInitialNodes;

	@SuppressWarnings("rawtypes")
	public BarabasiAlbertTopologyGenerator(
			Context<Object> context, 
			ContinuousSpace<Object> space, 
			Grid<Object> grid, 
			Class agentClass,
			String convertToBotMethod,
			int nodeEdges,
			int totalBots,
			int totalNodes,
			int initialNodes) {
		super(context, space, grid, agentClass, convertToBotMethod, totalNodes, totalBots);
		
		this.numberOfNodeEdges = nodeEdges;
		this.numberOfInitialNodes = initialNodes;
		
		// Create the net of our Online Social Network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object> ("OSN_network", context, false);
		net = netBuilder.buildNetwork();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void createTopology() {
		
		super.createTopology();
		
		int i;
		
		// Add the common agents to our model
		for (i = 0; i < this.totalNodes; i++) {
			try {
				context.add(agentClass.getDeclaredConstructor().newInstance());
			} catch (InstantiationException e) {
				System.err.println("Instationation exception while creating agent class");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				System.err.println("IllegalAccessException while creating agent calss");
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			
		}
		
		addAgentsInCircleToSpace();
		addBots();
		
		
		// Move the agents and bots into the corresponding place in the grid
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			if (pt != null) {
				if (grid.moveTo(obj, (int)pt.getX(), (int)pt.getY())) {
					System.out.println("Object moved in the grid");
				} else {
					System.out.println("Objcet not moved in the grid");
				}	
			}
			
			
		}
		
		createEdgesInBarabasiAlbertAlgorithm(context.getObjects(Object.class));

	}
	
	@SuppressWarnings({ "unchecked" })
	private void addBots() {
		// Add the bots to our model
		Object bot;
		int i;
		for (i=0; i < this.numberOfBots; i++) {
			try {
				bot = this.agentClass.getDeclaredConstructor().newInstance();
				this.agentClass.getDeclaredMethod(this.convertToBotMethod).invoke(bot);
				context.add(bot);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void createEdgesInBarabasiAlbertAlgorithm(IndexedIterable<Object> agentsInContext) {
		Iterator<Object> iterador = agentsInContext.iterator();
		Object tmp, node;
		addFirstAgents(iterador);
		Iterator<Object> nodes;
		double pi;
		int nodeDegree;
		while (iterador.hasNext()) {
			tmp = iterador.next();
			nodes = net.getNodes().iterator();
			nodeDegree = 0;
			while (nodes.hasNext() && nodeDegree < this.numberOfNodeEdges) {
				node = nodes.next();
				pi = calculateProbability(node);
				double randomNumber = randomNumberBetweenMargins(0, 1);
				// System.out.println("Random prob. Number: " + randomNumber);
				if (randomNumber <= pi) {
					net.addEdge(node, tmp);
					nodeDegree++;
				}
			}
			// System.out.println("Added " + nodeDegree + " edges to the net");
		}
	}
	
	private void addFirstAgents(Iterator<Object> agentsIncontext) {
		int i = 1;
		ArrayList<Object> INITIAL = new ArrayList<Object>();
		Object tmp;
		for (i = 0; i < this.numberOfInitialNodes; i++) {
			tmp = agentsIncontext.next();
			INITIAL.add(tmp);
		}
		int j;
		Object tmp2;
		for (i = 0; i < INITIAL.size(); i++) {
			tmp = INITIAL.get(i);
			for (j = 0; j < INITIAL.size(); j++) {
				if (i != j) {
					tmp2 = INITIAL.get(j);
					RepastEdge<Object> edge = new RepastEdge<Object>(tmp, tmp2, true);
					if (!net.containsEdge(edge)) {
						net.addEdge(edge);
					}
				}
			}
		}
	}
	
	private double calculateProbability(Object node) {
		double ki = ((Integer) net.getDegree(node)).doubleValue();
		double kj = ((Integer) net.getDegree()).doubleValue();
		double pi = (ki/kj);
		// System.out.println("ki/kj = " + ki + "/" + kj + "= " + pi);
		
		// System.out.println("probability: " + pi);
		return pi;
	}

}
