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
	
	public static void getParameters(Parameters params) {
		agents = params.getInteger("agents");
		interests = params.getInteger("dif_interests");
		influencers = params.getInteger("influencers");
		bots = params.getInteger("bots");
		nFollowersToBeInfluencer = (agents * 5)/100;
		createInterestsConnections = params.getBoolean("add_interests_connections");
		workWithTimeDynamics = params.getBoolean("work_with_time_dynamics");
		nConnectionsPerBot = (agents * 2) / 100;
	}
}
