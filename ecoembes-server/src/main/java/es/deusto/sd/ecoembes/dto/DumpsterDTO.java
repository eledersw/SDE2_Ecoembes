package es.deusto.sd.ecoembes.dto;

import es.deusto.sd.ecoembes.entity.FillLevel;

public class DumpsterDTO {
	private long id;
	private String address;
	private FillLevel fillLevel;
	private String postalCode;
	private int capacity;
	private int numContainers;

	// Constructor without parameters
	public DumpsterDTO() { }

	// Constructor with parameters
	public DumpsterDTO(long id, String address, FillLevel fillLevel, String postalCode,
					   int capacity, int numContainers) {
		this.id = id;
		this.address = address;
		this.fillLevel = fillLevel;
		this.postalCode = postalCode;
		this.capacity = capacity;
		this.numContainers = numContainers;
	}

	// Getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public FillLevel getFillLevel() {
		return fillLevel;
	}

	public void setFillLevel(FillLevel fillLevel) {
		this.fillLevel = fillLevel;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getNumContainers() {
		return numContainers;
	}

	public void setNumContainers(int numContainers) {
		this.numContainers = numContainers;
	}
}
