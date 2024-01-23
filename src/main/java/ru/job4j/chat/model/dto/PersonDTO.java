package ru.job4j.chat.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Role;
import ru.job4j.chat.model.validator.Operation;

import javax.validation.constraints.Positive;
import java.util.List;

@Data
@RequiredArgsConstructor
public class PersonDTO {

    @Positive(message = "Id should be positive", groups = {Operation.OnPatch.class})
    private int id;

    private String login;

    private String password;

    private int[] roles;

    public static PersonDTO of(int id, String login, String password) {
        PersonDTO person = new PersonDTO();
        person.setId(id);
        person.setLogin(login);
        person.setPassword(password);
        return person;
    }

    public static Person toPerson(PersonDTO dto, List<Role> roleList) {
        Person person = new Person();
        person.setId(dto.getId());
        person.setLogin(dto.getLogin());
        person.setPassword(dto.getPassword());
        person.setRoles(roleList);
        return person;
    }

    @Override
    public String toString() {
        return "PersonDTO{"
                + "id=" + id
                + ", login='" + login + '\''
                + ", password='" + password + '\''
                + ", roles=" + roles
                + '}';
    }
}
