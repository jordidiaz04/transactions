package com.nttdata.transactions.exceptions.customs;

/**
 * Object that returns a message in case an exception occurs.
 */
public class CustomInformationException extends RuntimeException {
  public CustomInformationException(String message) {
    super(message);
  }
}
