package com.ankhsquirrel.kplpay.integration.insee;

/**
 * Company data resolved from the INSEE Sirene registry, in the shape the rest of the application
 * cares about. INSEE's own wire format never leaks past {@link InseeClient}.
 */
public record CompanyInfo(String legalName, String address, String postalCode, String city) {
}
