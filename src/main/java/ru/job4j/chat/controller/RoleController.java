package ru.job4j.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.model.Role;
import ru.job4j.chat.repository.RoleRepository;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/role")
public class RoleController {
    private final RoleRepository roleRepository;

    public RoleController(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping("/")
    public List<Role> findAll() {
        return (List<Role>) roleRepository.findAll();
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