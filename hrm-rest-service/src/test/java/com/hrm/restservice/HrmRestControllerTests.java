/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hrm.restservice;


import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.xml.bind.Marshaller;

import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import generated.*;


@SpringBootTest
@AutoConfigureMockMvc
public class HrmRestControllerTests {

	@Autowired
	private MockMvc mockMvc;
		
	@Autowired
	private PersonsRepository  personRepository;
	
	private static Jaxb2Marshaller marshaller;
	
	@BeforeAll
	public static void pinit() {
		marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("generated");
		Map<String, Boolean> propMap = new HashMap<String, Boolean>();
		propMap.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setMarshallerProperties(propMap);
	}
	

	
	@Test
	public void testRepositoryAll() {
 
		Persons allPersons = new Persons();
		List<Person> personList = personRepository.getAll();
		personList.stream().forEach(sPerson -> allPersons.getPerson().add(sPerson));
		
		
		StringResult stringResult = new StringResult();
		
		marshaller.marshal(allPersons, stringResult);
		String personsAsString = stringResult.toString();
		assertThat(personsAsString.startsWith("<?xml"));
		
		Persons unmPersons = 
			(Persons) marshaller.unmarshal(new StringSource(personsAsString));
		assertThat( ( unmPersons.getPerson().size() == 2) );
	   
	}
	
	
	@Test
	public void testMarschallPersons() {
 
		StringResult stringResult = new StringResult();
	
		Persons persons = new Persons();
		
		Person person = new Person();
		person.setName("John-Jane Doe");
		person.setAge( BigInteger.valueOf(23));
		person.setGender( "f");
		
		persons.getPerson().add(person);
		
		marshaller.marshal(persons, stringResult);
		String personsAsString = stringResult.toString();
		assertThat(personsAsString.startsWith("<?xml"));
		
		Persons unmPersons = 
			(Persons) marshaller.unmarshal(new StringSource(personsAsString));
		assertThat( ( unmPersons.getPerson().size() == 1) );
	   
	}
	
	
	@Test
	public void testUnMarschallGenders() {
		String personGenderT = """
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<persons>
			<person>
				<name>John-Jane Doe</name>
				<gender>t</gender>
				<age>23</age>
			</person>
		</persons>		
		""";

		String personGenderF = """
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
		<persons>
			<person>
				<name>John-Jane Doe</name>
				<gender>f</gender>
				<age>23</age>
			</person>
		</persons>		
		""";
 
		
		Persons unmGenderF = 
			(Persons) marshaller.unmarshal(new StringSource(personGenderF));
		assertThat( ( unmGenderF.getPerson().size() == 1) );
		
		
		try {
			Persons unmGenderT = 
				(Persons) marshaller.unmarshal(new StringSource(personGenderT));
			assertThat( ( unmGenderT.getPerson().size() == 1) );
		}
		catch( XmlMappingException ex)	{
			assertThat( ex != null);
		}
			
	}

}
