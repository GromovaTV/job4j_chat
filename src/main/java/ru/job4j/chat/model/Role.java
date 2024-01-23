package ru.job4j.chat.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.job4j.chat.model.validator.Operation;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Entity(name = "u_role")
@Table(name = "u_role")
@Data
@RequiredArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive(message = "Id should be positive",
            groups = {Operation.OnPatch.class, Operation.OnUpdate.class})
    private int id;

    @Column(unique = true)
    @NotBlank(message = "Role should not be empty",
            groups = {Operation.OnCreate.class, Operation.OnPatch.class, Operation.OnUpdate.class})
    private String role;

    public static Role of(String role) {
        Role auth = new Role();
        auth.role = role;
        return auth;
    }

    @Override
    public String toString() {
        return "Role{"
                + "id=" + id
                + ", role='" + role + '\''
                + '}';
    }
}