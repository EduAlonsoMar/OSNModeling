package oSNRealistic.style;

import java.awt.Color;

import oSNRealistic.agent.Agent;
import oSNRealistic.agent.AgentState;
import repast.simphony.gis.styleEditor.SimpleMarkFactory;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class OSNAgentStyle extends DefaultStyleOGL2D {
    
    @Override
    public Color getColor(Object object) {
        Agent a;
        if (object instanceof Agent) {
            a = (Agent) object;
        } else {
            return Color.BLACK;
        }
        
        if (!a.isInfluencer()) {
        	if (a.getState() == AgentState.BELIEVER) {
        		return Color.PINK;
        	} else if (a.getState() == AgentState.SUSCEPTIBLE) {
        		return Color.BLUE;
        	} else {
        		return Color.DARK_GRAY;	
        	}            
        } else {
            return Color.YELLOW;
        }
    }

}