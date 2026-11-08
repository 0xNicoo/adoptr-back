package com.adoptr.adoptr.publication.model;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica contra PostgreSQL real las columnas que Hibernate genera para la base
 * común de publicaciones. Importa en particular que el autor siga en {@code user_id}:
 * con {@code ddl-auto=update}, una columna nueva dejaría la vieja {@code user_id NOT NULL}
 * en las bases existentes y haría fallar todo insert.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PublicationSchemaTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void tablaPublicationTieneLasColumnasDeLaBaseComun() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                select column_name, is_nullable
                from information_schema.columns
                where table_schema = current_schema() and table_name = 'publication'
                """).getResultList();
        Map<String, String> nullableByColumn = rows.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (String) r[1]));

        assertThat(nullableByColumn).containsOnlyKeys(
                "id", "type", "description", "user_id", "locality_id",
                "created_at", "updated_at", "deleted_at");
        assertThat(nullableByColumn)
                .containsEntry("type", "NO")
                .containsEntry("description", "NO")
                .containsEntry("user_id", "NO")
                .containsEntry("created_at", "NO")
                .containsEntry("updated_at", "NO")
                .containsEntry("locality_id", "YES")
                .containsEntry("deleted_at", "YES");
    }
}
