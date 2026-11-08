package es.deusto.sd.ecoembes.dto;

import es.deusto.sd.ecoembes.entity.FillLevel;

public class DumpsterStatusDTO {
	private long dumpsterId;
	private String address;
	private int numContainers;
	private FillLevel fillLevel;

	// Constructor without parameters
	public DumpsterStatusDTO() { }

	// Constructor with parameters
	public DumpsterStatusDTO(long dumpsterId, String address, int numContainers, FillLevel fillLevel) {
		this.dumpsterId = dumpsterId;
		this.address = address;
		this.numContainers = numContainers;
		this.fillLevel = fillLevel;
	}

	// Getters and setters
	public long getDumpsterId() {
		return dumpsterId;
	}

	public void setDumpsterId(long dumpsterId) {
		this.dumpsterId = dumpsterId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getNumContainers() {
		return numContainers;
	}

	public void setNumContainers(int numContainers) {
		this.numContainers = numContainers;
	}

	public FillLevel getFillLevel() {
		return fillLevel;
	}

	public void setFillLevel(FillLevel fillLevel) {
		this.fillLevel = fillLevel;
	}
}
