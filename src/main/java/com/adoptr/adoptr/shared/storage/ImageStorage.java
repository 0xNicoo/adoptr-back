package com.adoptr.adoptr.shared.storage;

/** Almacenamiento de imágenes públicas. En la base se guarda la {@code key}; la URL se arma al responder. */
public interface ImageStorage {

    /**
     * @throws com.adoptr.adoptr.shared.exception.custom.ServiceUnavailableException si no se pudo guardar.
     */
    void upload(String key, ProcessedImage image);

    /** No lanza si falla: la operación ya se completó y un archivo huérfano no debe volverla atrás. */
    void delete(String key);

    String publicUrl(String key);
}
