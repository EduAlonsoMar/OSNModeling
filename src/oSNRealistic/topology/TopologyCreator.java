package oSNRealistic.topology;

import java.util.Random;

import oSNRealistic.ModelUtils;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class TopologyCreator {

	protected Context <Object> context;
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	protected Random random;
	
	public TopologyCreator(Context <Object> context, ContinuousSpace<Object> space, Grid<Object> grid) {
		this.context = context;
		this.space = space;
		this.grid = grid;
		
		// Initialize the random object to work with.	
		random = new Random();
	}
	
	protected void addAgentsInCircleToSpace() {
		double degreesCount;
		double teta;
		double iteration = 0;
		double x;
		double y;
		// Move the objects to the space
		for (Object obj : context) {
			degreesCount = 0 + (iteration * ModelUtils.degreesSeparation);
			// System.out.println("Degrees count "+ degreesCount);
			teta = Math.toRadians(degreesCount);
			x = 24 * Math.sin(teta) + 25;
			y = 24 * Math.cos(teta) + 25;
			// System.out.println("Moving agent to " + x + ", " + y);
			// System.out.println("Space dimentions: "+ space.getDimensions());
			space.moveTo(obj, x, y);
			iteration++;
		}
	}
	
	protected double randomNumberBetweenMargins(int low, int high) {
		Integer intNumber = random.nextInt(high - low) + low;
		return random.nextDouble() + intNumber.doubleValue();
	}
	
	protected void addAgentsInRandomSpace() {
		double x;
		double y;
		for (Object obj : context) {
			x = randomNumberBetweenMargins(0, ModelUtils.spaceXSize);
			y = randomNumberBetweenMargins(0, ModelUtils.spaceYSize);
			// System.out.println("Adding element to " + x+", "+y);
			space.moveTo(obj, x, y);
		}
	}
	
	public void createTopology() {
		System.out.println("Creating topology " + this.getClass().getSimpleName());
	}
}
