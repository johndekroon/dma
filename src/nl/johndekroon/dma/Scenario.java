package nl.johndekroon.dma;

public class Scenario {
	private long id;
	  private String scenario;

	  public long getId() {
	    return id;
	  }

	  public void setId(long id) {
	    this.id = id;
	  }

	  public String getScenario() {
	    return scenario;
	  }

	  public void setScenario(String scenario) {
	    this.scenario = scenario;
	  }

	  // Will be used by the ArrayAdapter in the ListView
	  @Override
	  public String toString() {
	    return scenario;
	  }

	public void add(Scenario scenario2) {
		// TODO Auto-generated method stub
		
	}
}
