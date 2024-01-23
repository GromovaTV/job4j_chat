package ru.job4j.chat.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.job4j.chat.model.Message;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Room;
import ru.job4j.chat.model.validator.Operation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@RequiredArgsConstructor
public class MessageDTO {

    @Positive(message = "Id should be positive",
            groups = {Operation.OnPatch.class, Operation.OnUpdate.class})
    private int id;

    @NotBlank(message = "Message should not be empty",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private String message;

    @Positive(message = "Person id should be positive",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private int personId;

    @Positive(message = "Room id should be positive",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
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
