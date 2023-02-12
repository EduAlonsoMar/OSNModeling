package oSNRealistic.agent;


import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import oSNRealistic.ModelUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;

public class Agent {
	
	private Random r;
	private AgentState state = AgentState.SUSCEPTIBLE;
	
	private Queue<FeedMessage> feed;
	
	private double recoveryRate;
	private double vulnerability;
	private double sharingRate;
	
	protected int timeAccess;
	protected int tickCount;

	public Agent() { 
		this.feed = new PriorityQueue<FeedMessage>();
		r = new Random();
		this.recoveryRate = ModelUtils.recoveryMean + r.nextGaussian() * ModelUtils.recoveryVariance;
		this.vulnerability = ModelUtils.vulnerabilityMean + r.nextGaussian() * ModelUtils.vulnerabilityVariance;
		this.sharingRate = ModelUtils.sharingMean + r.nextGaussian() * ModelUtils.sharingVariance;
		if (ModelUtils.workWithTimeDynamics) {
			this.timeAccess = ModelUtils.timeAccessForCommonUsers;	
		}
		
		
		this.tickCount = 0;
		
	}
	
	public boolean isInfluencer() {
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext(this);
		@SuppressWarnings("unchecked")
		Network<Object> net = (Network<Object>)context.getProjection("OSN_network");
		
		Iterable<RepastEdge<Object>> iterable = net.getOutEdges(this);
		Iterator<RepastEdge<Object>> iterator = iterable.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		
		return count > ModelUtils.nFollowersToBeInfluencer;
	}
	
	public boolean isBeliever() {
		return this.state == AgentState.BELIEVER;
	}
	
	public boolean isSusceptible() {
		return this.state == AgentState.SUSCEPTIBLE;
	}
	
	public boolean isFactChecker() {
		return this.state == AgentState.FACT_CHECKER;
	}
	
	public boolean isBot() {
		return this.state == AgentState.BOT;
	}
	
	public void insertFeed(FeedMessage message) {
		this.feed.add(message);
	}
	
	public void convertToBeliever() {
		this.state = AgentState.BELIEVER;
	}
	
	public void convertToBot() {
		this.state = AgentState.BOT;
		this.timeAccess = ModelUtils.timeAccessForBots;
	}

	private void executeAgentStep() {
		if (this.state == AgentState.BOT){
			@SuppressWarnings("unchecked")
			Context<Object> context = ContextUtils.getContext(this);
			@SuppressWarnings("unchecked")
			Network<Object> net = (Network<Object>) context.getProjection("OSN_network");
			shareFakeNewsAsBot(net.getOutEdges(this).iterator());
			
		}
		
		if (this.state == AgentState.FACT_CHECKER) {
			shareMessage(FeedType.DEBUNKING);
		}

		if (this.state == AgentState.BELIEVER || this.state == AgentState.SUSCEPTIBLE) {
			int i = 0;
			while (!this.feed.isEmpty()) {
				FeedMessage message = this.feed.poll();
				if (isAgentConvinced(message)) {
					if (message.getType() == FeedType.FAKE_NEWS) { 
						i++;
					} else {
						i--;
					}
				}
			}

			if (i < 0) {
				this.state = AgentState.FACT_CHECKER;
			} else if (i>0) {
				this.state = AgentState.BELIEVER;
			}

			if (this.state != AgentState.BELIEVER) {
				if (ModelUtils.fackCheckersConversion && isAgentFactCheckerNow()) {
					this.state = AgentState.FACT_CHECKER;
				}
			}


			if (this.state == AgentState.BELIEVER) {
				shareMessage(FeedType.FAKE_NEWS);
			} else if (this.state == AgentState.FACT_CHECKER) {
				shareMessage(FeedType.DEBUNKING);
			}
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		tickCount++;
		if (ModelUtils.workWithTimeDynamics && tickCount % timeAccess == 0) {
			executeAgentStep();
		} else if (!ModelUtils.workWithTimeDynamics) {
			executeAgentStep();
		}
		
	}
	
	private boolean isAgentFactCheckerNow() {
		double random = r.nextDouble();
		// System.out.println("random to check with recovery rate " + this.recoveryRate + " is " + random);
		return (random < this.recoveryRate);
	}
	
	@SuppressWarnings("unchecked")
	private boolean isAgentConvinced(FeedMessage message) {
		double k;
		double weight;
		if (message.getType() == FeedType.FAKE_NEWS) {
			k = 1.0;
		} else {
			k = 0.1;
		}
		
		Context<Object> context = ContextUtils.getContext(this);
		Network<Object> net = (Network<Object>) context.getProjection("OSN_network");
		
		RepastEdge<Object> edge = net.getEdge(message.getCreator(), this);
		if (edge == null) {
			edge = net.getEdge(this, message.getCreator());
		}
		
		if (edge != null) {
			weight = net.getEdge(message.getCreator(), this).getWeight();	
		} else {
			weight = 1;
		}
		
		
		double random = r.nextDouble();
		double vulnerability = this.vulnerability * k * weight;
		// System.out.println("random to check with vulnerability " + vulnerability + " is " + random + " " + weight);
		return (random < vulnerability);
	}
	
	@SuppressWarnings("unchecked")
	private void shareMessage(FeedType messageType) {
		
		Context<Object> context = ContextUtils.getContext(this);
		Network<Object> net = (Network<Object>) context.getProjection("OSN_network");
		
		Iterator<RepastEdge<Object>> targets = net.getOutEdges(this).iterator();
		Agent tmp;
		while(targets.hasNext()) {
			tmp = (Agent) targets.next().getTarget();
			if (r.nextDouble() < this.sharingRate) {
				tmp.insertFeed(new FeedMessage(this, messageType));	
			}
			
		}
	}
	
	public AgentState getState() {
		return this.state;
	}
	
	private void shareFakeNewsAsBot(Iterator<RepastEdge<Object>> targets) {
		Agent tmp;
		RepastEdge<Object> edge;
		while(targets.hasNext()) {
			edge = targets.next();
			tmp = (Agent) edge.getTarget();
			if (tmp.equals(this)) {
				tmp = (Agent) edge.getSource();
			} 
			tmp.insertFeed(new FeedMessage(this, FeedType.FAKE_NEWS));
		}
	}
	
}
