package com.chatapp.roomservice.controller;

import com.chatapp.roomservice.entity.Room;
import com.chatapp.roomservice.exception.RoomNotFoundException;
import com.chatapp.roomservice.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Room Service REST Controller
 *
 * Responsibilities:
 *   POST /api/v1/rooms          — create a new room
 *   GET  /api/v1/rooms/{roomId} — join / validate a room
 *   GET  /api/v1/rooms/{roomId}/exists — lightweight existence check (used by other services)
 *
 * Port: 8081
 */
@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // ─── Create Room ─────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Room roomRequest) {
        String roomId = roomRequest.getRoomId();

        if (roomId == null || roomId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Room ID cannot be empty");
        }

        if (roomRepository.findByRoomId(roomId) != null) {
            return ResponseEntity.badRequest().body("Room already exists!");
        }

        Room room = new Room();
        room.setRoomId(roomId.trim());
        Room saved = roomRepository.save(room);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ─── Join / Get Room ──────────────────────────────────────────────────────
    @GetMapping("/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }
        return ResponseEntity.ok(room);
    }

    // ─── Existence Check (internal, called by Chat Service) ──────────────────
    @GetMapping("/{roomId}/exists")
    public ResponseEntity<Boolean> roomExists(@PathVariable String roomId) {
        boolean exists = roomRepository.findByRoomId(roomId) != null;
        return ResponseEntity.ok(exists);
    }
}
