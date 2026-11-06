package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.AssignmentDTO;
import es.deusto.sd.ecoembes.dto.DTOAssembler;
import es.deusto.sd.ecoembes.dto.DumpsterDTO;
import es.deusto.sd.ecoembes.dto.DumpsterStatusDTO;
import es.deusto.sd.ecoembes.dto.DumpsterUsageDTO;
import es.deusto.sd.ecoembes.dto.PlantDTO;
import es.deusto.sd.ecoembes.entity.Dumpster;
import es.deusto.sd.ecoembes.entity.FillLevel;
import es.deusto.sd.ecoembes.service.EcoembesAppService;

/**
 * Remote Façade pattern: the REST API of the Central Server. It only deals with
 * HTTP (paths, parameters, status codes) and converts between DTOs and domain
 * objects; all the business logic is delegated to EcoembesAppService.
 *
 * Employee operations receive the session token in the "token" HTTP header.
 */
@RestController
public class EcoembesController {

	private final EcoembesAppService appService;
	private final DTOAssembler assembler;

	// Spring injects the service and the assembler (dependency injection)
	public EcoembesController(EcoembesAppService appService, DTOAssembler assembler) {
		this.appService = appService;
		this.assembler = assembler;
	}

	@PostMapping("/dumpsters/{dumpsterId}/measurements")
	public ResponseEntity<Void> updateDumpsterInfo(
			@PathVariable("dumpsterId") long dumpsterId,
			@RequestParam("numContainers") int numContainers,
			@RequestParam("fillLevel") FillLevel fillLevel) {
		try {
			appService.updateDumpsterInfo(dumpsterId, numContainers, fillLevel);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@PostMapping("/sessions")
	public ResponseEntity<Long> login(
			@RequestParam("email") String email,
			@RequestParam("password") String password) {
		try {
			return new ResponseEntity<>(appService.login(email, password), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@DeleteMapping("/sessions/{token}")
	public ResponseEntity<Void> logout(
			@PathVariable("token") long token) {
		try {
			appService.logout(token);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@PostMapping("/dumpsters")
	public ResponseEntity<DumpsterDTO> createDumpster(
			@RequestHeader(value = "token", required = false) Long token,
			@RequestBody DumpsterDTO dumpster) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			Dumpster created = appService.createDumpster(token, assembler.toDumpster(dumpster));
			return new ResponseEntity<>(assembler.toDumpsterDTO(created), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@GetMapping("/dumpsters/{dumpsterId}/usage")
	public ResponseEntity<List<DumpsterUsageDTO>> getDumpsterUsage(
			@RequestHeader(value = "token", required = false) Long token,
			@PathVariable("dumpsterId") long dumpsterId,
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<DumpsterUsageDTO> usage = appService.getDumpsterUsage(token, dumpsterId, startDate, endDate)
					.stream().map(assembler::toUsageDTO).toList();
			return new ResponseEntity<>(usage, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@GetMapping("/dumpsters/status")
	public ResponseEntity<List<DumpsterStatusDTO>> getDumpsterStatusByArea(
			@RequestHeader(value = "token", required = false) Long token,
			@RequestParam("postalCode") String postalCode,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<DumpsterStatusDTO> status = appService.getDumpsterStatusByArea(token, postalCode, date)
					.stream().map(assembler::toStatusDTO).toList();
			return new ResponseEntity<>(status, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@GetMapping("/plants/{plantId}/capacity")
	public ResponseEntity<Double> getPlantCapacity(
			@RequestHeader(value = "token", required = false) Long token,
			@PathVariable("plantId") long plantId,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			return new ResponseEntity<>(appService.getPlantCapacity(token, plantId, date), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@PostMapping("/plants/{plantId}/assignments")
	public ResponseEntity<AssignmentDTO> assignDumpstersToPlant(
			@RequestHeader(value = "token", required = false) Long token,
			@PathVariable("plantId") long plantId,
			@RequestBody List<Long> dumpsterIds) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			AssignmentDTO assignment = assembler.toAssignmentDTO(
					appService.assignDumpstersToPlant(token, plantId, dumpsterIds));
			return new ResponseEntity<>(assignment, HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@GetMapping("/dumpsters")
	public ResponseEntity<List<DumpsterDTO>> getAllDumpsters(
			@RequestHeader(value = "token", required = false) Long token) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<DumpsterDTO> dumpsters = appService.getAllDumpsters(token)
					.stream().map(assembler::toDumpsterDTO).toList();
			return new ResponseEntity<>(dumpsters, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	@GetMapping("/plants")
	public ResponseEntity<List<PlantDTO>> getAllPlants(
			@RequestHeader(value = "token", required = false) Long token) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<PlantDTO> plants = appService.getAllPlants(token)
					.stream().map(assembler::toPlantDTO).toList();
			return new ResponseEntity<>(plants, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(errorStatus(e));
		}
	}

	// Translates the exceptions thrown by the service into HTTP status codes.
	private HttpStatus errorStatus(RuntimeException e) {
		if (e instanceof SecurityException) {
			return HttpStatus.UNAUTHORIZED;			// 401
		} else if (e instanceof IllegalArgumentException) {
			return HttpStatus.BAD_REQUEST;			// 400
		} else if (e instanceof NoSuchElementException) {
			return HttpStatus.NOT_FOUND;			// 404
		} else if (e instanceof IllegalStateException) {
			return HttpStatus.CONFLICT;				// 409
		} else {
			return HttpStatus.INTERNAL_SERVER_ERROR;	// 500
		}
	}
}
