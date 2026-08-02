package com.bullla.pix.api.security;

public class InvalidDemoCredentialsException extends RuntimeException {

    public InvalidDemoCredentialsException() {
        super("Credenciais de demonstração inválidas");
    }
}
