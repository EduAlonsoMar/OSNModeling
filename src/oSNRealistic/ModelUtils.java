package oSNRealistic;

import repast.simphony.parameter.Parameters;

public class ModelUtils {

	public static int agents;
	public static int interests;
	public static boolean createInterestsConnections;
	public static int influencers;
	public static int bots;
	public static int nFollowersToBeInfluencer;
	public static int nConnectionsPerBot;
	public static boolean workWithTimeDynamics;
	public static int timeAccessForCommonUsers;
	public static int timeAccessForBots;
	public static double degreesSeparation;
	public static String selectedTopology;
	public static int spaceXSize = 50;
	public static int spaceYSize = 50;
	public static int initialNodesInBarabasi;
	public static int nodeEdgesInBarabasi;
	public static boolean fackCheckersConversion;
	public static int numberOfInitialBeleivers;
	public static int numberOfInitialFactCheckers;
	public static double vulnerabilityMean;
	public static double recoveryMean;
	public static double sharingMean;
	public static int numberOfTicks;
	public static double sharingDebunking;
	public static int ticksToStartLosingInterest;
	
	
	public static int configId;
	
	
	
	public static void getParameters(Parameters params) {
		agents = params.getInteger("agents");
		interests = params.getInteger("dif_interests");
		influencers = params.getInteger("influencers");
		bots = params.getInteger("bots");
		nFollowersToBeInfluencer = (agents * 5)/100;
		createInterestsConnections = params.getBoolean("add_interests_connections");
		fackCheckersConversion =  params.getBoolean("factcheckers_conversion");
		workWithTimeDynamics = params.getBoolean("work_with_time_dynamics");
		nConnectionsPerBot = (agents * 2) / 100;
		timeAccessForCommonUsers = params.getInteger("time_access_for_users");
		timeAccessForBots = params.getInteger("time_access_for_bots");
		degreesSeparation = 360.0 / agents;
		selectedTopology = params.getString("topology");
		initialNodesInBarabasi = params.getInteger("initial_nodes_in_barabasi");
		nodeEdgesInBarabasi = params.getInteger("node_edges_in_barabasi");
		numberOfInitialBeleivers = params.getInteger("initial_believers");
		numberOfInitialFactCheckers = params.getInteger("initial_fact_checkers");
		vulnerabilityMean = params.getDouble("vulnerability_mean");
		recoveryMean = params.getDouble("recovery_mean");
		sharingMean = params.getDouble("sharing_mean");
		numberOfTicks = params.getInteger("number_steps");
		sharingDebunking = params.getDouble("sharing_debunking");
		ticksToStartLosingInterest = params.getInteger("ticks_lose_interest");
		
		
	}
}
