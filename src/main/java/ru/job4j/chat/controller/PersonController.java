package ru.job4j.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.handlers.PasswordException;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Role;
import ru.job4j.chat.model.dto.PersonDTO;
import ru.job4j.chat.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.chat.repository.RoleRepository;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/users")
public class PersonController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class.getSimpleName());
    private final PersonRepository persons;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final ObjectMapper objectMapper;

    public PersonController(PersonRepository persons, RoleRepository roleRepository, BCryptPasswordEncoder encoder, ObjectMapper objectMapper) {
        this.persons = persons;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
    }

    @PatchMapping("/patch")
    public Person patch(@RequestBody PersonDTO personDTO) throws InvocationTargetException, IllegalAccessException {
        System.out.println("DTO: " + personDTO);
        var current = persons.findById(personDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        System.out.println("Current: " + current);
        List<Role> roles = new ArrayList<>();
        if (personDTO.getRoles() != null && personDTO.getRoles().length > 0) {
            for (int id : personDTO.getRoles()) {
                roles.add(roleRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
            }
        } else {
            roles = null;
        }
        Person person = PersonDTO.toPerson(personDTO, roles);
        System.out.println("DTO to Person: " + person);
        var methods = current.getClass().getDeclaredMethods();
        var namePerMethod = new HashMap<String, Method>();
        for (var method : methods) {
            var name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                namePerMethod.put(name, method);
            }
        }
        for (var name : namePerMethod.keySet()) {
            if (name.startsWith("get")) {
                var getMethod = namePerMethod.get(name);
                var setMethod = namePerMethod.get(name.replace("get", "set"));
                if (setMethod == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid properties mapping");
                }
                Object newValue = getMethod.invoke(person);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        System.out.println("Current pre save:" + current);
        persons.save(current);
        return current;
    }

    @GetMapping("/all")
    public List<Person> findAll() {
        return (List<Person>) persons.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        validate(id);
        return new ResponseEntity<>(this.persons.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Person with id %s not found.", id))),
                HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> signUp(@RequestBody Person person) {
        validate(person);
        person.setPassword(encoder.encode(person.getPassword()));
        Person save = persons.save(person);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable int id, @RequestBody Person person) {
        validate(id);
        validate(person);
        var existingPerson = persons.findById(id);
        if (existingPerson.isPresent()) {
            var updPerson = existingPerson.get();
            updPerson.setLogin(person.getLogin());
            updPerson.setPassword(person.getPassword());
            updPerson.setRoles(person.getRoles());
            persons.save(updPerson);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        validate(id);
        Person person = new Person();
        person.setId(id);
        this.persons.delete(person);
        return ResponseEntity.ok().build();
    }

    private void validate(Person person) {
        var login = person.getLogin();
        var password = person.getPassword();
        if (login == null || password == null) {
            throw new NullPointerException("Login and password mustn't be empty");
        }
        if (password.length() < 6) {
            throw new PasswordException("Invalid password. Password length must be more than 5 characters.");
        }
    }

    private void validate(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id must be > 0");
        }
    }
}
