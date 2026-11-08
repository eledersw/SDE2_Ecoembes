package es.deusto.sd.ecoembes.dto;

public class PlantDTO {
	private long id;
	private String name;

	// Constructor without parameters
	public PlantDTO() { }

	// Constructor with parameters
	public PlantDTO(long id, String name) {
		this.id = id;
		this.name = name;
	}

	// Getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
