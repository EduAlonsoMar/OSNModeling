package oSNRealistic.topology;

import oSNRealistic.ModelUtils;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class TopologyGenerator {
	
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	//private Network<Object> net;
	private Grid<Object> grid;
	

	
	public TopologyGenerator (Context <Object> context, ContinuousSpace<Object> space, Grid<Object> grid) {
		this.context = context;
		this.space = space;
		this.grid = grid;
		
		
	}
	
	public void generateSelectedTopology() {
		System.out.println("Creating topology " + ModelUtils.selectedTopology);
		TopologyCreator creator;
		switch (ModelUtils.selectedTopology) {
		case "Barabasi-Albert":
			creator = new BarabasiAlbertTopologyGenerator(context, space, grid);
			break;
		case "default":
		default:
			creator = new ProximityComunitiesTopologyCreator(context, space, grid);
			break;
		}
		
		creator.createTopology();
	}

}
