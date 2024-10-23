package com.example.adoptr_backend.util;

import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.model.Profile;
import com.example.adoptr_backend.model.Publication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReportSupport {
    private static String adoptionUrl;
    private static String lostUrl;
    private static String serviceUrl;
    private  static String profileUrl;
    private  static String postUrl;

    public ReportSupport(@Value("${publication.adoption.url}") String adoptionUrl,
                         @Value("${publication.lost.url}") String lostUrl,
                         @Value("${publication.service.url}") String serviceUrl,
                         @Value("${profile.url}") String profileUrl) {
        ReportSupport.adoptionUrl = adoptionUrl;
        ReportSupport.lostUrl = lostUrl;
        ReportSupport.serviceUrl = serviceUrl;
        ReportSupport.profileUrl = profileUrl;
    }

    public static String buildPublicationURL(Publication publication){
        return switch (publication.getType()){
            case ADOPTION -> adoptionUrl + publication.getId();
            case LOST -> lostUrl + publication.getId();
            case SERVICE -> serviceUrl + publication.getId();
        };
    }

    public static String buildProfileURL(Profile profile) {
        return profileUrl + profile.getId();
    }

    public static String buildPostURL(Post post) {
        return postUrl + post.getId();
    }
}
