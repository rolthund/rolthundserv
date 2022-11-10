package com.rolthund.customer;

public record CustomerRegistrationRequest(
        String firstName,
        String lastName,
        String email) {
}
