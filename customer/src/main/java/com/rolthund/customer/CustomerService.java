package com.rolthund.customer;

import com.rolthund.amqp.RabbitMQMessageProducer;
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

    private final CustomerRepository customerRepository;

    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer producer;

    public void registerCustomer(CustomerRegistrationRequest customerRequest) {
        Customer customer = Customer.builder()
                .firstName(customerRequest.firstName())
                .lastName(customerRequest.lastName())
                .email(customerRequest.email())
                .build();
        //todo: check if email is valid
        //todo: check if email is not taken
        customerRepository.saveAndFlush(customer);

        FraudCheckResponse fraudCkeckResponse = fraudClient.isFraudster(customer.getId());



        if (fraudCkeckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        //todo: send notification
        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome!", customer.getFirstName())

        );

        producer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );
    }
}
