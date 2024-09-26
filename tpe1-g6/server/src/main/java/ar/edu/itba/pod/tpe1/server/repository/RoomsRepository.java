package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.emergencyCare.RoomStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class RoomsRepository {
    private final Object lock = "lock";

    @Getter
    private final List<RoomStatus> rooms = new ArrayList<>();

    public int addRoom() {
        synchronized (lock) {
            rooms.add(rooms.size(), RoomStatus.ROOM_STATUS_FREE);
            //if none, its 0
            //but by the assignment, rooms start at 1
            return rooms.size();
        }
    }

    public RoomStatus setRoomStatus(int roomId, RoomStatus status) {
        int id = roomId - 1; //rooms start at 1, but in reality they start from 0
        synchronized (lock) {
            if (roomId > rooms.size()) {
                throw new IllegalArgumentException("Room #" + roomId + " does not exist.");
            }
            rooms.set(id, status);

            return status;
        }
    }

    public RoomStatus getRoomStatus(int roomId) {
        int id = roomId - 1; //rooms start at 1, but in reality they start from 0
        synchronized (lock) {
            if (roomId > rooms.size()) {
                throw new IllegalArgumentException("Room #" + roomId + " does not exist.");
            }
            return rooms.get(id);
        }
    }
}
