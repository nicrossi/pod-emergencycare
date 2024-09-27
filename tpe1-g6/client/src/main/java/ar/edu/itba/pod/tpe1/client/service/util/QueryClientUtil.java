package ar.edu.itba.pod.tpe1.client.service.util;

import ar.edu.itba.pod.tpe1.administration.Doctor;
import ar.edu.itba.pod.tpe1.query.CaredInfo;
import ar.edu.itba.pod.tpe1.query.QueryRequest;
import ar.edu.itba.pod.tpe1.query.QueryRoomInfo;
import ar.edu.itba.pod.tpe1.waitingRoom.Patient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.Optional;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryClientUtil {
    @FunctionalInterface
    public interface Mapper<T> {
        String map(T t);
    }

    public static QueryRequest getQueryRequest() {
        final Optional<String> room = Optional.ofNullable(System.getProperty("room"));
        final QueryRequest.Builder request = QueryRequest.newBuilder();
        room.ifPresent(id -> request.setRoom(Integer.parseInt(id)));

        return request.build();
    }

    public static String[] QueryRoomsHeaders = {"Room", "Status", "Patient", "Doctor"};

    public static String mapQueryRoomInfo(QueryRoomInfo queryRoomInfo) {
        StringBuilder builder = new StringBuilder();
        builder.append(queryRoomInfo.getRoomId()).append(",").append(queryRoomInfo.getStatus()).append(",");
        if (queryRoomInfo.hasPatient()) {
            Patient patient = queryRoomInfo.getPatient();
            builder.append(patient.getPatientName()).append(" (").append(patient.getLevel()).append(")");
        }
        builder.append(",");
        if (queryRoomInfo.hasDoctor()) {
            Doctor doctor = queryRoomInfo.getDoctor();
            builder.append(doctor.getName()).append(" (").append(doctor.getLevel()).append(")");
        }
//        builder.append("\n");
        return builder.toString();
    }

    public static String[] QueryWaitingRoomHeaders = {"Patient", "Level"};

    public static String mapQueryWaitingRoomInfo(Patient patient) {
        return patient.getPatientName() + "," + patient.getLevel();
    }

    public static String[] QueryCaresHeaders = {"Room", "Patient", "Doctor"};

    public static String mapCaredInfo(CaredInfo caredInfo) {
        StringBuilder builder = new StringBuilder();
        Patient patient = caredInfo.getPatient();
        Doctor doctor = caredInfo.getDoctor();
        builder.append(caredInfo.getRoomId()).append(",").append(patient.getPatientName()).append(" (").append(patient.getLevel()).append("),").append(doctor.getName()).append(" (").append(doctor.getLevel()).append(")");
//        builder.append("\n");
        return builder.toString();
    }

    public static <T> String convertToCSV(String[] headers, Iterable<T> data, Mapper<T> mapper) {
        StringBuilder builder = new StringBuilder();
        //headers
        builder.append(String.join(",", headers));
        builder.append("\n");
        //data
        for (T item : data) {
            builder.append(mapper.map(item));
            builder.append("\n");
        }

        return builder.toString();
    }
}
