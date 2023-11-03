package ru.job4j.chat.model.dto;

import ru.job4j.chat.model.Person;
import ru.job4j.chat.model.Role;
import java.util.List;
import java.util.Objects;

public class PersonDTO {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int[] getRoles() {
        return roles;
    }

    public void setRoles(int[] roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDTO personDTO = (PersonDTO) o;
        return id == personDTO.id &&
                Objects.equals(login, personDTO.login) &&
                Objects.equals(password, personDTO.password) &&
                Objects.equals(roles, personDTO.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login, password, roles);
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
