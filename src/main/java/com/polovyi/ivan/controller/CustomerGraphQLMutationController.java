package com.polovyi.ivan.controller;

import com.polovyi.ivan.dto.request.CreateCustomerRequest;
import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.dto.request.PartiallyUpdateCustomerRequest;
import com.polovyi.ivan.dto.request.UpdateCustomerRequest;
import com.polovyi.ivan.service.CustomerService;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
@Validated
@RequiredArgsConstructor
public class CustomerGraphQLMutationController implements GraphQLMutationResolver {

    private final CustomerService customerService;

    public CustomerResponse createCustomer(@Valid CreateCustomerRequest createCustomerRequest) {
        return customerService.createCustomer(createCustomerRequest);
    }

    public CustomerResponse updateCustomer(@NotNull String customerId,
            @Valid @NotNull UpdateCustomerRequest updateCustomerRequest) {
        return customerService.updateCustomer(customerId, updateCustomerRequest);
    }

    public CustomerResponse partiallyUpdateCustomer(@NotNull String customerId,
            @Valid @NotNull PartiallyUpdateCustomerRequest partiallyUpdateCustomerRequest) {
        return  customerService.partiallyUpdateCustomer(customerId, partiallyUpdateCustomerRequest);
    }

    public String deleteCustomer(@NotNull String customerId) {
        customerService.deleteCustomer(customerId);
        return customerId;
    }

}
