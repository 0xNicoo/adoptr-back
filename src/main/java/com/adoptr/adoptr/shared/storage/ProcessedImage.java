package com.adoptr.adoptr.shared.storage;

/** Imagen lista para guardar: sin metadatos y en el formato final. */
public record ProcessedImage(byte[] bytes, String contentType, String extension) {
}
