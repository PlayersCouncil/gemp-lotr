package com.gempukku.lotro.argonauth;

/** A call to Argonauth that did not give an answer Gemp can act on. */
public class ArgonauthException extends Exception {
    public enum Reason {
        /** No answer, a late one, or one that says try again: nothing is known. */
        UNAVAILABLE,
        /** An answer that is refused or cannot be trusted: a bad token, a wrong issuer, a spent code. */
        REJECTED
    }

    private final Reason _reason;

    public ArgonauthException(Reason reason, String message) {
        super(message);
        _reason = reason;
    }

    public ArgonauthException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        _reason = reason;
    }

    public Reason getReason() {
        return _reason;
    }
}
