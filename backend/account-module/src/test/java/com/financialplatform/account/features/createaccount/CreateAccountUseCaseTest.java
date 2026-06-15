package com.financialplatform.account.features.createaccount;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.AccountCreated;
import com.financialplatform.account.domain.AccountStatus;
import com.financialplatform.account.domain.CustomerNotFoundException;
import com.financialplatform.account.domain.LedgerInitializationException;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.account.ports.CustomerLookupPort;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAccountUseCaseTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant FIXED_TIME = Instant.parse("2026-06-15T10:00:00Z");

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private LedgerPort ledgerPort;

    @Mock
    private CustomerLookupPort customerLookup;

    @Mock
    private EventPublisherPort eventPublisher;

    private CreateAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
        useCase = new CreateAccountUseCase(
                accountRepository, ledgerPort, customerLookup, eventPublisher, clock);
    }

    @Test
    void shouldCreateAccountWhenCustomerExists() {
        when(customerLookup.exists(Identifier.of(CUSTOMER_ID))).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateAccountResult result = useCase.execute(new CreateAccountCommand(CUSTOMER_ID, null));

        assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(result.createdAt()).isEqualTo(FIXED_TIME);
        assertThat(result.id()).isNotNull();

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().createdBy()).isEqualTo("system");

        verify(ledgerPort).initializeAccount(any(Identifier.class));

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(AccountCreated.class);
    }

    @Test
    void shouldUseProvidedActorWhenActorIsPresent() {
        when(customerLookup.exists(Identifier.of(CUSTOMER_ID))).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new CreateAccountCommand(CUSTOMER_ID, "operator-1"));

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().createdBy()).isEqualTo("operator-1");
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        when(customerLookup.exists(Identifier.of(CUSTOMER_ID))).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new CreateAccountCommand(CUSTOMER_ID, null)))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(CUSTOMER_ID.toString());

        verify(ledgerPort, never()).initializeAccount(any());
        verify(accountRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldNotPersistOrPublishWhenLedgerInitializationFails() {
        when(customerLookup.exists(Identifier.of(CUSTOMER_ID))).thenReturn(true);
        doThrow(new IllegalStateException("ledger unavailable"))
                .when(ledgerPort)
                .initializeAccount(any(Identifier.class));

        assertThatThrownBy(() -> useCase.execute(new CreateAccountCommand(CUSTOMER_ID, null)))
                .isInstanceOf(LedgerInitializationException.class)
                .hasCauseInstanceOf(IllegalStateException.class);

        verify(accountRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
