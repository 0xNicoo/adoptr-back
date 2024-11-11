package com.example.adoptr_backend.exception.error;

public enum Error implements ErrorCode {

    EXAMPLE_NOT_FOUND("0001","No se encontro el ejemplo"),
    PROFILE_ALREADY_EXIST("0002", "Este usuario ya tiene un perfil"),
    AUTH_ERROR("0003", "Credenciales invalidas"),
    PROVINCE_NOT_FOUND("0004", "No se encontro la provincia"),
    LOCALITY_NOT_FOUND("0005", "No se encontro la localidad"),
    PROFILE_UPDATE_NOT_FOUND("0006", "No tienes permiso para editar este perfil"),
    PROFILE_NOT_FOUND("0007", "El perfil no existe"),
    PUBLICATION_NOT_FOUND("0008", "No se encontro la publicacion"),
    CHAT_NOT_FOUND("0009", "No se encontro el chat"),
    USER_NOT_IN_CHAT("0010", "El usuario no pertenece al chat"),
    CHAT_NOT_FOUND_FOR_PUBLICATION("0011", "No se encontro ningun chat para la publicacion."),
    USER_NOT_ADOPTION_OWNER("0012", "El usuario no es dueño de la publicacion de adopcion"),
    USER_NOT_FOUND("0013", "No se encontro el usuario"),
    USER_NOT_LOST_OWNER("0014", "El usuario no es dueño de la publicacion de perdidos"),
    SERVICE_TYPE_NOT_FOUND("0015", "No se encontro el tipo de servicio"),
    SERVICE_NOT_FOUND("0016", "No se encontro el servicio"),
    USER_NOT_SERVICE_OWNER("0017", "El usuario no es dueño de la publicacion de servicio"),
    USER_NOT_POST_OWNER("0018", "El usuario no es el dueño del post"),
    USER_ALREADY_REPORTED_PUBLICATION("0019", "El usuario ya reporto la publicacion"),
    USER_CAN_NOT_REPORT_PUBLICATION("0020", "El usuario no puede reportar esta publicacion"),
    USER_CAN_NOT_REPORT_PROFILE("0021", "El usuario no puede reportar este perfil"),
    USER_ALREADY_REPORTED_PROFILE("0022", "El usuario ya reporto el perfil"),
    POST_NOT_FOUND("0023", "No se encontro el post"),
    USER_ALREADY_REPORTED_POST("0024", "El usuario ya reporto el post"),
    USER_CAN_NOT_REPORT_POST("0025", "El usuario no puede reportar este post"),

    CONTACT_USER_NOT_FOUND("0026", "No se encontró al usuario contactante"),
    USER_NOT_HAVE_FIREBASE_TOKEN("0027", "El usuario no tiene token de firebase"),
    NOTIFICATION_SAME_USER("0028", "No se puede mandar notificacion a el mismo")
    ;

    private final String code;

    private final String message;

    Error(String code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return code;
    }
}
