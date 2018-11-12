package com.common.entity;

/**
 * Авторизация клиента
 */
public class AuthRequest extends AbstractMessage{

    private String jwt;

    public AuthRequest(String jwt) {
        this.jwt = jwt;
    }

    public boolean isValid(String secretKey) {
        return true;
    }

}
