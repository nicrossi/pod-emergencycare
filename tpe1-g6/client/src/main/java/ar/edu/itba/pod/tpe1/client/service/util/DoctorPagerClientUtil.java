package ar.edu.itba.pod.tpe1.client.service.util;

import ar.edu.itba.pod.tpe1.doctorPager.DoctorPagerRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DoctorPagerClientUtil {
    public static DoctorPagerRequest getDoctorPagerRequest() {
        final String doctorName = System.getProperty("doctor");
        return DoctorPagerRequest.newBuilder()
                .setDoctorName(doctorName)
                .build();
    }
}
