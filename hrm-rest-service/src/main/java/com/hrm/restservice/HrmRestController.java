package com.hrm.restservice;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import jakarta.xml.bind.Marshaller;
import generated.Person;
import generated.Persons;

@RestController
public class HrmRestController {

	@Autowired
	PersonsRepository personsRepository;
	
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	private static Jaxb2Marshaller marshaller;
	
	private static AtomicLong addOkNo = new AtomicLong(0);
	private static AtomicLong addFailNo = new AtomicLong(0);
	private static AtomicLong getNo = new AtomicLong(0);
	private static AtomicLong personsAddedNo = new AtomicLong(0);
	
	public HrmRestController() {
		marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("generated");
		Map<String, Boolean> propMap = new HashMap<String, Boolean>();
		propMap.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setMarshallerProperties(propMap);
	}
	
	
	@GetMapping("/persons")
	public String getPersons() {
		Persons persons = new Persons();
		personsRepository.getAll().stream().forEach( p -> persons.getPerson().add(p));
		getNo.incrementAndGet();
		String retValue = marshall(persons );
		return retValue;
	}

	@GetMapping("/persons/statistics")
	public String statistics() {
	
		String retValue = String.format("""
			Anzahl_Add %d,  
			Anz_invalid_Add %d,  
			Anz_valid_Req %d,  
			Hinzugefügte Personen %d
			""", 
			(addFailNo.get() + addOkNo.get()),
			addFailNo.get(),
			(addOkNo.get() + getNo.get()),
			personsAddedNo.get());
		return retValue;
	}
	
	@PostMapping("/persons/add")
	public String addPersons(@RequestBody String xmlString) {
		
		Persons addPersons = unMarshall(xmlString);
		
		Boolean personInRepository =
			addPersons.getPerson().stream()
			.map( Person::getName)
			.anyMatch(nm -> { return personsRepository.find(nm) != null; } );
	
		if ( personInRepository ) {
			addFailNo.incrementAndGet();
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Person already in Repository");
		}
		else {
			addPersons.getPerson().stream()
			.forEach(personsRepository::add);
			addOkNo.incrementAndGet();
			personsAddedNo.addAndGet(addPersons.getPerson().size());
			return "" + addPersons.getPerson().size() + " Persons added";
		}
	}
	
	@DeleteMapping("/persons/delete")
	public String deletePersons(@RequestBody String xmlString) {
		Persons delPersons = unMarshall(xmlString);
		
		delPersons.getPerson().stream()
		.forEach(personsRepository::remove);
		
		return " Persons deleted";
	}
	
	private String marshall(Persons persons ) {
		StringResult stringResult = new StringResult();
		marshaller.marshal(persons, stringResult);
		String personsAsString = stringResult.toString();
		return personsAsString;
	}
	
	private Persons unMarshall(String xmlString ) {
		Persons unmPersons = 
			(Persons) marshaller.unmarshal(new StringSource(xmlString));
		return unmPersons;
	}
}
