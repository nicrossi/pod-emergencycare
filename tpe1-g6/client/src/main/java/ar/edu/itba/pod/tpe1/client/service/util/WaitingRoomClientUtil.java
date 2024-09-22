package ar.edu.itba.pod.tpe1.client.service.util;

import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import ar.edu.itba.pod.tpe1.waitingRoom.PatientCheck;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WaitingRoomClientUtil {

    public static Patient getAddPatientRequest() {
        final String name = Validate.notBlank(System.getProperty("patient"), "Patient name is required");
        final String levelStr = System.getProperty("level");
        final int level = levelStr != null ? Integer.parseInt(levelStr) : 1;
        return Patient.newBuilder()
                .setPatientName(name)
                .setLevel(level)
                .build();
    }

    public static Patient getUpdateLevelRequest() {
        return getAddPatientRequest();
    }

    public static PatientCheck getCheckPatientRequest() {
        final String name = Validate.notBlank(System.getProperty("patient"), "Patient name is required");
        return PatientCheck.newBuilder()
                .setPatientName(name)
                .build();
    }
}
