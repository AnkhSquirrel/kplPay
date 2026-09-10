package com.ankhsquirrel.kplpay.integration.insee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Subset of the INSEE Sirene {@code GET /siret/{siret}} response that we actually read.
 * Package-private on purpose — only {@link InseeClient} touches this type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record SiretResponse(Etablissement etablissement) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Etablissement(UniteLegale uniteLegale, AdresseEtablissement adresseEtablissement) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UniteLegale(String denominationUniteLegale,
                       String nomUniteLegale,
                       String prenom1UniteLegale) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AdresseEtablissement(String numeroVoieEtablissement,
                                String typeVoieEtablissement,
                                String libelleVoieEtablissement,
                                String codePostalEtablissement,
                                String libelleCommuneEtablissement) {
    }
}
