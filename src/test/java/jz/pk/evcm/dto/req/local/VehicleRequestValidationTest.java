package jz.pk.evcm.dto.req.local;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jz.pk.evcm.entity.VehicleInlet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
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

    private static Set<ConstraintViolation<VehicleRequest>> validateSpecs(Double battery, Double maxAc, Double maxDc,
                                                                         Integer acPhases) {
        return validator.validate(new VehicleRequest("Kia", "EV6", Year.of(2023), EnumSet.of(VehicleInlet.CCS2),
                null, battery, maxAc, maxDc, acPhases));
    }

    private static List<String> violatedProperties(Set<ConstraintViolation<VehicleRequest>> violations) {
        return violations.stream().map(violation -> violation.getPropertyPath().toString()).toList();
    }

    @Test
    void acceptsPositiveSpecs() {
        assertThat(validateSpecs(77.4, 11.0, 235.0, 3)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void rejectsNonPositivePowerAndBattery(double value) {
        assertThat(violatedProperties(validateSpecs(value, value, value, null)))
                .containsExactlyInAnyOrder("batteryCapacityKwh", "maxAcPowerKw", "maxDcPowerKw");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3})
    void acceptsAcPhasesInRange(int acPhases) {
        assertThat(validateSpecs(null, null, null, acPhases)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void rejectsAcPhasesOutOfRange(int acPhases) {
        assertThat(violatedProperties(validateSpecs(null, null, null, acPhases))).containsExactly("acPhases");
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
