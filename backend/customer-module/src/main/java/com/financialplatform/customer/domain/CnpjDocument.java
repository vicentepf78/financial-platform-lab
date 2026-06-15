package com.financialplatform.customer.domain;

import com.financialplatform.sharedkernel.domain.Cnpj;

public final class CnpjDocument implements Document {

    private final Cnpj cnpj;

    CnpjDocument(String raw) {
        this.cnpj = Cnpj.of(raw);
    }

    CnpjDocument(Cnpj cnpj) {
        this.cnpj = cnpj;
    }

    public Cnpj cnpj() {
        return cnpj;
    }

    @Override
    public String digits() {
        return cnpj.value();
    }

    @Override
    public String formatted() {
        return cnpj.formatted();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CnpjDocument that = (CnpjDocument) other;
        return cnpj.equals(that.cnpj);
    }

    @Override
    public int hashCode() {
        return cnpj.hashCode();
    }

    @Override
    public String toString() {
        return cnpj.formatted();
    }
}
