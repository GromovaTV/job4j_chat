package ru.job4j.chat.model.dto;

import ru.job4j.chat.model.Message;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Room;
import java.util.Objects;

public class MessageDTO {
    private int id;
    private String message;
    private int personId;
    private int roomId;

    public static MessageDTO of(int id, String message, int personId, int roomId) {
        MessageDTO m = new MessageDTO();
        m.setId(id);
        m.setMessage(message);
        m.setPersonId(personId);
        m.setRoomId(roomId);
        return m;
    }

    public static Message toMessage(MessageDTO dto, Person person, Room room) {
        Message message = new Message();
        message.setId(dto.getId());
        message.setMessage(dto.getMessage());
        message.setPerson(person);
        message.setRoom(room);
        return message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageDTO that = (MessageDTO) o;
        return id == that.id &&
                personId == that.personId &&
                roomId == that.roomId &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, personId, roomId);
    }

    @Override
    public String toString() {
        return "MessageDTO{"
                + "id=" + id
                + ", message='" + message + '\''
                + ", personId=" + personId
                + ", roomId=" + roomId
                + '}';
    }
}
