package ru.job4j.chat.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import ru.job4j.chat.model.validator.Operation;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

@Entity(name = "person")
@DynamicUpdate
@Table(name = "person")
@Data
@RequiredArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive(message = "Id should be positive", groups = {Operation.OnUpdate.class})
    private int id;

    @Column(unique = true)
    @NotBlank(message = "Login should not be empty",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private String login;

    @NotBlank(message = "Password should not be empty",
            groups = {Operation.OnCreate.class, Operation.OnUpdate.class})
    private String password;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "persons_roles",
            joinColumns = @JoinColumn(name = "person_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false))
    private List<Role> roles;

    public static Person of(int id, String login, String password) {
        Person person = new Person();
        person.setId(id);
        person.setLogin(login);
        person.setPassword(password);
        return person;
    }

    @Override
    public String toString() {
        return "Person{"
                + "id=" + id
                + ", login='" + login + '\''
                + ", password='" + password + '\''
                + ", roles='" + roles + '\''
                + '}';
    }
}