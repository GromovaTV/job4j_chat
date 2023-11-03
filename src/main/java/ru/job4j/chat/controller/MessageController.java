package ru.job4j.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.model.Message;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Room;
import ru.job4j.chat.model.dto.MessageDTO;
import ru.job4j.chat.repository.MessageRepository;
import ru.job4j.chat.repository.PersonRepository;
import ru.job4j.chat.repository.RoomRepository;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/message")
public class MessageController {
    private final MessageRepository messageRepository;
    private final PersonRepository persons;
    private final RoomRepository roomRepository;

    public MessageController(final MessageRepository messageRepository, PersonRepository persons, RoomRepository roomRepository) {
        this.messageRepository = messageRepository;
        this.persons = persons;
        this.roomRepository = roomRepository;
    }

    @PatchMapping("/patch")
    public Message patch(@RequestBody MessageDTO messageDTO) throws InvocationTargetException, IllegalAccessException {
        System.out.println("DTO: " + messageDTO);
        var current = messageRepository.findById(messageDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Person person = persons.findById(messageDTO.getPersonId()).orElse(null);
        Room room = roomRepository.findById(messageDTO.getRoomId()).orElse(null);
        Message msg = MessageDTO.toMessage(messageDTO, person, room);
        System.out.println("DTO to Msg: " + msg);
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
                Object newValue = getMethod.invoke(msg);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        System.out.println("Current pre save:" + current);
        messageRepository.save(current);
        return current;
    }

    @GetMapping("")
    @ResponseBody
    public List<Message> findByRoomName(@RequestParam String name) {
        return StreamSupport.stream(
                this.messageRepository.findByRoom(name).spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/")
    public List<Message> findAll() {
        return StreamSupport.stream(
                this.messageRepository.findAll().spliterator(), false
        ).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> findById(@PathVariable int id) {
        validate(id);
        var person = this.messageRepository.findById(id);
        return new ResponseEntity<>(
                person.orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                String.format("Message with id %s not found.", id))),
                HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Message> create(@RequestBody Message message) {
        validate(message);
        var room = message.getRoom();
        var person = message.getPerson();
        var msg = message.getMessage();
        if (room == null || person == null || msg  == null) {
            throw new NullPointerException("Person, room and message mustn't be empty");
        }
        return new ResponseEntity<>(
                this.messageRepository.save(message),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable int id, @RequestBody Message message) {
        validate(id);
        validate(message);
        var existingMessage = messageRepository.findById(id);
        if (existingMessage.isPresent()) {
            var updMessage = existingMessage.get();
            updMessage.setMessage(message.getMessage());
            updMessage.setPerson(message.getPerson());
            updMessage.setRoom(message.getRoom());
            messageRepository.save(updMessage);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        validate(id);
        Message message = new Message();
        message.setId(id);
        this.messageRepository.delete(message);
        return ResponseEntity.ok().build();
    }

    private void validate(Message msg) {
        var text = msg.getMessage();
        var person = msg.getPerson();
        var room = msg.getRoom();
        if (text == null || person == null || room == null) {
            throw new NullPointerException("Message, person and room mustn't be empty");
        }
    }

    private void validate(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id must be > 0");
        }
    }
}