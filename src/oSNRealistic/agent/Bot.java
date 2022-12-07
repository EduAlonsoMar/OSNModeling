package oSNRealistic.agent;

import java.util.Iterator;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Bot extends Agent {

	public Bot(ContinuousSpace<Object> space, Grid<Object> grid) {
		super(space, grid);
	}

	public boolean isBoot() {
		return true;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	@Override
	public void step() {
		
		
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext(this);
		@SuppressWarnings("unchecked")
		Network<Object> net = (Network<Object>) context.getProjection("OSN_network");
		
		spreadFakeNews(net.getOutEdges(this).iterator());
		
		
	}
	
	private void spreadFakeNews(Iterator<RepastEdge<Object>> targets) {
		Agent tmp;
		while(targets.hasNext()) {
			tmp = (Agent) targets.next().getTarget();
			tmp.insertFeed(FeedType.FAKE_NEWS);
		}
	}
}
