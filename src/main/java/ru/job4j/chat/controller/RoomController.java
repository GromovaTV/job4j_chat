package ru.job4j.chat.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.model.Message;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Room;
import ru.job4j.chat.model.dto.RoomDTO;
import ru.job4j.chat.repository.PersonRepository;
import ru.job4j.chat.repository.RoomRepository;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/room")
public class RoomController {

    private static final String API = "http://localhost:8080/message/";
    private final PersonRepository persons;
    private final RoomRepository roomRepository;
    private final RestTemplate rest;

    public RoomController(PersonRepository persons, final RoomRepository roomRepository, RestTemplate rest) {
        this.persons = persons;
        this.roomRepository = roomRepository;
        this.rest = rest;
    }

    @PatchMapping("/patch")
    public Room patch(@RequestBody RoomDTO roomDTO) throws InvocationTargetException, IllegalAccessException {
        System.out.println("DTO: " + roomDTO);
        var current = roomRepository.findById(roomDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Person person = persons.findById(roomDTO.getOwnerId()).orElse(null);
        Room room = RoomDTO.toRoom(roomDTO, person);
        System.out.println("DTO to Room: " + room);
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
                Object newValue = getMethod.invoke(room);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        System.out.println("Current pre save:" + current);
        roomRepository.save(current);
        return current;
    }

    @GetMapping("/all")
    public List<Room> findAll() {
        return StreamSupport.stream(
                this.roomRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("")
    @ResponseBody
    public List<Message> findMessageByRoom(@RequestParam String name) {
        return rest.exchange(
                API,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Message>>() { }, name
        ).getBody();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> findById(@PathVariable int id) {
        validate(id);
        Room room = roomRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Room with id %s not found.", id)));

        return new ResponseEntity<>(room, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Room> create(@RequestBody Room room) {
        validate(room);
        return new ResponseEntity<>(
                this.roomRepository.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable int id, @RequestBody Room room) {
        validate(id);
        validate(room);
        var existingRoom = roomRepository.findById(id);
        if (existingRoom.isPresent()) {
            var updRoom = existingRoom.get();
            updRoom.setName(room.getName());
            updRoom.setOwner(room.getOwner());
            roomRepository.save(updRoom);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        validate(id);
        Room room = new Room();
        room.setId(id);
        this.roomRepository.delete(room);
        return ResponseEntity.ok().build();
    }

    private void validate(Room room) {
        var name = room.getName();
        var owner = room.getOwner();
        if (name == null || owner == null) {
            throw new NullPointerException("Room and owner mustn't be empty");
        }
    }

    private void validate(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id must be > 0");
        }
    }
}