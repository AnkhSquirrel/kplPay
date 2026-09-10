package com.ankhsquirrel.kplpay.integration.insee;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Looks a company up in the INSEE Sirene registry by SIRET and maps the response to {@link CompanyInfo}.
 */
@Component
public class InseeClient {

    private final RestClient restClient;

    public InseeClient(@Qualifier("inseeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * @throws SiretNotFoundException    if the registry has no établissement for this SIRET
     * @throws InseeUnavailableException if the API cannot be reached or fails unexpectedly
     */
    public CompanyInfo lookup(String siret) {
        SiretResponse response = fetch(siret);
        if (response == null || response.etablissement() == null) {
            throw new SiretNotFoundException(siret);
        }
        return toCompanyInfo(response.etablissement());
    }

    private SiretResponse fetch(String siret) {
        try {
            return restClient.get()
                    .uri("/siret/{siret}", siret)
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            (req, res) -> { throw new SiretNotFoundException(siret); })
                    .body(SiretResponse.class);
        } catch (SiretNotFoundException e) {
            throw e;
        } catch (RestClientException e) {
            throw new InseeUnavailableException(e);
        }
    }

    private static CompanyInfo toCompanyInfo(SiretResponse.Etablissement etablissement) {
        String legalName = legalName(etablissement.uniteLegale());
        SiretResponse.AdresseEtablissement adresse = etablissement.adresseEtablissement();
        if (adresse == null) {
            return new CompanyInfo(legalName, null, null, null);
        }
        return new CompanyInfo(
                legalName,
                joinNonBlank(adresse.numeroVoieEtablissement(),
                        adresse.typeVoieEtablissement(),
                        adresse.libelleVoieEtablissement()),
                StringUtils.trimToNull(adresse.codePostalEtablissement()),
                StringUtils.trimToNull(adresse.libelleCommuneEtablissement()));
    }

    private static String legalName(SiretResponse.UniteLegale uniteLegale) {
        if (uniteLegale == null) {
            return null;
        }
        String denomination = StringUtils.trimToNull(uniteLegale.denominationUniteLegale());
        if (denomination != null) {
            return denomination;
        }
        // Sole trader: the unité légale carries no company name, only the owner's identity.
        return joinNonBlank(uniteLegale.prenom1UniteLegale(), uniteLegale.nomUniteLegale());
    }

    /** Trimmed parts that carry text, glued with single spaces; {@code null} if none do. */
    private static String joinNonBlank(String... parts) {
        return Stream.of(parts)
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + " " + b)
                .orElse(null);
    }
}
