package com.rolthund.customer;

import com.rolthund.clients.fraud.FraudCheckResponse;
import com.rolthund.clients.fraud.FraudClient;
import com.rolthund.clients.notification.NotificationClient;
import com.rolthund.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CustomerService {

    private final RestTemplate restTemplate;

    private final CustomerRepository customerRepository;

    private final FraudClient fraudClient;
    private final NotificationClient notificationClient;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        customerRepository.saveAndFlush(customer);


        FraudCheckResponse fraudCheckResponse =
                fraudClient.isFraudster(customer.getId());

        if(fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        notificationClient.sendNotification(new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi, %s", customer.getFirstName())
        ));
    }
}
