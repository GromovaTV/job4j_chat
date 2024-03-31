package ru.job4j.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.model.Message;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Room;
import ru.job4j.chat.model.dto.RoomDTO;
import ru.job4j.chat.model.validator.Operation;
import ru.job4j.chat.repository.PersonRepository;
import ru.job4j.chat.repository.RoomRepository;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/room")
@Validated
public class RoomController {

    private static final Logger LOG = LoggerFactory.getLogger(RoomController.class.getName());
    @Value("${app.url}")
    private String domainName;
    private final PersonRepository persons;
    private final RoomRepository roomRepository;
    private final RestTemplate rest;

    public RoomController(PersonRepository persons, final RoomRepository roomRepository,
                          RestTemplate rest) {
        this.persons = persons;
        this.roomRepository = roomRepository;
        this.rest = rest;
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
                domainName + "/message/",
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
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Room> create(@Valid @RequestBody RoomDTO roomDTO) {
        var ownerId = roomDTO.getOwnerId();
        var owner = persons.findById(ownerId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id %s not found.", ownerId))
        );
        Room room = Room.of(0, roomDTO.getName(), owner);
        return new ResponseEntity<>(
                this.roomRepository.save(room),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Void> update(@Valid @RequestBody RoomDTO roomDTO) {
        var id = roomDTO.getId();
        var updRoom = roomRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Room with id %s not found.", id)));
        var ownerId = roomDTO.getOwnerId();
        var owner = persons.findById(ownerId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id %s not found.", ownerId))
        );
        updRoom.setName(roomDTO.getName());
        updRoom.setOwner(owner);
        roomRepository.save(updRoom);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        validate(id);
        Room room = new Room();
        room.setId(id);
        this.roomRepository.delete(room);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/patch")
    @Validated(Operation.OnPatch.class)
    public Room patch(@Valid @RequestBody RoomDTO roomDTO) throws InvocationTargetException,
            IllegalAccessException {
        LOG.info("DTO: " + roomDTO);
        var current = roomRepository.findById(roomDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Person person = persons.findById(roomDTO.getOwnerId()).orElse(null);
        Room room = RoomDTO.toRoom(roomDTO, person);
        LOG.info("DTO to Room: " + room);
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
                Object newValue = getMethod.invoke(room);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        LOG.info("Current pre save:" + current);
        roomRepository.save(current);
        return current;
    }

    private void validate(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id must be > 0");
        }
    }
}