package com.padelconnect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class I18nValidationTest {

    @Autowired
    private MessageSource messageSource;

    @Test
    void testMensajesEnEspanol() {
        String msgNombre = messageSource.getMessage("validation.nombre.required", null, Locale.of("es"));
        String msgMatchFull = messageSource.getMessage("error.match.full", null, Locale.of("es"));
        String msgPasswordMismatch = messageSource.getMessage("error.password.mismatch", null, Locale.of("es"));

        assertEquals("El nombre es obligatorio", msgNombre);
        assertEquals("¡No hay cupos disponibles!", msgMatchFull);
        assertEquals("Las contraseñas no coinciden", msgPasswordMismatch);
    }

    @Test
    void testMensajesEnIngles() {
        String msgNombre = messageSource.getMessage("validation.nombre.required", null, Locale.ENGLISH);
        String msgMatchFull = messageSource.getMessage("error.match.full", null, Locale.ENGLISH);
        String msgPasswordMismatch = messageSource.getMessage("error.password.mismatch", null, Locale.ENGLISH);

        assertEquals("Name is required", msgNombre);
        assertEquals("No slots available!", msgMatchFull);
        assertEquals("Passwords do not match", msgPasswordMismatch);
    }
}
