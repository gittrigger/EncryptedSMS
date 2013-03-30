package com.rsasms;

public class RsaSmsException extends Exception {
    private static final long serialVersionUID = -7323249827281485390L;

    /**
     * Creates a new SmsException.
     */
    public RsaSmsException() {
        super();
    }

    /**
     * Creates a new SmsException with the specified detail message.
     *
     * @param message the detail message.
     */
    public RsaSmsException(String message) {
        super(message);
    }

    /**
     * Creates a new SmsException with the specified cause.
     *
     * @param cause the cause.
     */
    public RsaSmsException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new SmsException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public RsaSmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
