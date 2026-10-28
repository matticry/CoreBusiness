package com.matticry.corebusiness.domain.port.out;

/**
 * Puerto de salida para el hasheo y verificación de contraseñas.
 * Permite mantener el dominio desacoplado de la librería criptográfica.
 */
public interface PasswordEncoderPort {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
