package ru.job4j.chat.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.job4j.chat.model.validator.Operation;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Entity(name = "room")
@Table(name = "room")
@Data
@RequiredArgsConstructor
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

    @Override
    public String toString() {
        return "Room{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", owner=" + owner
                + '}';
    }
}