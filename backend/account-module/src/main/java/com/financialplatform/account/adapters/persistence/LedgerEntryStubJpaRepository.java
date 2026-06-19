package com.financialplatform.account.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

interface LedgerEntryStubJpaRepository extends JpaRepository<LedgerEntryStubEntity, UUID> {

    @Query("""
            select coalesce(sum(
                case
                    when entry.entryType = 'CREDIT' then entry.amount
                    else -entry.amount
                end
            ), 0)
            from LedgerEntryStubEntity entry
            where entry.accountId = :accountId
            """)
    BigDecimal projectBalance(@Param("accountId") UUID accountId);
}
