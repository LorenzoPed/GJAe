package com.laundry.app.dto;



/**

* Simple status DTO describing a service and its state.

*

* @param service the service name

* @param state current state of the service

*/

public record Status(String service, String state) {}