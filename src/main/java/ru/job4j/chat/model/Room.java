package ru.job4j.chat.model;

import ru.job4j.chat.model.validator.Operation;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.Objects;

@Entity(name = "room")
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive(message = "Id should be positive", groups = {Operation.OnPatch.class})
    private int id;

    @Column(unique = true)
    @NotBlank(message = "Name should not be empty",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private String name;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "person_id")
    @NotBlank(message = "Owner should not be empty",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private Person owner;

    public static Room of(int id, String name, Person owner) {
        Room room = new Room();
        room.setId(id);
        room.setName(name);
        room.setOwner(owner);
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

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Room room = (Room) o;
        return id == room.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", owner=" + owner
                + '}';
    }
}