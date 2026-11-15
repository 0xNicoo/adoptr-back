package com.adoptr.adoptr.chat.model;

/**
 * Lugar de una persona en una conversación: {@code OWNER} es quien publicó (la contactaron),
 * {@code INTERESTED} es quien escribió por la publicación (contactó). No se guarda: depende de quién mira.
 */
public enum ChatRole {
    OWNER, INTERESTED
}
