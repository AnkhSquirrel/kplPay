package com.ankhsquirrel.kplpay.subscription;

import com.ankhsquirrel.kplpay.customer.CustomerNotFoundException;
import com.ankhsquirrel.kplpay.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final CustomerRepository customerRepository;

    public SubscriptionService(SubscriptionRepository repository, CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        if (!customerRepository.existsById(request.customerId())) {
            throw new CustomerNotFoundException(request.customerId());
        }

        // saveAndFlush so the INSERT runs now and the @CreationTimestamp is populated on the
        // instance we map into the response.
        Subscription saved = repository.saveAndFlush(new Subscription(
                request.customerId(),
                request.planName(),
                request.monthlyAmount(),
                request.billingCycleDay()));

        return SubscriptionResponse.from(saved);
    }
}
