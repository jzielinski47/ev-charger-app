package jz.pk.evcm.dto.req.local;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jz.pk.evcm.entity.VehicleInlet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private static Set<ConstraintViolation<VehicleRequest>> validate(Set<VehicleInlet> inlets) {
        return validator.validate(new VehicleRequest("Kia", "EV6", Year.of(2023), inlets, null, null, null, null, null));
    }

    @Test
    void acceptsNonEmptyInlets() {
        assertThat(validate(EnumSet.of(VehicleInlet.CCS2, VehicleInlet.TYPE_2))).isEmpty();
    }

    @Test
    void rejectsEmptyInlets() {
        assertThat(validate(Set.of()))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("supportedInlets");
    }

    @Test
    void rejectsMissingInlets() {
        assertThat(validate(null))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("supportedInlets");
    }

    @Test
    void rejectsNullInletElement() {
        Set<VehicleInlet> inlets = new HashSet<>();
        inlets.add(VehicleInlet.CCS2);
        inlets.add(null);

        assertThat(validate(inlets))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("supportedInlets[].<iterable element>");
    }
}
