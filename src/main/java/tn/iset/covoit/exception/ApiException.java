package tn.iset.covoit.exception;

import org.springframework.http.HttpStatus;

/** Erreur métier renvoyée au client avec un code HTTP et un message clair. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException introuvable(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException requeteInvalide(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException conflit(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException interdit(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }
}
