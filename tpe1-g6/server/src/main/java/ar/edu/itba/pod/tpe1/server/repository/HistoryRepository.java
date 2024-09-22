package ar.edu.itba.pod.tpe1.server.repository;

import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.query.CaredInfo;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryRepository {
    private final Object lock = "lock";
    private final static List<CaredInfo> history = new ArrayList<>();

    public void addHistory(CaredInfo caredInfo) {
        synchronized (lock) {
            history.add(caredInfo);
        }
    }

    public void addHistory(final int roomId, Patient patient, Doctor doctor) {
        synchronized (lock) {
            CaredInfo entry = CaredInfo.newBuilder()
                    .setRoomId(roomId)
                    .setDoctor(doctor)
                    .setPatient(patient).build();
            history.add(entry);
        }
    }

    public List<CaredInfo> getHistory(int roomId) {
        synchronized (lock) {
            return history.stream()
                    .filter(caredInfo -> caredInfo.getRoomId() == roomId)
                    .collect(Collectors.toList());
        }
    }

    public List<CaredInfo> getHistory() {
        synchronized (lock) {
            return history;
        }
    }
}
