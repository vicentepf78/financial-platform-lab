package com.financialplatform.customer.domain;

import com.financialplatform.sharedkernel.domain.Cpf;

public final class CpfDocument implements Document {

    private final Cpf cpf;

    CpfDocument(String raw) {
        this.cpf = Cpf.of(raw);
    }

    CpfDocument(Cpf cpf) {
        this.cpf = cpf;
    }

    public Cpf cpf() {
        return cpf;
    }

    @Override
    public String digits() {
        return cpf.value();
    }

    @Override
    public String formatted() {
        return cpf.formatted();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CpfDocument that = (CpfDocument) other;
        return cpf.equals(that.cpf);
    }

    @Override
    public int hashCode() {
        return cpf.hashCode();
    }

    @Override
    public String toString() {
        return cpf.formatted();
    }
}
