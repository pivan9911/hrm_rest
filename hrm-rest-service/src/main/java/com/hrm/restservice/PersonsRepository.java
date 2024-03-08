package com.hrm.restservice;

import jakarta.annotation.PostConstruct;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import generated.Person;

@Component
public class PersonsRepository {
	private static final Map<String, Person> personMap = new ConcurrentHashMap<>();

	@PostConstruct
	public void initData() {
		
		
		Person albert = new Person();
		albert.setName("Albert");
		albert.setAge( BigInteger.valueOf(23));
		albert.setGender( "m");
		
		personMap.put(albert.getName(), albert);
		
		Person britta = new Person();
		britta.setName("Britta");
		britta.setAge( BigInteger.valueOf(23));
		britta.setGender( "f");
		
		personMap.put(britta.getName(), britta);
	}

	/**
	 * 
	 * @param name
	 * @return Person or null
	 */
	public Person find(String name) {
		Assert.notNull(name, "The Person name must not be null");
		return personMap.get(name);
	}
	
	/**
	 * 
	 * @param person
	 * @throws IllegalArgumentException		Person already in personMap
	 */
	public void add(Person person) throws IllegalArgumentException {
		if ( personMap.containsKey(person.getName())) {
			throw new IllegalArgumentException("Already existing person:" + person.getName());
		}
		personMap.put(person.getName(), person);
	}
	
	/**
	 * 
	 * @param person
	 * @throws IllegalArgumentException		Person does not Exist
	 */
	public void update(Person person) throws IllegalArgumentException {
		 
		if ( !personMap.containsKey(person.getName())) {
			throw new IllegalArgumentException("None-existing person:" + person.getName());
		}
		personMap.put(person.getName(), person);
	}
	
	
	/**
	 * @param person	Removed from Repository
	 */
	public void remove(Person person) {
		personMap.remove(person.getName());
	}
	
	/**
	 * 
	 * @return		All Persons in personMap
	 */
	public List<Person> getAll(){
		return new ArrayList<Person>(personMap.values());
	}
}
