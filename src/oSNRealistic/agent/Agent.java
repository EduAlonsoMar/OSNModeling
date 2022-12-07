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
	
	private Queue<FeedType> feed;
	
	private double recoveryRate;
	private double vulnerability;

	public Agent(ContinuousSpace<Object> space, Grid<Object> grid) { 
		this.space = space;
		this.grid = grid;
		this.feed = new PriorityQueue<FeedType>();
		r = new Random();
		this.recoveryRate = 0.2 + r.nextGaussian() * 0.04;
		this.vulnerability = 0.5 + r.nextGaussian() * 0.04;
		
		if (r.nextDouble() < 0.25) {
			this.feed.add(FeedType.FAKE_NEWS);
		}
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
	
	public void insertFeed(FeedType message) {
		this.feed.add(message);
	}

	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		
		if (this.state == AgentState.FACT_CHECKER) {
			shareMessage(FeedType.DEBUNKING);
			return;
		}
		
		if (this.feed.isEmpty()) {
			if (this.state == AgentState.SUSCEPTIBLE) {
				if (isAgentFactCheckerNow()) {
					this.state = AgentState.FACT_CHECKER;
				}
			}
			
			if (this.state == AgentState.BELIEVER) {
				shareMessage(FeedType.FAKE_NEWS);
			}
			
			if (this.state == AgentState.FACT_CHECKER) {
				shareMessage(FeedType.DEBUNKING);
			}
		} else {
			FeedType message = this.feed.poll();
			if (isAgentConvinced()) {
				if (message == FeedType.FAKE_NEWS) { 
					this.state = AgentState.BELIEVER;
				} else {
					this.state = AgentState.FACT_CHECKER;
				}
			}
			
		}
		
	}
	
	private boolean isAgentFactCheckerNow() {
		return (r.nextDouble() < this.recoveryRate);
	}
	
	private boolean isAgentConvinced() {
		return (r.nextDouble() < this.vulnerability);
	}
	
	private void shareMessage(FeedType messageType) {
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext(this);
		@SuppressWarnings("unchecked")
		Network<Object> net = (Network<Object>) context.getProjection("OSN_network");
		
		Iterator<RepastEdge<Object>> targets = net.getOutEdges(this).iterator();
		Agent tmp;
		while(targets.hasNext()) {
			tmp = (Agent) targets.next().getTarget();
			tmp.insertFeed(messageType);
		}
	}
	
	public AgentState getState() {
		return this.state;
	}
	
}
