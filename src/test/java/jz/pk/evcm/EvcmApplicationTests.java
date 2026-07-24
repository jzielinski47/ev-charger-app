package jz.pk.evcm;

import jz.pk.evcm.controller.AuthController;
import jz.pk.evcm.controller.ChargePointController;
import jz.pk.evcm.controller.OpenChargeController;
import jz.pk.evcm.controller.VehicleController;
import jz.pk.evcm.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EvcmApplicationTests {

    @Autowired
    private AuthController authController;
    @Autowired
    private ChargePointController chargePointController;
    @Autowired
    private OpenChargeController openChargeController;
    @Autowired
    private VehicleController vehicleController;

    @Test
    void contextLoads() throws Exception {
        Assertions.assertThat(authController).isNotNull();
        Assertions.assertThat(chargePointController).isNotNull();
        Assertions.assertThat(openChargeController).isNotNull();
        Assertions.assertThat(vehicleController).isNotNull();
    }

}
