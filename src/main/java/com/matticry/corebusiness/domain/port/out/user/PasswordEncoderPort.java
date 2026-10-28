package com.matticry.corebusiness.domain.port.out.user;

/**
 * Puerto de salida para el hasheo y verificación de contraseñas.
 */
public interface PasswordEncoderPort {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
