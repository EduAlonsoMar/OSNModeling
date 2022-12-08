package oSNRealistic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import oSNRealistic.agent.Agent;
import oSNRealistic.agent.Bot;
import oSNRealistic.agent.FeedType;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StickyBorders;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.IndexedIterable;

public class OSNRealisticBuilder implements ContextBuilder<Object> {
	
	private static final int pFollowInterest = 10; 
	private Random random;

	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context <Object> context) { 
		context.setId("OSN");
		
		int i;
		// Initialize the random object to work with.	
		random = new Random();
		
		// Create the net of our Online Social Network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object> ("OSN_network", context, true);
		Network<Object> net = netBuilder.buildNetwork();
		
		// Create the space in which our network is going to be contained
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", 
				context, 
				new RandomCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.StickyBorders(), 
				50, 50);
		
		// Create the grid in which our agents are going to be moved
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new StickyBorders(),
						new SimpleGridAdder<Object>(),
						true, 50, 50));
		
		// Get the parameters for our simulation
		Parameters params = RunEnvironment.getInstance().getParameters();
		ModelUtils.getParameters(params);
		
		// Add the common agents to our model
		for (i = 0; i < ModelUtils.agents; i++) {
			context.add(new Agent(space, grid));
			
		}
		
		// Add the bots to our model
		for (i=0; i<ModelUtils.bots; i++) {
			context.add(new Bot(space, grid));
			
		}
		
		// Move the agents and bots into the corresponding place in the grid
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
			
		}
		
		// Create the OSN connections based on proximity
		createNetEdgesByProximity(context.getObjects(Agent.class), grid, net);
		
		// Add the connections based in interests if necessary
		if (ModelUtils.createInterestsConnections) {
			ArrayList<ArrayList<Agent>> interests = new ArrayList<ArrayList<Agent>>();
			// Adds as many lists of agents as the parameter says
			// The more high is the number of interests, less connections will be between users
			for (i = 0; i < ModelUtils.interests; i++) {
				interests.add(new ArrayList<Agent>());
			}
			createInterestsForAgents(context.getObjects(Agent.class), interests);
			createNetEdgestByInterest(net, interests);
		}
		
		// Add the influencers to our network		
		addInfluencers(context.getRandomObjects(Agent.class, ModelUtils.influencers).iterator(), ModelUtils.agents, context, net);
		
		// Connect the bots randomly to agents in the network
		addBots(context.getObjects(Bot.class).iterator(), context, net);
		
		Agent firstInfected = (Agent) context.getRandomObjects(Agent.class, 1).iterator().next();
		firstInfected.insertFeed(FeedType.FAKE_NEWS);
		
		
		return context;
	
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
	private void createNetEdgesByProximity(IndexedIterable<Object> agentsInContext, Grid<Object> grid, Network<Object> net) {
		Iterator<Object> iterador = agentsInContext.iterator();
		Agent tmp;
		while (iterador.hasNext()) {
			tmp = (Agent) iterador.next();
			GridPoint pt = grid.getLocation(tmp);
			// use the GridCellNgh class to create GridCells for
			// the surrounding neighborhood.
			GridCellNgh<Agent> nghCreator = new GridCellNgh<Agent>(grid, pt, Agent.class, 1, 1);
			// import preast.simphony.query.space.grid.GridCell
			List<GridCell<Agent>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			for (GridCell<Agent> cell : gridCells) {
				addEdgesToAgentsInCell(cell, tmp, net);
			}
		}
	}
	
	/**
	 * Adds edges in the network from one agent to all the agents in the node.
	 * @param cell
	 * @param node
	 * @param net
	 */
	private void addEdgesToAgentsInCell(GridCell<Agent> cell, Agent node, Network<Object> net) {
		Iterator<Agent> iterator = cell.items().iterator();
		while(iterator.hasNext()) {
			Agent agent = iterator.next();
			net.addEdge(node, agent);
		}
	}
	
	/**
	 * Creates the edges between nodes with the same interest.
	 * @param net
	 * @param interests
	 */
	private void createNetEdgestByInterest(Network<Object> net, ArrayList<ArrayList<Agent>> interests) {
		int i;
		int j;
		for(i=0; i<interests.size(); i++) {
			for (j=0; j<interests.get(i).size(); j++) {
				createNetEdgeInList(interests.get(i).get(j), interests.get(i), net);
			}
		}
		
	}
	
	/**
	 * Creates an edge between an user and a list of users.
	 * @param agent
	 * @param list
	 * @param net
	 */
	private void createNetEdgeInList(Agent agent, ArrayList<Agent> list, Network<Object> net) {
		Iterator<Agent> iterator = list.iterator();
		Agent tmp;
		while(iterator.hasNext()) {
			tmp = iterator.next();
			if (agent != tmp && random.ints(0,500).findFirst().getAsInt() < pFollowInterest) {
				net.addEdge(agent, tmp);	
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
	private void addInfluencers(Iterator<Object> influencers, int totalUsers, Context <Object> context, Network<Object> net) {
		Agent influencer;
		int i;
		System.out.println("A total of " + ModelUtils.nFollowersToBeInfluencer + " followers will be added to the influencers");
		while(influencers.hasNext()) {
			influencer = (Agent) influencers.next();
			for(i=0; i<ModelUtils.nFollowersToBeInfluencer; i++) {
				net.addEdge(influencer, context.getRandomObject());
			}
			
		}
	}
	
	/**
	 * Adds the bots to the network.
	 * @param bots
	 * @param context
	 * @param net
	 */
	private void addBots(Iterator<Object> bots, Context<Object> context, Network<Object> net) {
		Bot tmp;
		
		while (bots.hasNext()) {
			tmp = (Bot) bots.next();
			addBotConnections(tmp, context, net);
		}
	}
	
	/**
	 * Adds the connections for a bot in the network. Gets a random list of the population 
	 * that needs to be connected to a bot. 
	 * @param bot
	 * @param context
	 * @param net
	 */
	private void addBotConnections(Bot bot, Context<Object> context, Network<Object> net) {
		Iterator<Object> randomAgents = context.getRandomObjects(Agent.class, ModelUtils.nConnectionsPerBot).iterator();
		
		while (randomAgents.hasNext()) {
			net.addEdge(bot, (Agent) randomAgents.next());
		}
	}
}
