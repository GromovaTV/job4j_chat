package ru.job4j.chat.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity(name = "message")
@Table(name = "message")
@Data
@RequiredArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String message;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "room_id")
    private Room room;

    public static Message of(int id, String message, Person person, Room room) {
        Message m = new Message();
        m.setId(id);
        m.setMessage(message);
        m.setPerson(person);
        m.setRoom(room);
        return m;
    }

    @Override
    public String toString() {
        return "Message{"
                + "id=" + id
                + ", message='" + message + '\''
                + ", person=" + person
                + ", room=" + room
                + '}';
    }
}