package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Dumpster;
import es.deusto.sd.ecoembes.entity.DumpsterMeasurement;
import es.deusto.sd.ecoembes.entity.FillLevel;

class EcoembesAppServiceTest {

	private EcoembesAppService service;
	private long token;

	@BeforeEach
	void setUp() {
		service = new EcoembesAppService();
		token = service.login("ane@ecoembes.es", "ane123");
	}

	@Test
	void loginAndLogout() {
		assertThrows(SecurityException.class, () -> service.login("ane@ecoembes.es", "wrong"));
		assertEquals(5, service.getAllDumpsters(token).size());

		service.logout(token);

		assertThrows(SecurityException.class, () -> service.getAllDumpsters(token));
	}

	@Test
	void sensorUpdateReplacesTodaysReading() {
		service.updateDumpsterInfo(2, 790, FillLevel.RED);

		LocalDate today = LocalDate.now();
		List<DumpsterMeasurement> usage = service.getDumpsterUsage(token, 2, today.minusDays(6), today);

		assertEquals(7, usage.size());
		assertEquals(790, usage.get(6).getNumContainers());
		assertThrows(IllegalArgumentException.class, () -> service.updateDumpsterInfo(2, 9999, FillLevel.RED));
		assertThrows(NoSuchElementException.class, () -> service.updateDumpsterInfo(99, 10, FillLevel.GREEN));
	}

	@Test
	void statusByAreaUsesThatDaysReading() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		List<Dumpster> status = service.getDumpsterStatusByArea(token, "48001", yesterday);

		assertEquals(2, status.size());
		assertEquals(850, status.get(0).getNumContainers());
		assertEquals(910, service.getAllDumpsters(token).get(0).getNumContainers());
	}

	@Test
	void createDumpsterRejectsDuplicates() {
		Dumpster created = service.createDumpster(token, new Dumpster(6, "Calle Nueva 1", "48001", 500, 0, FillLevel.GREEN));

		assertEquals(6, created.getId());
		assertThrows(IllegalStateException.class,
				() -> service.createDumpster(token, new Dumpster(6, "Other", "48001", 500, 0, FillLevel.GREEN)));
	}

	@Test
	void assignmentUsesAndChecksPlantCapacity() {
		LocalDate today = LocalDate.now();
		assertEquals(20.0, service.getPlantCapacity(token, 1, today));

		Assignment assignment = service.assignDumpstersToPlant(token, 1, List.of(1L, 3L));

		assertEquals(2, assignment.getNumDumpsters());
		assertEquals(1810, assignment.getTotalContainers());
		assertEquals("ane@ecoembes.es", assignment.getEmployee().getEmail());
		assertEquals(1.9, service.getPlantCapacity(token, 1, today));

		assertThrows(IllegalStateException.class, () -> service.assignDumpstersToPlant(token, 1, List.of(4L)));
		assertThrows(NoSuchElementException.class, () -> service.assignDumpstersToPlant(token, 99, List.of(4L)));
	}
}
