package ru.job4j.chat.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.job4j.chat.model.Message;

public interface MessageRepository extends CrudRepository<Message, Integer> {

    Message findByMessage(String message);

    @Query("select distinct m from message m where m.room.name=?1")
    Iterable<Message> findByRoom(String name);
}