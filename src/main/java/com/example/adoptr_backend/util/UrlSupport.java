package com.example.adoptr_backend.util;

import com.example.adoptr_backend.model.Post;
import com.example.adoptr_backend.model.Publication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlSupport {
    private static String adoptionUrl;
    private static String lostUrl;
    private static String serviceUrl;
    private  static String profileUrl;
    private  static String postUrl;

    public UrlSupport(@Value("${publication.adoption.url}") String adoptionUrl,
                      @Value("${publication.lost.url}") String lostUrl,
                      @Value("${publication.service.url}") String serviceUrl,
                      @Value("${profile.url}") String profileUrl,
                      @Value("${post.url}") String postUrl) {
        UrlSupport.adoptionUrl = adoptionUrl;
        UrlSupport.lostUrl = lostUrl;
        UrlSupport.serviceUrl = serviceUrl;
        UrlSupport.profileUrl = profileUrl;
        UrlSupport.postUrl= postUrl;
    }

    public static String buildPublicationURL(Publication publication){
        return switch (publication.getType()){
            case ADOPTION -> adoptionUrl + publication.getId();
            case LOST -> lostUrl + publication.getId();
            case SERVICE -> serviceUrl + publication.getId();
        };
    }

    public static String buildProfileURL(Long userId) {
        return profileUrl + userId;
    }

    public static String buildPostURL(Long postId) {
        return postUrl + postId;
    }
}
