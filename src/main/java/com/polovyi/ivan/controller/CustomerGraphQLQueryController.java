package com.polovyi.ivan.controller;

import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.service.CustomerService;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
@Component
@RequiredArgsConstructor
public class CustomerGraphQLQueryController implements GraphQLQueryResolver {

    private final CustomerService customerService;

    public List<CustomerResponse> allCustomers() {
        return customerService.getAllCustomers();
    }

    public List<CustomerResponse> allCustomersWithFilters(
            String fullName,
            String phoneNumber,
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdAt) {
        return customerService.getCustomersWithFilters(fullName, phoneNumber, createdAt);
    }

}
