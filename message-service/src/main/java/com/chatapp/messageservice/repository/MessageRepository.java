package com.chatapp.messageservice.repository;

import com.chatapp.messageservice.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

    // Paginated retrieval, sorted by Pageable (caller provides Sort.by("timeStamp"))
    Page<Message> findByRoomId(String roomId, Pageable pageable);
}
