package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

public class RecyclingPlant {
	private long id;
	private String name;

	// Constructor without parameters
	public RecyclingPlant() { }

	// Constructor with parameters
	public RecyclingPlant(long id, String name) {
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

	// hashCode and equals
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecyclingPlant other = (RecyclingPlant) obj;
		return id == other.id;
	}
}
