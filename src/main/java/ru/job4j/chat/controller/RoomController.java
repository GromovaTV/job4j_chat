package ru.job4j.chat.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.model.Message;
import ru.job4j.chat.model.Room;
import ru.job4j.chat.repository.RoomRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/room")
public class RoomController {

    private static final String API = "http://localhost:8080/message/";

    private final RoomRepository roomRepository;

    private final RestTemplate rest;

    public RoomController(final RoomRepository roomRepository, RestTemplate rest) {
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