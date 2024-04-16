package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.common.Gender;
import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeGenerator;
import ee.taltech.inbankbackend.exception.*;
import ee.taltech.inbankbackend.model.Decision;
import ee.taltech.inbankbackend.utils.DecisionEngineConstant;
import ee.taltech.inbankbackend.utils.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DecisionEngineTest {
    private DecisionEngine decisionEngine;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;
    private String underLegalAgePersonalCode;
    private String riskyMalePersonalCode;
    private String riskyFemalePersonalCode;

    @BeforeEach
    void setUp() throws PersonalCodeException {
        EstonianPersonalCodeGenerator personalCodeGenerator = new EstonianPersonalCodeGenerator();
        LocalDate averageUserBirthday = LocalDate.now().minusYears(30);
        LocalDate underAgeUserBirthday = LocalDate.now().minusYears(17);
        LocalDate riskyMaleAgeBirthDay = LocalDate.now().minusYears(74);
        LocalDate riskyFemaleAgeBirthDay = LocalDate.now().minusYears(76);
        debtorPersonalCode = personalCodeGenerator.generatePersonalCode(Gender.MALE, averageUserBirthday, 29);
        segment1PersonalCode = personalCodeGenerator.generatePersonalCode(Gender.FEMALE, averageUserBirthday, 300);
        segment2PersonalCode = personalCodeGenerator.generatePersonalCode(Gender.MALE, averageUserBirthday, 600);
        segment3PersonalCode = personalCodeGenerator.generatePersonalCode(Gender.FEMALE, averageUserBirthday, 900);
        underLegalAgePersonalCode = personalCodeGenerator.generatePersonalCode(Gender.MALE, underAgeUserBirthday, 300);
        riskyMalePersonalCode = personalCodeGenerator.generatePersonalCode(Gender.MALE, riskyMaleAgeBirthDay, 300);
        riskyFemalePersonalCode = personalCodeGenerator.generatePersonalCode(Gender.FEMALE, riskyFemaleAgeBirthDay, 300);
        decisionEngine = new DecisionEngine(new Validator(), new CountryService());
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 4000L, 12));
    }

    @Test
    void testSegment1PersonalCode() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12);
        assertEquals(2000, decision.loanAmount());
        assertEquals(20, decision.loanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12);
        assertEquals(3600, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, 12);
        assertEquals(10000, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateApprovedLoan(invalidPersonalCode, 4000L, 12));
    }

    @Test
    void testInvalidLoanAmount() {
        Long tooLowLoanAmount = DecisionEngineConstant.MINIMUM_LOAN_AMOUNT - 1L;
        Long tooHighLoanAmount = DecisionEngineConstant.MAXIMUM_LOAN_AMOUNT + 1L;

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount, 12));

        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount, 12));
    }

    @Test
    void testInvalidLoanPeriod() {
        int tooShortLoanPeriod = DecisionEngineConstant.MINIMUM_LOAN_PERIOD - 1;
        int tooLongLoanPeriod = DecisionEngineConstant.MAXIMUM_LOAN_PERIOD + 1;

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooShortLoanPeriod));

        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, tooLongLoanPeriod));
    }

    @Test
    void testFindMaximumLoanAmount() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 2000L, 12);
        assertEquals(3600, decision.loanAmount());
        assertEquals(12, decision.loanPeriod());
    }

    @Test
    void testFindSuitableLoanPeriod() throws Exception {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 2000L, 15);
        assertEquals(2000, decision.loanAmount());
        assertEquals(20, decision.loanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 10000L, 60));
    }

    @Test
    void testNotLegalAgeLoanRequest() {
        assertThrows(InvalidAgeException.class,
                () -> decisionEngine.calculateApprovedLoan(underLegalAgePersonalCode, 2000L, 20));
    }

    @Test
    void testRiskyMaleAgeLoanRequest() {
        assertThrows(InvalidAgeException.class,
                () -> decisionEngine.calculateApprovedLoan(riskyMalePersonalCode, 2000L, 20));
    }

    @Test
    void testRiskyFemaleAgeLoanRequest() {
        assertThrows(InvalidAgeException.class,
                () -> decisionEngine.calculateApprovedLoan(riskyFemalePersonalCode, 2000L, 20));
    }


}

