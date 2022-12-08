package oSNRealistic.style;

import java.awt.Color;

import oSNRealistic.agent.Agent;
import oSNRealistic.agent.AgentState;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;

public class OSNnetEdgeStyle implements EdgeStyleOGL2D {

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		return (int) (Math.abs(edge.getWeight()));
	}

	@Override
	public Color getColor(RepastEdge<?> edge) {
		
		Agent a = (Agent) edge.getSource();
		if (a.getState() == AgentState.SUSCEPTIBLE) {
			return Color.gray;
		} else if (a.getState() == AgentState.BELIEVER || a.getState() == AgentState.BOT) {
			return Color.RED;
		} else {
			return Color.GREEN;	
		}
		
	}

}
