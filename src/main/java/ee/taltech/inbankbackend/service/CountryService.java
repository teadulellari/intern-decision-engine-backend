package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import org.springframework.stereotype.Service;

import java.time.Period;

@Service
public class CountryService {
    public static final int MAXIMUM_LIFETIME_FEMALE = 80;
    public static final int MAXIMUM_LIFETIME_MALE = 78;
    private final EstonianPersonalCodeParser estonianPersonalCodeParser;

    public CountryService() {
        estonianPersonalCodeParser = new EstonianPersonalCodeParser();
    }

    public boolean isEligibleAgeForLoan(String personalCode) throws PersonalCodeException {
        return estonianPersonalCodeParser.getAge(personalCode).getYears() >= 18;
    }

    public Period getPersonAgeFromIdentifier(String personalCode) throws PersonalCodeException {
        return estonianPersonalCodeParser.getAge(personalCode);
    }

    public int getMaximumLifeExpectancy(String personalCode) throws PersonalCodeException {
        switch (estonianPersonalCodeParser.getGender(personalCode)) {
            case MALE -> {
                return MAXIMUM_LIFETIME_MALE;
            }
            case FEMALE -> {
                return MAXIMUM_LIFETIME_FEMALE;
            }
            default -> throw new PersonalCodeException("Unknown Gender Definition");
        }
    }
}
