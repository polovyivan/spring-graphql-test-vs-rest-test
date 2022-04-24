package com.polovyi.ivan;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.polovyi.ivan.dto.CustomerResponse;
import com.polovyi.ivan.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CustomerGraphQLQueryControllerTest {

    private final static String GRAPHQL_QUERY_REQUEST_PATH = "/graphql/request/%s.query";

    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;

    @MockBean
    private CustomerService customerService;

    private GraphQLResponse response;

    private CustomerResponse customerResponse;

    @Test
    public void testGetCustomersAPIMethod() throws Exception {
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersReturnsListOgCustomers();
        whenGetCustomersAPICalled();
        thenExpectResponseHasOkStatus();
        thenExpectResponseWithCustomerList();
    }


    /*
     * GIVEN Methods
     */

    private void givenCustomerResponse() {
        customerResponse = CustomerResponse.builder()
                .id("1")
                .fullName("Ivan Polovyi")
                .address("Address")
                .phoneNumber("1-669-210-0504")
                .createdAt(LocalDate.now())
                .build();
    }

    private void givenCustomerServiceGetAllCustomersReturnsListOgCustomers() {
        doReturn(List.of(customerResponse)).when(customerService).getAllCustomers();
    }


    /*
     * WHEN Methods
     */

    private void whenGetCustomersAPICalled() throws Exception {
        response = graphQLTestTemplate.postForResource(String.format(GRAPHQL_QUERY_REQUEST_PATH, "getAllCustomers"));
    }

    /*
     * THEN Methods
     */

    private void thenExpectResponseHasOkStatus() {
        assertEquals(HttpStatus.OK, response.getRawResponse().getStatusCode());

    }

    private void thenExpectResponseWithCustomerList() {
        List<CustomerResponse> getAllCustomers = response.getList("data.getAllCustomers",
                CustomerResponse.class);
        assertTrue(getAllCustomers.size() == 1);
        assertTrue(getAllCustomers.contains(customerResponse));
    }

}
