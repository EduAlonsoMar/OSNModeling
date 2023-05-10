package oSNRealistic.topology;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	
	private int totalNodes;
	private boolean createInterestsConnections;
	private int numberOfInterests;
	private int numberOfInfluencers;
	private int nFollwersToBeInfluencer;
	private int nConnectionsPerBot;
	
	@SuppressWarnings("rawtypes")
	public ProximityComunitiesTopologyCreator(
			Context <Object> context, 
			ContinuousSpace<Object> space, 
			Grid<Object> grid, 
			Class agentClass,
			String convertToBotMethod,
			int totalNodes,
			boolean createInterests,
			int numberOfInterests,
			int numberOfInfluencers,
			int nFollowersToBeInfluencer,
			int nBots,
			int nConnectionPerBot) {
		super(context, space, grid, agentClass, convertToBotMethod, totalNodes, nBots);
		this.totalNodes = totalNodes;
		this.createInterestsConnections = createInterests;
		this.numberOfInterests = numberOfInterests;
		this.numberOfInfluencers = numberOfInfluencers;
		this.nFollwersToBeInfluencer = nFollowersToBeInfluencer;
		this.nConnectionsPerBot = nConnectionPerBot;
		
		// Create the net of our Online Social Network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object> ("OSN_network", context, true);
		this.net = netBuilder.buildNetwork();		
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void createTopology() {
		super.createTopology();
		
		int i;
		
		// Add the common agents to our model
		for (i = 0; i < this.totalNodes; i++) {
			try {
				context.add(this.agentClass.getDeclaredConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
		}
		
		addAgentsInRandomSpace();
		
		// Move the agents and bots into the corresponding place in the grid
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			if (grid.moveTo(obj, (int)pt.getX(), (int)pt.getY())) {
				// System.out.println("Object moved in the grid"); 
			} else {
				System.err.println("Objcet not moved in the grid");
			}
			
		}
		
		createNetEdgesByProximity(context.getObjects(Object.class));
		
		// Add the connections based in interests if necessary
		if (this.createInterestsConnections) {
			ArrayList<ArrayList<Object>> interests = new ArrayList<ArrayList<Object>>();
			// Adds as many lists of agents as the parameter says
			// The more high is the number of interests, less connections will be between users
			for (i = 0; i < this.numberOfInterests; i++) {
				interests.add(new ArrayList<Object>());
			}
			createInterestsForAgents(context.getObjects(Object.class), interests);
			createNetEdgestByInterest(interests);
		}
		
		// Add the influencers to our network		
		addInfluencers(context.getRandomObjects(Object.class, this.numberOfInfluencers).iterator(), this.totalNodes);
		
		// Connect the bots randomly to agents in the network
		addBots();	
		
	}
	
	/**
	 * Creates the interests for all agents in context.
	 * We are generating the interest randomly for each agent.
	 * @param agentsInContext
	 * @param interests
	 */
	private void createInterestsForAgents(IndexedIterable<Object> agentsInContext, ArrayList<ArrayList<Object>> interests) {
		
		Iterator<Object> iterador = agentsInContext.iterator();
		Object tmp;
		int interest;
		while (iterador.hasNext()) {
			tmp = iterador.next();
			interest = random.ints(0, this.numberOfInterests).findFirst().getAsInt();
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
		Object tmp;
		while (iterador.hasNext()) {
			tmp = iterador.next();
			GridPoint pt = grid.getLocation(tmp);
			if (pt == null) {
				System.out.println("Object not in grid");
			} else {


				// use the GridCellNgh class to create GridCells for
				// the surrounding neighborhood.
				GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, pt, Object.class, 1, 1);
				// import preast.simphony.query.space.grid.GridCell
				List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(true);
				SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
				for (GridCell<Object> cell : gridCells) {
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
	private void addEdgesToAgentsInCell(GridCell<Object> cell, Object node) {
		Iterator<Object> iterator = cell.items().iterator();
		while(iterator.hasNext()) {
			Object agent = iterator.next();
			net.addEdge(node, agent, calculateWeight(node, agent));
		}
	}
	
	private double calculateWeight(Object agent1, Object agent2) {
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
	private void createNetEdgestByInterest(ArrayList<ArrayList<Object>> interests) {
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
	private void createNetEdgeInList(Object agent, ArrayList<Object> list) {
		Iterator<Object> iterator = list.iterator();
		Object tmp;
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
		Object influencer;
		System.out.println("A total of " + this.nFollwersToBeInfluencer + " followers will be added to the influencers");
		Iterator<Object> iteratorAgents = context.getRandomObjects(Object.class, this.nFollwersToBeInfluencer).iterator();
		Object agentForConnection;
		while(influencers.hasNext()) {
			influencer = influencers.next();
			while(iteratorAgents.hasNext()) {
				agentForConnection = iteratorAgents.next();
				net.addEdge(influencer, agentForConnection, calculateWeight(influencer, agentForConnection));
			}
			
			
		}
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
				addBotConnections(bot);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | InstantiationException e) {
				e.printStackTrace();
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
	private void addBotConnections(Object bot) {
		Iterator<Object> randomAgents = context.getRandomObjects(Object.class, this.nConnectionsPerBot).iterator();
		
		while (randomAgents.hasNext()) {
			net.addEdge(bot, randomAgents.next(), 0.1);
		}
	}
}
