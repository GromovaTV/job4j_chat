package ru.job4j.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.handlers.PasswordException;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Role;
import ru.job4j.chat.model.dto.PersonDTO;
import ru.job4j.chat.model.validator.Operation;
import ru.job4j.chat.repository.PersonRepository;
import ru.job4j.chat.repository.RoleRepository;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class PersonController {

    private static final Logger LOG =
            LoggerFactory.getLogger(PersonController.class.getSimpleName());
    private final PersonRepository persons;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final ObjectMapper objectMapper;

    public PersonController(PersonRepository persons, RoleRepository roleRepository,
                            BCryptPasswordEncoder encoder, ObjectMapper objectMapper) {
        this.persons = persons;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
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
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Person> signUp(@Valid @RequestBody Person person) {
        validate(person);
        person.setPassword(encoder.encode(person.getPassword()));
        Person save = persons.save(person);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    @PutMapping("/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Void> update(@Valid @RequestBody Person person) {
        var id = person.getId();
        validate(person);
        var updPerson = persons.findById(person.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("User with id %s not found.", id)));
        updPerson.setLogin(person.getLogin());
        updPerson.setPassword(encoder.encode(person.getPassword()));
        updPerson.setRoles(person.getRoles());
        persons.save(updPerson);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        validate(id);
        Person person = new Person();
        person.setId(id);
        this.persons.delete(person);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/patch")
    @Validated(Operation.OnPatch.class)
    public Person patch(@Valid @RequestBody PersonDTO personDTO) throws InvocationTargetException,
            IllegalAccessException {
        LOG.info("DTO: " + personDTO);
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
        LOG.info("DTO to Person: " + person);
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
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid properties mapping");
                }
                Object newValue = getMethod.invoke(person);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        LOG.info("Current pre save:" + current);
        persons.save(current);
        return current;
    }

    private void validate(Person person) {
        var password = person.getPassword();
        if (password.length() < 6) {
            throw new PasswordException("Invalid password."
                    + "Password length must be more than 5 characters.");
        }
    }

    private void validate(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id must be > 0");
        }
    }
}
