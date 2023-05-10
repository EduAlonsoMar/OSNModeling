package oSNRealistic.topology;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class TopologyGenerator {
	
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	@SuppressWarnings("rawtypes")
	private Class agentClass;
	private String methodToconvertToBot;
	private int totalNodes;
	private int nodesInBarabasi;
	private int initialNodesInBarabasi;
	private int nBots;
	private boolean createInterest;
	private int nOfInterest;
	private int nInfluencers;
	private int nFollowersTobeInfluencer;
	private int nBotsConnections;

	
	public TopologyGenerator (Context <Object> context, ContinuousSpace<Object> space, Grid<Object> grid) {
		this.context = context;
		this.space = space;
		this.grid = grid;
		
		
	}
	
	public void configure(
			Class agentClass, 
			String methodToConvertToBot, 
			int totalNodes, 
			int nodesInBarabasi, 
			int initialNodesInBarabasi,
			int nBots,
			boolean createInterest,
			int nOfInterests,
			int nInfluencers,
			int nFollowersTobeInfluencer,
			int nBotsConnections) {
		this.agentClass = agentClass;
		this.methodToconvertToBot = methodToConvertToBot;
		this.totalNodes = totalNodes;
		this.nodesInBarabasi = nodesInBarabasi;
		this.initialNodesInBarabasi = initialNodesInBarabasi;
		this.nBots = nBots;
		this.createInterest = createInterest;
		this.nOfInterest = nOfInterests;
		this.nInfluencers = nInfluencers;
		this.nFollowersTobeInfluencer = nFollowersTobeInfluencer;
		this.nBotsConnections = nBotsConnections;
	}
	
	
	public void generateSelectedTopology(String topoly) {
		System.out.println("Creating topology " + topoly);
		TopologyCreator creator;
		switch (topoly) {
		case "Barabasi-Albert":
			creator = new BarabasiAlbertTopologyGenerator(
					context, 
					space, 
					grid, 
					this.agentClass,
					this.methodToconvertToBot,
					this.nodesInBarabasi,
					this.nBots,
					this.totalNodes,
					this.initialNodesInBarabasi);
			break;
		case "default":
		default:
			creator = new ProximityComunitiesTopologyCreator(
					context, 
					space, 
					grid, 
					agentClass,
					this.methodToconvertToBot,
					this.totalNodes,
					this.createInterest,
					this.nOfInterest,
					this.nInfluencers,
					this.nFollowersTobeInfluencer,
					this.nBots,
					this.nBotsConnections);
			break;
		}
		
		creator.createTopology();
	}

}
