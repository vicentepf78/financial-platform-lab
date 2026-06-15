package com.financialplatform.customer.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentTest {

    private static final String VALID_CPF = "529.982.247-25";
    private static final String VALID_CNPJ = "11.222.333/0001-81";

    @Test
    void shouldCreateCpfDocumentWhenTypeIsIndividual() {
        Document document = Document.of(CustomerType.INDIVIDUAL, VALID_CPF);

        assertThat(document).isInstanceOf(CpfDocument.class);
        assertThat(document.digits()).isEqualTo("52998224725");
        assertThat(document.formatted()).isEqualTo("529.982.247-25");
    }

    @Test
    void shouldCreateCnpjDocumentWhenTypeIsCompany() {
        Document document = Document.of(CustomerType.COMPANY, VALID_CNPJ);

        assertThat(document).isInstanceOf(CnpjDocument.class);
        assertThat(document.digits()).isEqualTo("11222333000181");
    }

    @Test
    void shouldRejectDocumentWhenCpfChecksumIsInvalid() {
        assertThatThrownBy(() -> Document.of(CustomerType.INDIVIDUAL, "111.111.111-11"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid CPF");
    }

    @Test
    void shouldRejectDocumentWhenIndividualTypeHasCnpjLength() {
        assertThatThrownBy(() -> Document.of(CustomerType.INDIVIDUAL, VALID_CNPJ))
                .isInstanceOf(CustomerTypeMismatchException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    void shouldRejectDocumentWhenCompanyTypeHasCpfLength() {
        assertThatThrownBy(() -> Document.of(CustomerType.COMPANY, VALID_CPF))
                .isInstanceOf(CustomerTypeMismatchException.class)
                .hasMessageContaining("CNPJ");
    }
}
