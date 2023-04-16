package oSNRealistic;


import java.util.HashMap;

import oSNRealistic.topology.TopologyGenerator;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;



public class OSNRealisticBuilder implements ContextBuilder<Object> {
	


	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context <Object> context) { 
		context.setId("OSN");

		
		// Create the space in which our network is going to be contained
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", 
				context, 
				new SimpleCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.StickyBorders(), 
				ModelUtils.spaceXSize, 
				ModelUtils.spaceYSize);
		
		// Create the grid in which our agents are going to be moved
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(new HashMap<String, Object>());
		Grid<Object> grid = gridFactory.createGrid("grid", context, // GridBuilderParameters.singleOccupancy2DTorus(new SimpleGridAdder<Object>(), 50, 50)); 
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true, 50, 50));
		
		// Get the parameters for our simulation
		Parameters params = RunEnvironment.getInstance().getParameters();
		ModelUtils.getParameters(params);
		
		TopologyGenerator generator = new TopologyGenerator(context, space, grid);
		generator.generateSelectedTopology();
		
		RunEnvironment.getInstance().endAt(ModelUtils.numberOfTicks);
		return context;
	
	}	

}
