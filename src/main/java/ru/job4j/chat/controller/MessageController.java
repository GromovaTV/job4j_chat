package ru.job4j.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.chat.model.Message;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Room;
import ru.job4j.chat.model.dto.MessageDTO;
import ru.job4j.chat.model.validator.Operation;
import ru.job4j.chat.repository.MessageRepository;
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
@RequestMapping("/message")
@Validated
public class MessageController {

    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class.getName());
    private final MessageRepository messageRepository;
    private final PersonRepository persons;
    private final RoomRepository roomRepository;

    public MessageController(final MessageRepository messageRepository, PersonRepository persons,
                             RoomRepository roomRepository) {
        this.messageRepository = messageRepository;
        this.persons = persons;
        this.roomRepository = roomRepository;
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
    @Validated(Operation.OnCreate.class)
    public ResponseEntity<Message> create(@Valid @RequestBody MessageDTO messageDTO) {
        int roomId = messageDTO.getRoomId();
        var room = this.roomRepository.findById(roomId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Room with id %s not found.", roomId)));
        int personId = messageDTO.getPersonId();
        var person = this.persons.findById(personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Person with id %s not found.", personId)));
        var msg = messageDTO.getMessage();
        var message = Message.of(0, msg, person, room);
        return new ResponseEntity<>(
                this.messageRepository.save(message),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/")
    @Validated(Operation.OnUpdate.class)
    public ResponseEntity<Void> update(@Valid @RequestBody MessageDTO messageDTO) {
        var id = messageDTO.getId();
        Message updMessage = messageRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Message with id %s not found.", id)));
        int roomId = messageDTO.getRoomId();
        var room = this.roomRepository.findById(roomId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Room with id %s not found.", roomId)));
        int personId = messageDTO.getPersonId();
        var person = this.persons.findById(personId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Person with id %s not found.", personId)));
        updMessage.setMessage(messageDTO.getMessage());
        updMessage.setPerson(person);
        updMessage.setRoom(room);
        messageRepository.save(updMessage);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        validate(id);
        Message message = new Message();
        message.setId(id);
        this.messageRepository.delete(message);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/patch")
    @Validated(Operation.OnPatch.class)
    public Message patch(@Valid @RequestBody MessageDTO messageDTO)
            throws InvocationTargetException,
            IllegalAccessException {
        LOG.info("DTO: " + messageDTO);
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
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid properties mapping");
                }
                Object newValue = getMethod.invoke(msg);
                if (newValue != null) {
                    setMethod.invoke(current, newValue);
                }
            }
        }
        LOG.info("Current pre save:" + current);
        messageRepository.save(current);
        return current;
    }

    private void validate(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id must be > 0");
        }
    }
}