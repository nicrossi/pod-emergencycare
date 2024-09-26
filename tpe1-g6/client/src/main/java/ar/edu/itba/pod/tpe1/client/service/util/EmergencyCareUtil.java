package ar.edu.itba.pod.tpe1.client.service.util;

import ar.edu.itba.pod.tpe1.emergencyCare.CarePatientRequest;
import ar.edu.itba.pod.tpe1.emergencyCare.DischargePatientRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmergencyCareUtil {
    public static CarePatientRequest getCarePatientRequest() {
        final String roomId = Validate.notBlank(System.getProperty("room"), "Room is required");
        return CarePatientRequest.newBuilder()
                .setRoom(Integer.parseInt(roomId))
                .build();
    }

    public static DischargePatientRequest getDischargePatientRequest() {
        final String roomId = Validate.notBlank(System.getProperty("room"), "Room is required");
        final String patient = Validate.notBlank(System.getProperty("patient"), "Patient is required");
        final String doctor = Validate.notBlank(System.getProperty("doctor"), "Doctor is required");

        return DischargePatientRequest.newBuilder()
                .setRoom(Integer.parseInt(roomId))
                .setDoctorName(doctor)
                .setPatientName(patient)
                .build();
    }
}
