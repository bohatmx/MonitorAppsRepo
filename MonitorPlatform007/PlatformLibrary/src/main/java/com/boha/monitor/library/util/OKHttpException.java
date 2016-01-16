package com.boha.monitor.library.util;

/**
 * Created by aubreyM on 16/01/15.
 */

public class OKHttpException
        extends Exception {
    public String msg;
    public OKHttpException(String message) {
        msg = message;
    }
}
