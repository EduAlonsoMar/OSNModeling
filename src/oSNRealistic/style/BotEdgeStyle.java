package oSNRealistic.style;

import java.awt.Color;

import oSNRealistic.agent.Agent;
import oSNRealistic.agent.AgentState;
import oSNRealistic.agent.Bot;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;

public class BotEdgeStyle implements EdgeStyleOGL2D {

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		return (int) (Math.abs(edge.getWeight()));
	}

	@Override
	public Color getColor(RepastEdge<?> edge) {
		if (edge.getSource() instanceof Bot){
			return Color.RED;
		} else {
			Agent a = (Agent) edge.getSource();
			if (a.getState() == AgentState.SUSCEPTIBLE) {
				return Color.gray;
			} else if (a.getState() == AgentState.BELIEVER) {
				return Color.red;
			} else {
				return Color.GREEN;	
			}
				
		}
		
	}

}
