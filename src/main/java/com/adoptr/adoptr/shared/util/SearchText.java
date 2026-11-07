package com.adoptr.adoptr.shared.util;

import java.text.Normalizer;
import java.util.Locale;

/** Normaliza texto para búsquedas: minúsculas, sin tildes y con espacios simples. */
public final class SearchText {

    private SearchText() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
}
