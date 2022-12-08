package oSNRealistic.agent;

public class FeedMessage implements Comparable<FeedMessage> {

	private FeedType type;
	private Agent creator;
	
	public FeedMessage(Agent creator, FeedType type) {
		this.type = type;
		this.creator = creator;
	}
	
	public Agent getCreator() {
		return this.creator;
	}
	
	public FeedType getType() {
		return this.type;
	}
	
	@Override
	public int compareTo(FeedMessage other) {
	    return (Integer)(this.type).compareTo(other.type);
	}
}
