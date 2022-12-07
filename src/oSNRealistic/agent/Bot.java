package oSNRealistic.agent;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Bot extends Agent {

	public Bot(ContinuousSpace<Object> space, Grid<Object> grid) {
		super(space, grid);
		// TODO Auto-generated constructor stub
	}

	
	@ScheduledMethod(start = 1, interval = 1)
	@Override
	public void step() {
		System.out.println("Executing step in bot");
		
		
		
	}
}
