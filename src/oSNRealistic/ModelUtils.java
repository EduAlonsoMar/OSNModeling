package oSNRealistic;

import repast.simphony.parameter.Parameters;

public class ModelUtils {

	public static int agents;
	public static int interests;
	public static int influencers;
	public static int bots;
	
	public static void getParameters(Parameters params) {
		agents = params.getInteger("agents");
		interests = params.getInteger("dif_interests");
		influencers = params.getInteger("influencers");
		bots = params.getInteger("bots");
	}
}
