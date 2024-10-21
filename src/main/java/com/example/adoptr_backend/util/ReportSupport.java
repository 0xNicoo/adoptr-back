package com.example.adoptr_backend.util;

import com.example.adoptr_backend.model.Publication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReportSupport {
    private static String adoptionUrl;
    private static String lostUrl;
    private static String serviceUrl;

    public ReportSupport(@Value("${publication.adoption.url}") String adoptionUrl,
                         @Value("${publication.lost.url}") String lostUrl,
                         @Value("${publication.service.url}") String serviceUrl){
        ReportSupport.adoptionUrl = adoptionUrl;
        ReportSupport.lostUrl = lostUrl;
        ReportSupport.serviceUrl = serviceUrl;
    }

    public static String buildPublicationURL(Publication publication){
        return switch (publication.getType()){
            case ADOPTION -> adoptionUrl + publication.getId();
            case LOST -> lostUrl + publication.getId();
            case SERVICE -> serviceUrl + publication.getId();
        };
    }

}
