package oSNRealistic.agent;


import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import oSNRealistic.ModelUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Agent {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Random r;
	private AgentState state = AgentState.SUSCEPTIBLE;
	
	private Queue<FeedMessage> feed;
	
	private double recoveryRate;
	private double vulnerability;
	private double sharingRate;
	
	private boolean isConvinced = false;
	
	protected int timeAccess;
	protected int tickCount;

	public Agent(ContinuousSpace<Object> space, Grid<Object> grid) { 
		this.space = space;
		this.grid = grid;
		this.feed = new PriorityQueue<FeedMessage>();
		r = new Random();
		this.recoveryRate = 0.2 + r.nextGaussian() * 0.04;
		this.vulnerability = 0.5 + r.nextGaussian() * 0.04;
		this.sharingRate = 0.5 + r.nextGaussian() * 0.04;
		this.timeAccess = 4;
		
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
	}

	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		tickCount++;
		if (tickCount % timeAccess == 0) {
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

				if (this.state != AgentState.BELIEVER && !isConvinced) {
					if (isAgentFactCheckerNow()) {
						this.state = AgentState.FACT_CHECKER;
					}
				}


				if (this.state == AgentState.BELIEVER) {
					shareMessage(FeedType.FAKE_NEWS);
				} else if (this.state == AgentState.FACT_CHECKER || isConvinced) {
					shareMessage(FeedType.DEBUNKING);
				}
			}

		}
		
		isConvinced = false;
		
	}
	
	private boolean isAgentFactCheckerNow() {
		double random = r.nextDouble();
		System.out.println("random to check with recovery rate " + this.recoveryRate + " is " + random);
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
		weight = net.getEdge(message.getCreator(), this).getWeight();
		
		double random = r.nextDouble();
		double vulnerability = this.vulnerability * k * weight;
		System.out.println("random to check with vulnerability " + vulnerability + " is " + random);
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
		while(targets.hasNext()) {
			tmp = (Agent) targets.next().getTarget();
			tmp.insertFeed(new FeedMessage(this, FeedType.FAKE_NEWS));
		}
	}
	
}
