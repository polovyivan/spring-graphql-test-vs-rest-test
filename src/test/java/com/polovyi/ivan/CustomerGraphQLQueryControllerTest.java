package com.polovyi.ivan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.polovyi.ivan.dto.request.CreateCustomerRequest;
import com.polovyi.ivan.dto.request.PartiallyUpdateCustomerRequest;
import com.polovyi.ivan.dto.request.UpdateCustomerRequest;
import com.polovyi.ivan.dto.response.CustomerResponse;
import com.polovyi.ivan.service.CustomerService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CustomerGraphQLQueryControllerTest {

    private final static String GRAPHQL_QUERY_REQUEST_PATH = "/graphql/request/%s.query";
    private final static String CUSTOMER_ID = UUID.randomUUID().toString();
    private static ObjectMapper mapper;

    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;

    @MockBean
    private CustomerService customerService;

    private CustomerResponse customerResponse;

    private CreateCustomerRequest createCustomerRequest;

    private UpdateCustomerRequest updateCustomerRequest;

    private PartiallyUpdateCustomerRequest partiallyUpdateCustomerRequest;

    private GraphQLResponse response;

    private String fullName;
    private String phoneNumber;
    private String createdAt;

    @BeforeAll
    public static void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

     /*
    POST All Customers
     */

    @Test
    public void shouldReturnListOfCustomersFromGetAllCustomersGraphQLAPI() throws Exception {
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersReturnsListOgCustomers();
        whenPostForAllCustomersGraphQLAPICalled();
        thenExpectResponseHasOkStatus();
        thenExpectCustomerServiceGetAllCustomersCalledOnce();
        thenExpectResponseWithCustomerList();
    }

    /*
    POST /v1/customers-with-filters
     */

    @Test
    public void shouldReturnListOfCustomersFromGetAllCustomersWithFiltersAPI() throws Exception {
        givenAllAPIMethodParameters();
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers();
        whenPostForAllCustomersWithFiltersGraphQLAPICalled();
        thenExpectCustomerServiceGetCustomersWithFiltersCalledOnce();
        thenExpectResponseHasOkStatus();
        thenExpectResponseWithCustomerListFromAllCustomersWithFilters();
    }

    @Test
    public void shouldNotReturnListOfCustomersFromGetAllCustomersWithFiltersAPIGivenInvalidDateFormat()
            throws Exception {
        givenAllAPIMethodParametersWithInvalidDateFormat();
        givenCustomerResponse();
        givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers();
        whenPostForAllCustomersWithFiltersGraphQLAPICalled();
        thenExpectNoCallToCustomerServiceGetCustomersWithFilters();
        thenExpectResponseHasOkStatus();
        thenExpectResponseWithFieldInvalidFormatErrorMessage();
        thenExpectResponseWithBadRequestErrorCode();
    }

        /*
        POST /v1/customers
         */

    @Test
    public void shouldCreateCustomer() throws Exception {
        givenCustomerResponse();
        givenValidCreateCustomerRequest();
        givenCustomerServiceCreateCustomerReturnsCustomerResponse();
        whenPostCreateCustomerGraphQLAPICalled();
        thenExpectCustomerServiceCreateCustomerCalledOnce();
        thenExpectCreateCustomerResponseWithCustomer();
        thenExpectResponseHasOkStatus();
    }

    @Test
    public void shouldNotCreateCustomerGivenRequestWithoutRequiredFields() throws Exception {
        givenCreateCustomerRequestWithoutRequiredFields();
        whenPostCreateCustomerGraphQLAPICalled();
        thenExpectNoCallToCustomerServiceCreateCustomer();
        thenExpectResponseHasOkStatus();
        thenExpectResponseWithThreeBadRequestErrorCodes();
        thenExpectResponseWithRequiredFieldErrorMessages();
    }

        /*
        PUT /v1/customers
         */

    @Test
    public void shouldUpdateCustomer() throws Exception {
        givenCustomerResponse();
        givenValidUpdateCustomerRequest();
        givenCustomerServiceUpdateCustomerReturnsCustomerResponse();
        whenUpdateCustomersAPICalled();
        thenExpectCustomerServiceUpdateCustomerCalledOnce();
        thenExpectUpdateCustomerResponseWithCustomer();
        thenExpectResponseHasOkStatus();
    }

    @Test
    public void shouldNotUpdateCustomerGivenRequestWithoutRequiredFields() throws Exception {
        givenUpdateCustomerRequestWithoutRequiredFields();
        whenUpdateCustomersAPICalled();
        thenExpectNoCallToCustomerServiceUpdateCustomer();
        thenExpectResponseHasOkStatus();
        thenExpectResponseWithThreeBadRequestErrorCodes();
        thenExpectResponseWithRequiredFieldErrorMessages();
    }

        /*
        PATCH /v1/customers
         */

    @Test
    public void shouldPartiallyUpdateCustomer() throws Exception {
        givenCustomerResponse();
        givenValidPartiallyUpdateCustomerRequest();
        givenCustomerServicePartiallyUpdateCustomerReturnsCustomerResponse();
        whenPartiallyUpdateCustomersAPICalled();
        thenExpectCustomerServicePartiallyUpdateCustomerCalledOnce();
        thenExpectPartiallyUpdateCustomerResponseWithCustomer();
        thenExpectResponseHasOkStatus();
    }

        /*
        DELETE /v1/customers
         */

    @Test
    public void shouldDeleteCustomer() throws Exception {
        givenCustomerServiceDeleteCustomerReturnsNothing();
        whenDeleteCustomersAPICalled();
        thenExpectCustomerServiceDeleteCustomerCalledOnce();
        thenExpectResponseHasOkStatus();
        thenExpectDeleteCustomerResponseWithCustomerId();
    }

    /*
     * GIVEN Methods
     */

    private void givenCustomerResponse() {
        customerResponse = CustomerResponse.builder()
                .id(CUSTOMER_ID)
                .fullName("Ivan Polovyi")
                .address("Address")
                .phoneNumber("1-669-210-0504")
                .createdAt(LocalDate.now())
                .build();
    }

    private void givenCustomerServiceGetAllCustomersReturnsListOgCustomers() {
        doReturn(List.of(customerResponse)).when(customerService).getAllCustomers();
    }

    private void givenAllAPIMethodParameters() {
        fullName = "Ivan Polovyi";
        phoneNumber = "626.164.7481";
        createdAt = "2015-09-01";
    }

    private void givenAllAPIMethodParametersWithInvalidDateFormat() {
        givenAllAPIMethodParameters();
        createdAt = "09-01-2015";
    }

    private void givenValidCreateCustomerRequest() {
        createCustomerRequest = CreateCustomerRequest.builder()
                .fullName("Ivan Polovyi")
                .phoneNumber("626.164.7481")
                .address("Apt. 843 399 Lachelle Crossing, New Eldenhaven, LA 63962-9260")
                .build();
    }

    private void givenValidUpdateCustomerRequest() {
        updateCustomerRequest = UpdateCustomerRequest.builder()
                .fullName("Ivan Polovyi")
                .phoneNumber("626.164.7481")
                .address("Apt. 843 399 Lachelle Crossing, New Eldenhaven, LA 63962-9260")
                .build();
    }

    private void givenValidPartiallyUpdateCustomerRequest() {
        partiallyUpdateCustomerRequest = PartiallyUpdateCustomerRequest.builder()
                .phoneNumber("626.164.7481")
                .build();
    }

    private void givenCustomerServiceCreateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).createCustomer(createCustomerRequest);
    }

    private void givenCustomerServiceUpdateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).updateCustomer(CUSTOMER_ID, updateCustomerRequest);
    }

    private void givenCustomerServicePartiallyUpdateCustomerReturnsCustomerResponse() {
        doReturn(customerResponse).when(customerService).partiallyUpdateCustomer(CUSTOMER_ID, partiallyUpdateCustomerRequest);
    }

    private void givenCustomerServiceDeleteCustomerReturnsNothing() {
        doNothing().when(customerService).deleteCustomer(CUSTOMER_ID);
    }

    private void givenCustomerServiceGetAllCustomersWithFiltersReturnsListOfCustomers() {
        doReturn(List.of(customerResponse)).when(customerService)
                .getCustomersWithFilters(any(), any(), any());
    }

    private void givenCreateCustomerRequestWithoutRequiredFields() {
        givenValidCreateCustomerRequest();
        createCustomerRequest.setFullName(null);
        createCustomerRequest.setPhoneNumber(null);
        createCustomerRequest.setAddress(null);
    }

    private void givenUpdateCustomerRequestWithoutRequiredFields() {
        givenValidUpdateCustomerRequest();
        updateCustomerRequest.setFullName(null);
        updateCustomerRequest.setPhoneNumber(null);
        updateCustomerRequest.setAddress(null);
    }

    /*
     * WHEN Methods
     */

    private void whenPostForAllCustomersGraphQLAPICalled() throws Exception {
        response = graphQLTestTemplate.postForResource(String.format(GRAPHQL_QUERY_REQUEST_PATH, "allCustomers"));
    }

    private void whenPostForAllCustomersWithFiltersGraphQLAPICalled() throws Exception {
        String queryVariables = String.format("""
                  {
                  "fullName": "%s",
                  "phoneNumber": "%s",
                  "createdAt": "%s"
                }
                """, fullName, phoneNumber, createdAt);
        ObjectNode jsonNodes = new ObjectMapper().readValue(queryVariables, ObjectNode.class);
        response = graphQLTestTemplate.perform(String.format(GRAPHQL_QUERY_REQUEST_PATH, "allCustomersWithFilters"),
                jsonNodes);
    }

    private void whenPostCreateCustomerGraphQLAPICalled() throws Exception {
        String jsonString = objectToJsonString(createCustomerRequest);
        String queryVariables = String.format("""
                    {
                        "createCustomerRequest": %s
                    }
                """, jsonString);
        ObjectNode jsonNodes = new ObjectMapper().readValue(queryVariables, ObjectNode.class);
        response = graphQLTestTemplate.perform(String.format(GRAPHQL_QUERY_REQUEST_PATH, "createCustomer"),
                jsonNodes);
    }

    private void whenUpdateCustomersAPICalled() throws Exception {
        String jsonString = objectToJsonString(updateCustomerRequest);
        String queryVariables = String.format("""
                    {
                        "customerId" : "%s",
                        "updateCustomerRequest": %s
                    }
                """, CUSTOMER_ID, jsonString);
        ObjectNode jsonNodes = new ObjectMapper().readValue(queryVariables, ObjectNode.class);
        response = graphQLTestTemplate.perform(String.format(GRAPHQL_QUERY_REQUEST_PATH, "updateCustomer"),
                jsonNodes);
    }

    private void whenPartiallyUpdateCustomersAPICalled() throws Exception {
        String jsonString = objectToJsonString(partiallyUpdateCustomerRequest);
        String queryVariables = String.format("""
                    {
                        "customerId" : "%s",
                        "partiallyUpdateCustomerRequest": %s
                    }
                """, CUSTOMER_ID, jsonString);
        ObjectNode jsonNodes = new ObjectMapper().readValue(queryVariables, ObjectNode.class);
        response = graphQLTestTemplate.perform(String.format(GRAPHQL_QUERY_REQUEST_PATH, "partiallyUpdateCustomer"),
                jsonNodes);
    }

    private void whenDeleteCustomersAPICalled() throws Exception {
        String queryVariables = String.format("""
                    {
                        "customerId" : "%s"
                    }
                """, CUSTOMER_ID);
        ObjectNode jsonNodes = new ObjectMapper().readValue(queryVariables, ObjectNode.class);
        response = graphQLTestTemplate.perform(String.format(GRAPHQL_QUERY_REQUEST_PATH, "deleteCustomer"),
                jsonNodes);
    }


    /*
     * THEN Methods
     */

    private void thenExpectResponseHasOkStatus() {
        assertEquals(HttpStatus.OK, response.getRawResponse().getStatusCode());

    }

    private void thenExpectResponseWithCustomerList() {
        List<CustomerResponse> getAllCustomers = response.getList("data.allCustomers",
                CustomerResponse.class);
        assertTrue(getAllCustomers.size() == 1);
        assertTrue(getAllCustomers.contains(customerResponse));
    }

    private void thenExpectResponseWithCustomerListFromAllCustomersWithFilters() {
        List<CustomerResponse> getAllCustomers = response.getList("data.allCustomersWithFilters",
                CustomerResponse.class);
        assertTrue(getAllCustomers.size() == 1);
        assertTrue(getAllCustomers.contains(customerResponse));
    }

    private void thenExpectResponseWithFieldInvalidFormatErrorMessage() {
        List<String> getAllCustomers = response.getList("errors[*].message",
                String.class);
        assertTrue(getAllCustomers.size() == 1);
        assertTrue(getAllCustomers.contains("Field createdAt has an invalid format."));
    }

    private void thenExpectResponseWithRequiredFieldErrorMessages() {
        List<String> errors = response.getList("errors[*].message",
                String.class);
        assertTrue(errors.size() == 3);
        assertTrue(errors.contains("Field fullName cannot be null"));
        assertTrue(errors.contains("Field phoneNumber cannot be null"));
        assertTrue(errors.contains("Field address cannot be null"));
    }

    private void thenExpectResponseWithThreeBadRequestErrorCodes() {
        List<Integer> getAllCustomers = response.getList("errors[*].extensions.errorCode",
                Integer.class);
        assertTrue(getAllCustomers.size() == 3);
        assertTrue(getAllCustomers.contains(HttpStatus.BAD_REQUEST.value()));
    }

    private void thenExpectResponseWithBadRequestErrorCode() {
        List<Integer> getAllCustomers = response.getList("errors[*].extensions.errorCode",
                Integer.class);
        assertTrue(getAllCustomers.size() == 1);
        assertTrue(getAllCustomers.contains(HttpStatus.BAD_REQUEST.value()));
    }

    private void thenExpectCreateCustomerResponseWithCustomer() {
        CustomerResponse customer = response.get("data.createCustomer",
                CustomerResponse.class);
        assertTrue(customer.equals(customerResponse));
    }

    private void thenExpectUpdateCustomerResponseWithCustomer() {
        CustomerResponse customer = response.get("data.updateCustomer",
                CustomerResponse.class);
        assertTrue(customer.equals(customerResponse));
    }

    private void thenExpectPartiallyUpdateCustomerResponseWithCustomer() {
        CustomerResponse customer = response.get("data.partiallyUpdateCustomer",
                CustomerResponse.class);
        assertTrue(customer.equals(customerResponse));
    }

    private void thenExpectDeleteCustomerResponseWithCustomerId() {
        String customerId = response.get("data.deleteCustomer",
                String.class);
        assertTrue(customerId.equals(CUSTOMER_ID));
    }

    private void thenExpectCustomerServiceGetAllCustomersCalledOnce() {
        verify(customerService).getAllCustomers();
    }

    private void thenExpectCustomerServiceGetCustomersWithFiltersCalledOnce() {
        verify(customerService).getCustomersWithFilters(anyString(), anyString(), any());
    }

    private void thenExpectNoCallToCustomerServiceGetCustomersWithFilters() {
        verify(customerService, times(0)).getCustomersWithFilters(anyString(), anyString(), any());
    }

    private void thenExpectCustomerServiceCreateCustomerCalledOnce() {
        verify(customerService).createCustomer(any(CreateCustomerRequest.class));
    }

    private void thenExpectNoCallToCustomerServiceCreateCustomer() {
        verify(customerService, times(0)).createCustomer(any(CreateCustomerRequest.class));
    }

    private void thenExpectCustomerServiceUpdateCustomerCalledOnce() {
        verify(customerService).updateCustomer(anyString(), any(UpdateCustomerRequest.class));
    }

    private void thenExpectNoCallToCustomerServiceUpdateCustomer() {
        verify(customerService, times(0)).updateCustomer(anyString(), any(UpdateCustomerRequest.class));
    }

    private void thenExpectCustomerServicePartiallyUpdateCustomerCalledOnce() {
        verify(customerService).partiallyUpdateCustomer(anyString(), any(PartiallyUpdateCustomerRequest.class));
    }

    private void thenExpectCustomerServiceDeleteCustomerCalledOnce() {
        verify(customerService).deleteCustomer(anyString());
    }

    @SneakyThrows
    protected String objectToJsonString(Object object) {
        return mapper.writeValueAsString(object);
    }
}
