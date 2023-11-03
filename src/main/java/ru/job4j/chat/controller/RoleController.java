package ru.job4j.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.model.Role;
import ru.job4j.chat.repository.RoleRepository;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/role")
public class RoleController {
    private final RoleRepository roleRepository;

    public RoleController(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PatchMapping("/patch")
    public Role patch(@RequestBody Role role) throws InvocationTargetException, IllegalAccessException {
        var current = roleRepository.findById(role.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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
                Object newValue = getMethod.invoke(role);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        System.out.println("Current pre save:" + current);
        roleRepository.save(current);
        return current;
    }

    @GetMapping("/")
    public ResponseEntity<List<Role>> findAll() {
        List<Role> body = (List<Role>) roleRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK)
                .header("Job4jCustomHeader", "job4j")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> findById(@PathVariable int id) {
        validate(id);
        Optional<Role> role = this.roleRepository.findById(id);
        if (role.isEmpty()) {
            System.out.println("ROLE IS EMPTY");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Role with id %s not found.", id));
        } else {
            System.out.println(role.get());

        }
        return new ResponseEntity<>(role.get(), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Role> create(@RequestBody Role role) {
        validate(role);
        return new ResponseEntity<>(
                this.roleRepository.save(role),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable int id, @RequestBody Role role) {
        validate(id);
        validate(role);
        var existingRole = roleRepository.findById(id);
        if (existingRole.isPresent()) {
            var updRole = existingRole.get();
            updRole.setRole(role.getRole());
            roleRepository.save(updRole);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        validate(id);
        Role role = new Role();
        role.setId(id);
        this.roleRepository.delete(role);
        return ResponseEntity.ok().build();
    }

    private void validate(Role role) {
        var r = role.getRole();
        if (r == null) {
            throw new NullPointerException("Role mustn't be empty");
        }
    }

    private void validate(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id must be > 0");
        }
    }
}