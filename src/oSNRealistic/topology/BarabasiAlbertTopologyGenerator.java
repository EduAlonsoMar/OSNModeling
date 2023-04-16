package oSNRealistic.topology;

import java.util.ArrayList;
import java.util.Iterator;

import oSNRealistic.ModelUtils;
import oSNRealistic.agent.Agent;
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

	public BarabasiAlbertTopologyGenerator(Context<Object> context, ContinuousSpace<Object> space, Grid<Object> grid) {
		super(context, space, grid);
		
		// Create the net of our Online Social Network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object> ("OSN_network", context, false);
		net = netBuilder.buildNetwork();
	}
	
	@Override
	public void createTopology() {
		
		super.createTopology();
		

		
		int i;
		
		// Add the common agents to our model
		for (i = 0; i < ModelUtils.agents; i++) {
			context.add(new Agent());
			
		}
		
		// Add the bots to our model
		Agent bot;
		for (i=0; i<ModelUtils.bots; i++) {
			bot = new Agent();
			bot.convertToBot();
			context.add(bot);
			
		}
		
		addAgentsInCircleToSpace();
		
		// Move the agents and bots into the corresponding place in the grid
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			if (grid.moveTo(obj, (int)pt.getX(), (int)pt.getY())) {
				// System.out.println("Object moved in the grid");
			} else {
				// System.out.println("Objcet not moved in the grid");
			}
			
		}
		
		createEdgesInBarabasiAlbertAlgorithm(context.getObjects(Agent.class));
		i=0;
		Agent believer;
		Iterator<Object> agents = context.getRandomObjects(Agent.class, ModelUtils.numberOfInitialBeleivers).iterator();
		while (agents.hasNext()) {
			believer = (Agent) agents.next();
			believer.convertToBeliever();
		}
		
		Agent factChecker;
		Iterator<Object> agentsFC = context.getRandomObjects(Agent.class, ModelUtils.numberOfInitialFactCheckers).iterator();
		while (agentsFC.hasNext()) {
			factChecker = (Agent) agentsFC.next();
			factChecker.convertToFactChecker();
		}
	}
	
	
	private void createEdgesInBarabasiAlbertAlgorithm(IndexedIterable<Object> agentsInContext) {
		Iterator<Object> iterador = agentsInContext.iterator();
		Agent tmp, node;
		addFirstAgents(iterador);
		Iterator<Object> nodes;
		double pi;
		int nodeDegree;
		while (iterador.hasNext()) {
			tmp = (Agent) iterador.next();
			nodes = net.getNodes().iterator();
			nodeDegree = 0;
			while (nodes.hasNext() && nodeDegree < ModelUtils.nodeEdgesInBarabasi) {
				node = (Agent) nodes.next();
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
		// Agent initial = (Agent) agentsIncontext.next();
		ArrayList<Agent> INITIAL = new ArrayList<Agent>();
		// INITIAL.add(initial);
		Agent tmp;
		// TODO: Check how to create nodes initialy. Maybe from one to everyone.
		for (i = 0; i < ModelUtils.initialNodesInBarabasi; i++) {
			tmp = (Agent) agentsIncontext.next();
			INITIAL.add(tmp);
		}
		int j;
		Agent tmp2;
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
	
	private double calculateProbability(Agent node) {
		double ki = ((Integer) net.getDegree(node)).doubleValue();
		double kj = ((Integer) net.getDegree()).doubleValue();
		double pi = (ki/kj);
		// System.out.println("ki/kj = " + ki + "/" + kj + "= " + pi);
		
		// System.out.println("probability: " + pi);
		return pi;
	}

}
