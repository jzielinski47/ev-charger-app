package jz.pk.evcm.dto.res;

import jz.pk.evcm.dto.req.ocm.ChargerPointRequest;

import java.util.List;

public record ChargerImportSummary(int imported, int created, int updated, List<ChargerPointRequest> pois) {

    public static final ChargerImportSummary EMPTY = new ChargerImportSummary(0, 0, 0, List.of());
}
