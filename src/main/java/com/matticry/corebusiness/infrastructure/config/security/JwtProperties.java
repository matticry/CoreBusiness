package com.matticry.corebusiness.infrastructure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para la seguridad JWT y OAuth2 Resource Server.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String issuer = "corebusiness-auth";
    private String audience = "corebusiness-api";
    private int accessTokenExpirationMinutes = 15;
    private int refreshTokenExpirationDays = 7;
    private String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
}
