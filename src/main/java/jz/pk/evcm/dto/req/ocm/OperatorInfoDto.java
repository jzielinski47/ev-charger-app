package jz.pk.evcm.dto.req.ocm;

public record OperatorInfoDto(
        Long id,
        String title,
        String websiteURL,
        String phonePrimaryContact,
        String contactEmail,
        String faultReportEmail
) {
}
