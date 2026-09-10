package com.ankhsquirrel.kplpay.customer;

import com.ankhsquirrel.kplpay.integration.insee.CompanyInfo;
import com.ankhsquirrel.kplpay.integration.insee.InseeClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final InseeClient inseeClient;

    public CustomerService(CustomerRepository repository, InseeClient inseeClient) {
        this.repository = repository;
        this.inseeClient = inseeClient;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (repository.existsBySiret(request.siret())) {
            throw new DuplicateSiretException(request.siret());
        }

        CompanyInfo company = inseeClient.lookup(request.siret());

        // saveAndFlush so the INSERT runs now and the @CreationTimestamp is populated on the
        // instance we map into the response.
        Customer saved = repository.saveAndFlush(new Customer(
                request.siret(),
                company.legalName(),
                company.address(),
                company.postalCode(),
                company.city(),
                request.email()));

        return CustomerResponse.from(saved);
    }
}
