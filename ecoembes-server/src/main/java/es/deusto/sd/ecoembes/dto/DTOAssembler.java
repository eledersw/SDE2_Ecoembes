package es.deusto.sd.ecoembes.dto;

import org.springframework.stereotype.Component;

import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Dumpster;
import es.deusto.sd.ecoembes.entity.DumpsterMeasurement;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;

/** Assembler: converts between domain objects and DTOs. */
@Component
public class DTOAssembler {

	public Dumpster toDumpster(DumpsterDTO dto) {
		return new Dumpster(dto.getId(),
							dto.getAddress(),
							dto.getPostalCode(),
							dto.getCapacity(),
							dto.getNumContainers(),
							dto.getFillLevel());
	}

	public DumpsterDTO toDumpsterDTO(Dumpster dumpster) {
		return new DumpsterDTO(dumpster.getId(),
							   dumpster.getAddress(),
							   dumpster.getFillLevel(),
							   dumpster.getPostalCode(),
							   dumpster.getCapacity(),
							   dumpster.getNumContainers());
	}

	public DumpsterUsageDTO toUsageDTO(DumpsterMeasurement measurement) {
		return new DumpsterUsageDTO(measurement.getDate(),
									measurement.getNumContainers(),
									measurement.getFillLevel());
	}

	public DumpsterStatusDTO toStatusDTO(Dumpster dumpster) {
		return new DumpsterStatusDTO(dumpster.getId(),
									 dumpster.getAddress(),
									 dumpster.getNumContainers(),
									 dumpster.getFillLevel());
	}

	public PlantDTO toPlantDTO(RecyclingPlant plant) {
		return new PlantDTO(plant.getId(), plant.getName());
	}

	public AssignmentDTO toAssignmentDTO(Assignment assignment) {
		return new AssignmentDTO(assignment.getId(),
								 assignment.getPlant().getId(),
								 assignment.getDate(),
								 assignment.getNumDumpsters(),
								 assignment.getTotalContainers(),
								 assignment.getEmployee().getEmail());
	}
}
