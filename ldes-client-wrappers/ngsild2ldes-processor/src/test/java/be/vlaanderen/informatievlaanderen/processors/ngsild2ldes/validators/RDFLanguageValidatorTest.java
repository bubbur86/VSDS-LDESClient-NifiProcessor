package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.validators;

import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RDFLanguageValidatorTest {
    RDFLanguageValidator rdfLanguageValidator = new RDFLanguageValidator();

    @Test
    void when_inputRepresentRDFFormat_ValidationResultIsValid(){
        ValidationResult validate = rdfLanguageValidator.validate("", "n-quads", mock(ValidationContext.class));

        assertTrue(validate.isValid());
    }

    @Test
    void when_inputIsNull_ValidationResultIsNotValid(){
        ValidationResult validate = rdfLanguageValidator.validate("", null, mock(ValidationContext.class));

        assertFalse(validate.isValid());
    }

}