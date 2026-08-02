package com.bullla.pix.application.port;

public interface IPixTransactionPublisher {

    void enqueue(String transactionId);
}
