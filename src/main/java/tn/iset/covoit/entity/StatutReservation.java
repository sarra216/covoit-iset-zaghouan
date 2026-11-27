package tn.iset.covoit.entity;

/**
 * EN_ATTENTE : le passager a demandé, le conducteur n'a pas encore répondu (les places sont bloquées).
 * CONFIRMEE  : le conducteur a accepté.
 * REFUSEE    : le conducteur a refusé (les places sont rendues).
 * ANNULEE    : le passager a annulé (les places sont rendues).
 * EXPIREE    : le conducteur n'a pas répondu à temps (les places sont rendues).
 */
public enum StatutReservation {
    EN_ATTENTE,
    CONFIRMEE,
    REFUSEE,
    ANNULEE,
    EXPIREE
}
