package oSNRealistic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import oSNRealistic.agent.Agent;
import oSNRealistic.agent.Bot;
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
	
	private ArrayList<ArrayList<Agent>> interests = new ArrayList<ArrayList<Agent>>();
	private static final int pFollowInterest = 1; 
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Network<Object> net;
	private Random random;

	@Override
	public Context build(Context <Object> context) { 
		context.setId("OSN");
		int i;

		random = new Random();
		
		System.out.println("Context set id done");
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object> ("OSN_network", context, true);
		net = netBuilder.buildNetwork();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", 
				context, 
				new RandomCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.StickyBorders(), 
				50, 50);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		
		grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new StickyBorders(),
						new SimpleGridAdder<Object>(),
						true, 50, 50));
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		ModelUtils.getParameters(params);
		
		for (i = 0; i < ModelUtils.agents; i++) {
			context.add(new Agent(space, grid));
			
		}
		System.out.println("Added " + ModelUtils.agents + " Agents" );
		
		for (i=0; i<ModelUtils.bots; i++) {
			context.add(new Bot(space, grid));
			
		}
		
		
		for (i = 0; i < ModelUtils.interests; i++) {
			interests.add(new ArrayList<Agent>());
		}
		System.out.println("Added " + ModelUtils.interests + " interests");
		
		
		System.out.println(ModelUtils.influencers + " influencers will be added");
		
		
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int)pt.getX(), (int)pt.getY());
			
		}
		
		createInterestsForAgents(context.getObjects(Agent.class));
		createNetEdgesByProximity(context.getObjects(Agent.class));
		createNetEdgestByInterest();
		addInfluencers(context.getRandomObjects(Agent.class, ModelUtils.influencers).iterator(), ModelUtils.agents, context);
		addBots(context.getObjects(Bot.class).iterator(), context);
		
		
		System.out.println("Saliendo");
		
		return context;
	
	}
	
	private void createInterestsForAgents(IndexedIterable<Object> agentsInContext) {
		
		Iterator<Object> iterador = agentsInContext.iterator();
		Agent tmp;
		int interest;
		while (iterador.hasNext()) {
			tmp = (Agent) iterador.next();
			interest = random.ints(0, 25).findFirst().getAsInt();
			interests.get(interest).add(tmp);
		}
	}
	
	private void createNetEdgesByProximity(IndexedIterable<Object> agentsInContext) {
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
				addEdgesToAgentsInCell(cell, tmp);
			}
		}
	}
	
	private void addEdgesToAgentsInCell(GridCell<Agent> cell, Agent node) {
		Iterator<Agent> iterator = cell.items().iterator();
		while(iterator.hasNext()) {
			Agent agent = iterator.next();
			net.addEdge(node, agent);
		}
	}
	
	private void createNetEdgestByInterest() {
		int i;
		int j;
		for(i=0; i<interests.size(); i++) {
			for (j=0; j<interests.get(i).size(); j++) {
				createNetEdgeInList(interests.get(i).get(j), interests.get(i));
			}
		}
		
	}
	
	private void createNetEdgeInList(Agent agent, ArrayList<Agent> list) {
		Iterator<Agent> iterator = list.iterator();
		Agent tmp;
		while(iterator.hasNext()) {
			tmp = iterator.next();
			if (agent != tmp && random.ints(0,500).findFirst().getAsInt() < pFollowInterest) {
				net.addEdge(agent, tmp);	
			}
		}
	}
	
	private void addInfluencers(Iterator<Object> influencers, int totalUsers, Context <Object> context) {
		Agent influencer;
		int i;
		System.out.println("A total of " + (totalUsers*5)/100 + " followers will be added to the influencers");
		while(influencers.hasNext()) {
			influencer = (Agent) influencers.next();
			for(i=0; i<((totalUsers*5)/100); i++) {
				net.addEdge(influencer, context.getRandomObject());
			}
			
		}
	}
	
	private void addBots(Iterator<Object> bots, Context<Object> context) {
		Bot tmp;
		
		while (bots.hasNext()) {
			tmp = (Bot) bots.next();
			addBotConnections(tmp, context);
		}
	}
	
	private void addBotConnections(Bot bot, Context<Object> context) {
		Iterator<Object> randomAgents = context.getRandomObjects(Agent.class, (ModelUtils.agents * 2 / 100)).iterator();
		
		while (randomAgents.hasNext()) {
			net.addEdge(bot, (Agent) randomAgents.next());
		}
	}
}
