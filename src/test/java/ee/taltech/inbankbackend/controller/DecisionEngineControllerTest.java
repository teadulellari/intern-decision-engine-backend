package ee.taltech.inbankbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vladislavgoltjajev.personalcode.common.Gender;
import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeGenerator;
import ee.taltech.inbankbackend.dto.DecisionRequest;
import ee.taltech.inbankbackend.dto.DecisionResponse;
import ee.taltech.inbankbackend.service.DecisionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class holds integration tests for the DecisionEngineController endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class DecisionEngineControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DecisionEngine decisionEngine;
    private ObjectMapper objectMapper;
    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String underLegalAgePersonalCode;
    private String riskyMalePersonalCode;
    private String riskyFemalePersonalCode;


    @BeforeEach
    public void setup() throws PersonalCodeException {
        objectMapper = new ObjectMapper();
        EstonianPersonalCodeGenerator personalCodeGenerator = new EstonianPersonalCodeGenerator();
        LocalDate averageUserBirthday = LocalDate.now().minusYears(30);
        LocalDate underAgeUserBirthday = LocalDate.now().minusYears(17);
        LocalDate riskyMaleAgeBirthDay = LocalDate.now().minusYears(74);
        LocalDate riskyFemaleAgeBirthDay = LocalDate.now().minusYears(76);
        debtorPersonalCode = personalCodeGenerator.generatePersonalCode(Gender.MALE, averageUserBirthday, 29);
        segment1PersonalCode = personalCodeGenerator.generatePersonalCode(Gender.FEMALE, averageUserBirthday, 300);
        underLegalAgePersonalCode = personalCodeGenerator.generatePersonalCode(Gender.MALE, underAgeUserBirthday, 300);
        riskyMalePersonalCode = personalCodeGenerator.generatePersonalCode(Gender.MALE, riskyMaleAgeBirthDay, 300);
        riskyFemalePersonalCode = personalCodeGenerator.generatePersonalCode(Gender.FEMALE, riskyFemaleAgeBirthDay, 300);
    }

    /**
     * This method tests the /loan/decision endpoint with valid inputs.
     */
    @Test
    void givenValidRequest_whenRequestDecision_thenReturnsExpectedResponse() throws Exception {

        DecisionRequest request = new DecisionRequest(segment1PersonalCode, 2000L, 20);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(2000))
                .andExpect(jsonPath("$.loanPeriod").value(20))
                .andExpect(jsonPath("$.errorMessage").isEmpty())
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertEquals(2000, response.getLoanAmount());
        assertEquals(20, response.getLoanPeriod());
        assertNull(response.getErrorMessage());
    }

    /**
     * This test ensures that if an invalid personal code is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid personal ID code!"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertNull(response.getLoanAmount());
        assertNull(response.getLoanPeriod());
        assertEquals("Invalid personal ID code!", response.getErrorMessage());
    }

    /**
     * This test ensures that if an invalid loan amount is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        DecisionRequest request = new DecisionRequest(segment1PersonalCode, 10L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan amount!"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertNull(response.getLoanAmount());
        assertNull(response.getLoanPeriod());
        assertEquals("Invalid loan amount!", response.getErrorMessage());
    }

    /**
     * This test ensures that if an invalid loan period is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        DecisionRequest request = new DecisionRequest(segment1PersonalCode, 2000L, 10);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan period!"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertNull(response.getLoanAmount());
        assertNull(response.getLoanPeriod());
        assertEquals("Invalid loan period!", response.getErrorMessage());
    }

    /**
     * This test ensures that if no valid loan is found, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    void givenNoValidLoan_whenRequestDecision_thenReturnsNotFound() throws Exception {
        DecisionRequest request = new DecisionRequest(debtorPersonalCode, 2000L, 12);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value("No valid loan found!"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertNull(response.getLoanAmount());
        assertNull(response.getLoanPeriod());
        assertEquals("No valid loan found!", response.getErrorMessage());
    }

    @Test
    void givenValidRequest_whenLoanPeriodNotSuitable_thenReturnsSuitableLoanPeriodIfExists() throws Exception {
        DecisionRequest request = new DecisionRequest(segment1PersonalCode, 2000L, 15);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertEquals(2000, response.getLoanAmount());
        assertEquals(20, response.getLoanPeriod());
        assertNull(response.getErrorMessage());
    }

    @Test
    void givenValidRequest_whenLoanAmountNotSuitable_thenReturnsSuitableLoanAmountIfExists() throws Exception {
        DecisionRequest request = new DecisionRequest(segment1PersonalCode, 10000L, 40);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertEquals(4000, response.getLoanAmount());
        assertEquals(40, response.getLoanPeriod());
        assertNull(response.getErrorMessage());
    }

    @Test
    void givenValidRequest_whenCustomerIsLegallyNotEligible_thenReturnsNotFound() throws Exception {
        DecisionRequest request = new DecisionRequest(underLegalAgePersonalCode, 2000L, 20);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertNull(response.getLoanAmount());
        assertNull(response.getLoanPeriod());
        assertEquals("Legally not eligible to get a loan!", response.getErrorMessage());
    }

    @Test
    void givenValidRequest_whenCustomerIsInRiskyMaleAgeGroup_thenReturnsNotFound() throws Exception {
        DecisionRequest request = new DecisionRequest(riskyMalePersonalCode, 2000L, 12);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertNull(response.getLoanAmount());
        assertNull(response.getLoanPeriod());
        assertEquals("Customer Age is in high risk group!", response.getErrorMessage());
    }

    @Test
    void givenValidRequest_whenCustomerIsInRiskyFemaleAgeGroup_thenReturnsNotFound() throws Exception {
        DecisionRequest request = new DecisionRequest(riskyFemalePersonalCode, 2000L, 12);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assertNull(response.getLoanAmount());
        assertNull(response.getLoanPeriod());
        assertEquals("Customer Age is in high risk group!", response.getErrorMessage());
    }
}
