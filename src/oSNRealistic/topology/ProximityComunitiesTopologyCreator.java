package oSNRealistic.topology;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oSNRealistic.ModelUtils;
import oSNRealistic.agent.Agent;
import oSNRealistic.agent.AgentState;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.IndexedIterable;

public class ProximityComunitiesTopologyCreator extends TopologyCreator{
	
	private static final int pFollowInterest = 10;
	private Network<Object> net;
	
	public ProximityComunitiesTopologyCreator(Context <Object> context, ContinuousSpace<Object> space, Grid<Object> grid) {
		super(context, space, grid);
		
		// Create the net of our Online Social Network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object> ("OSN_network", context, true);
		this.net = netBuilder.buildNetwork();		
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
		
		addAgentsInRandomSpace();
		
		// Move the agents and bots into the corresponding place in the grid
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			if (grid.moveTo(obj, (int)pt.getX(), (int)pt.getY())) {
				// System.out.println("Object moved in the grid"); 
			} else {
				System.out.println("Objcet not moved in the grid");
			}
			
		}
		
		createNetEdgesByProximity(context.getObjects(Agent.class));
		
		// Add the connections based in interests if necessary
		if (ModelUtils.createInterestsConnections) {
			ArrayList<ArrayList<Agent>> interests = new ArrayList<ArrayList<Agent>>();
			// Adds as many lists of agents as the parameter says
			// The more high is the number of interests, less connections will be between users
			for (i = 0; i < ModelUtils.interests; i++) {
				interests.add(new ArrayList<Agent>());
			}
			createInterestsForAgents(context.getObjects(Agent.class), interests);
			createNetEdgestByInterest(interests);
		}
		
		// Add the influencers to our network		
		addInfluencers(context.getRandomObjects(Agent.class, ModelUtils.influencers).iterator(), ModelUtils.agents);
		
		// Connect the bots randomly to agents in the network
		addBots(context.getObjects(Agent.class).iterator());
		
		Iterator<Object> iterator = context.getRandomObjects(Agent.class, ModelUtils.numberOfInitialBeleivers).iterator();
		Agent firstInfected;
		while (iterator.hasNext()) {
			firstInfected = (Agent) iterator.next();
			firstInfected.convertToBeliever();
		}	
		
	}
	
	/**
	 * Creates the interests for all agents in context.
	 * We are generating the interest randomly for each agent.
	 * @param agentsInContext
	 * @param interests
	 */
	private void createInterestsForAgents(IndexedIterable<Object> agentsInContext, ArrayList<ArrayList<Agent>> interests) {
		
		Iterator<Object> iterador = agentsInContext.iterator();
		Agent tmp;
		int interest;
		while (iterador.hasNext()) {
			tmp = (Agent) iterador.next();
			interest = random.ints(0, ModelUtils.interests).findFirst().getAsInt();
			interests.get(interest).add(tmp);
		}
	}
	
	/**
	 * Creates the links between nodes that are geographically closed.
	 * @param agentsInContext
	 * @param grid
	 * @param net
	 */
	private void createNetEdgesByProximity(IndexedIterable<Object> agentsInContext) {
		Iterator<Object> iterador = agentsInContext.iterator();
		Agent tmp;
		while (iterador.hasNext()) {
			tmp = (Agent) iterador.next();
			GridPoint pt = grid.getLocation(tmp);
			if (pt == null) {
				System.out.println("Object not in grid");
			} else {


				// use the GridCellNgh class to create GridCells for
				// the surrounding neighborhood.
				GridCellNgh<Agent> nghCreator = new GridCellNgh<Agent>(grid, pt, Agent.class, 1, 1);
				// import preast.simphony.query.space.grid.GridCell
				List<GridCell<Agent>> gridCells = nghCreator.getNeighborhood(true);
				SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
				for (GridCell<Agent> cell : gridCells) {
					addEdgesToAgentsInCell(cell, tmp);
				}
			}
		}
	}
	
	/**
	 * Adds edges in the network from one agent to all the agents in the node.
	 * @param cell
	 * @param node
	 * @param net
	 */
	private void addEdgesToAgentsInCell(GridCell<Agent> cell, Agent node) {
		Iterator<Agent> iterator = cell.items().iterator();
		while(iterator.hasNext()) {
			Agent agent = iterator.next();
			net.addEdge(node, agent, calculateWeight(node, agent));
		}
	}
	
	private double calculateWeight(Agent agent1, Agent agent2) {
		GridPoint pt1 = grid.getLocation(agent1);
		GridPoint pt2 = grid.getLocation(agent2);
		
		double ac = Math.abs(pt2.getY() - pt1.getY());
		double cb = Math.abs(pt2.getX() - pt1.getX());
		
		double distance = (Math.hypot(ac, cb)/Math.sqrt(2.0))/100;
		
		return (1-distance);
	}
	
	/**
	 * Creates the edges between nodes with the same interest.
	 * @param net
	 * @param interests
	 */
	private void createNetEdgestByInterest(ArrayList<ArrayList<Agent>> interests) {
		int i;
		int j;
		for(i=0; i<interests.size(); i++) {
			for (j=0; j<interests.get(i).size(); j++) {
				createNetEdgeInList(interests.get(i).get(j), interests.get(i));
			}
		}
		
	}
	
	/**
	 * Creates an edge between an user and a list of users.
	 * @param agent
	 * @param list
	 * @param net
	 */
	private void createNetEdgeInList(Agent agent, ArrayList<Agent> list) {
		Iterator<Agent> iterator = list.iterator();
		Agent tmp;
		while(iterator.hasNext()) {
			tmp = iterator.next();
			if (agent != tmp && random.ints(0,500).findFirst().getAsInt() < pFollowInterest) {
				net.addEdge(agent, tmp, calculateWeight(agent, tmp));	
			}
		}
	}
	
	/**
	 * Adds the influencers to the net. Receives a list of users that will be the influencers
	 * and generates as much as out edges as needed to become an influencer.
	 * @param influencers
	 * @param totalUsers
	 * @param context
	 * @param net
	 */
	private void addInfluencers(Iterator<Object> influencers, int totalUsers) {
		Agent influencer;
		System.out.println("A total of " + ModelUtils.nFollowersToBeInfluencer + " followers will be added to the influencers");
		Iterator<Object> iteratorAgents = context.getRandomObjects(Agent.class, ModelUtils.nFollowersToBeInfluencer).iterator();
		Agent agentForConnection;
		while(influencers.hasNext()) {
			influencer = (Agent) influencers.next();
			while(iteratorAgents.hasNext()) {
				agentForConnection = (Agent) iteratorAgents.next();
				net.addEdge(influencer, agentForConnection, calculateWeight(influencer, agentForConnection));
			}
			
			
		}
	}
	
	/**
	 * Adds the bots to the network.
	 * @param bots
	 * @param context
	 * @param net
	 */
	private void addBots(Iterator<Object> bots) {
		Agent tmp;
		
		while (bots.hasNext()) {
			tmp = (Agent) bots.next();
			if (tmp.getState() == AgentState.BOT) {
				addBotConnections(tmp);	
			}
			
		}
	}
	
	/**
	 * Adds the connections for a bot in the network. Gets a random list of the population 
	 * that needs to be connected to a bot. 
	 * @param bot
	 * @param context
	 * @param net
	 */
	private void addBotConnections(Agent bot) {
		Iterator<Object> randomAgents = context.getRandomObjects(Agent.class, ModelUtils.nConnectionsPerBot).iterator();
		
		while (randomAgents.hasNext()) {
			net.addEdge(bot, (Agent) randomAgents.next(), 0.1);
		}
	}
}
