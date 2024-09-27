package ar.edu.itba.pod.tpe1.client.service.util;

import ar.edu.itba.pod.tpe1.administration.AddDoctorRequest;
import ar.edu.itba.pod.tpe1.administration.AvailabilityStatus;
import ar.edu.itba.pod.tpe1.administration.SetDoctorRequest;
import com.google.protobuf.StringValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdministrationClientUtil {

    public static AddDoctorRequest getAddDoctorRequest() {
        final String doctorName = Validate.notBlank(System.getProperty("doctor"), "Doctor name is required");
        final String levelStr = System.getProperty("level");
        final int level = levelStr != null ? Integer.parseInt(levelStr) : 1;

        return AddDoctorRequest.newBuilder()
                .setDoctorName(doctorName)
                .setLevel(level)
                .build();
    }

    public static String getAvailability(AvailabilityStatus availabilityStatus) {
        return switch (availabilityStatus) {
            case AVAILABILITY_STATUS_AVAILABLE -> "Available";
            case AVAILABILITY_STATUS_UNAVAILABLE -> "Unavailable";
            case AVAILABILITY_STATUS_ATTENDING -> "Attending";
            default -> "Unspecified";
        };
    }

    private static AvailabilityStatus getAvailabilityStatus(String in) {
        return switch (in.toLowerCase()) {
            case "available" -> AvailabilityStatus.AVAILABILITY_STATUS_AVAILABLE;
            case "unavailable" -> AvailabilityStatus.AVAILABILITY_STATUS_UNAVAILABLE;
            case "attending" -> AvailabilityStatus.AVAILABILITY_STATUS_ATTENDING;
            default -> AvailabilityStatus.AVAILABILITY_STATUS_UNSPECIFIED;
        };
    }

    public static SetDoctorRequest getSetDoctorRequest() {
        final String doctorName = Validate.notBlank(System.getProperty("doctor"), "Doctor name is required");
        final String availability = Validate.notBlank(System.getProperty("availability"), "Availability is required");


        return SetDoctorRequest.newBuilder()
                .setDoctorName(doctorName)
                .setAvailability(getAvailabilityStatus(availability))
                .build();
    }

    public static StringValue getCheckDoctorRequest() {
        final String doctorName = Validate.notBlank(System.getProperty("doctor"), "Doctor name is required");
        return StringValue.of(doctorName);
    }
}
