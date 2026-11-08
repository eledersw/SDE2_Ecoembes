package es.deusto.sd.ecoembes.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import es.deusto.sd.ecoembes.entity.Employee;

/** State Management: in-memory map of logged-in employees (token -> employee). */
public class SessionManager {

	private final Map<Long, Employee> sessions = new ConcurrentHashMap<>();

	public long createSession(Employee emp) {
		long token = System.currentTimeMillis();

		while (sessions.putIfAbsent(token, emp) != null) {
			token++;
		}

		return token;
	}

	public Employee getEmployee(long token) {
		return sessions.get(token);
	}

	public void removeSession(long token) {
		sessions.remove(token);
	}
}
