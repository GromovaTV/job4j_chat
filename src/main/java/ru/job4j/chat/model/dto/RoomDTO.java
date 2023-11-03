package ru.job4j.chat.model.dto;

import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Room;
import java.util.Objects;

public class RoomDTO {
    private int id;
    private String name;
    private int ownerId;

    public static RoomDTO of(int id, String name, int personId) {
        RoomDTO room = new RoomDTO();
        room.setId(id);
        room.setName(name);
        room.setOwnerId(personId);
        return room;
    }

    public static Room toRoom(RoomDTO dto, Person person) {
        Room room = new Room();
        room.setId(dto.getId());
        room.setName(dto.getName());
        room.setOwner(person);
        return room;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomDTO roomDTO = (RoomDTO) o;
        return id == roomDTO.id &&
                ownerId == roomDTO.ownerId &&
                Objects.equals(name, roomDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, ownerId);
    }

    @Override
    public String toString() {
        return "RoomDTO{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", ownerId=" + ownerId
                + '}';
    }
}