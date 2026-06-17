package com.financialplatform.account.features.transfermoney;

import com.financialplatform.account.domain.Account;
import com.financialplatform.account.domain.AccountNotFoundException;
import com.financialplatform.account.domain.AccountStatus;
import com.financialplatform.account.domain.InactiveAccountException;
import com.financialplatform.account.domain.InsufficientBalanceException;
import com.financialplatform.account.domain.InvalidAmountException;
import com.financialplatform.account.domain.SameAccountTransferException;
import com.financialplatform.account.domain.Transfer;
import com.financialplatform.account.domain.TransferDomainService;
import com.financialplatform.account.domain.TransferExecuted;
import com.financialplatform.account.domain.TransferStatus;
import com.financialplatform.account.ports.AccountRepositoryPort;
import com.financialplatform.account.ports.EventPublisherPort;
import com.financialplatform.account.ports.LedgerPort;
import com.financialplatform.account.ports.TransferRepositoryPort;
import com.financialplatform.sharedkernel.domain.DomainEvent;
import com.financialplatform.sharedkernel.domain.Identifier;
import com.financialplatform.sharedkernel.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferMoneyUseCaseTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ORIGIN_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID DESTINATION_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
    private static final UUID CORRELATION_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");
    private static final String IDEMPOTENCY_KEY = "client-req-001";
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");
    private static final Instant FIXED_TIME = Instant.parse("2026-06-15T14:00:00Z");

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private TransferRepositoryPort transferRepository;

    @Mock
    private LedgerPort ledgerPort;

    @Mock
    private EventPublisherPort eventPublisher;

    private TransferMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
        useCase = new TransferMoneyUseCase(
                accountRepository,
                transferRepository,
                ledgerPort,
                eventPublisher,
                new TransferDomainService(),
                clock);
    }

    @Test
    void shouldTransferMoneyWhenAccountsAreActiveAndBalanceIsSufficient() {
        Account origin = activeAccount(ORIGIN_ID);
        Account destination = activeAccount(DESTINATION_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));
        when(ledgerPort.getBalanceProjection(Identifier.of(ORIGIN_ID))).thenReturn(Money.brl("500.00"));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferMoneyResult result = useCase.execute(validCommand(null, null));

        assertThat(result.originAccountId()).isEqualTo(ORIGIN_ID);
        assertThat(result.destinationAccountId()).isEqualTo(DESTINATION_ID);
        assertThat(result.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(result.currency()).isEqualTo("BRL");
        assertThat(result.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(result.correlationId()).isEqualTo(CORRELATION_ID.toString());
        assertThat(result.createdAt()).isEqualTo(FIXED_TIME);
        assertThat(result.transferId()).isNotNull();

        verify(ledgerPort)
                .recordTransfer(
                        eq(Identifier.of(result.transferId())),
                        eq(Identifier.of(ORIGIN_ID)),
                        eq(Identifier.of(DESTINATION_ID)),
                        eq(Money.brl(AMOUNT)),
                        eq(CORRELATION_ID.toString()));

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        assertThat(transferCaptor.getValue().createdBy()).isEqualTo("system");

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(TransferExecuted.class);
    }

    @Test
    void shouldUseProvidedActorWhenActorIsPresent() {
        Account origin = activeAccount(ORIGIN_ID);
        Account destination = activeAccount(DESTINATION_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));
        when(ledgerPort.getBalanceProjection(Identifier.of(ORIGIN_ID))).thenReturn(Money.brl("500.00"));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new TransferMoneyCommand(
                ORIGIN_ID, DESTINATION_ID, AMOUNT, CORRELATION_ID, null, "operator-1"));

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        assertThat(transferCaptor.getValue().createdBy()).isEqualTo("operator-1");
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenOriginBalanceIsTooLow() {
        Account origin = activeAccount(ORIGIN_ID);
        Account destination = activeAccount(DESTINATION_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));
        when(ledgerPort.getBalanceProjection(Identifier.of(ORIGIN_ID))).thenReturn(Money.brl("50.00"));

        assertThatThrownBy(() -> useCase.execute(validCommand(null, null)))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining(ORIGIN_ID.toString());

        verify(ledgerPort, never()).recordTransfer(any(), any(), any(), any(), any());
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowInactiveAccountExceptionWhenOriginIsClosed() {
        Account origin = account(ORIGIN_ID, AccountStatus.CLOSED);
        Account destination = activeAccount(DESTINATION_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> useCase.execute(validCommand(null, null)))
                .isInstanceOf(InactiveAccountException.class)
                .hasMessageContaining(ORIGIN_ID.toString())
                .hasMessageContaining("CLOSED");

        verifyNoInteractions(ledgerPort);
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowInactiveAccountExceptionWhenDestinationIsClosed() {
        Account origin = activeAccount(ORIGIN_ID);
        Account destination = account(DESTINATION_ID, AccountStatus.CLOSED);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> useCase.execute(validCommand(null, null)))
                .isInstanceOf(InactiveAccountException.class)
                .hasMessageContaining(DESTINATION_ID.toString())
                .hasMessageContaining("CLOSED");

        verifyNoInteractions(ledgerPort);
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenOriginDoesNotExist() {
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(validCommand(null, null)))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(ORIGIN_ID.toString());

        verify(accountRepository, never()).findById(Identifier.of(DESTINATION_ID));
        verifyNoInteractions(ledgerPort);
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenDestinationDoesNotExist() {
        Account origin = activeAccount(ORIGIN_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(validCommand(null, null)))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(DESTINATION_ID.toString());

        verifyNoInteractions(ledgerPort);
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowSameAccountTransferExceptionWhenOriginEqualsDestination() {
        Account account = activeAccount(ORIGIN_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(account));

        TransferMoneyCommand command = new TransferMoneyCommand(
                ORIGIN_ID, ORIGIN_ID, AMOUNT, CORRELATION_ID, null, null);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SameAccountTransferException.class)
                .hasMessageContaining(ORIGIN_ID.toString());

        verifyNoInteractions(ledgerPort);
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldReturnExistingTransferWhenIdempotencyKeyAlreadyExists() {
        Transfer existing = Transfer.reconstitute(
                Identifier.of("880e8400-e29b-41d4-a716-446655440003"),
                Identifier.of(ORIGIN_ID),
                Identifier.of(DESTINATION_ID),
                Money.brl(AMOUNT),
                TransferStatus.COMPLETED,
                CORRELATION_ID.toString(),
                IDEMPOTENCY_KEY,
                "system",
                FIXED_TIME);
        when(transferRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.of(existing));

        TransferMoneyResult result = useCase.execute(validCommand(IDEMPOTENCY_KEY, null));

        assertThat(result.transferId()).isEqualTo(existing.id().value());
        assertThat(result.originAccountId()).isEqualTo(ORIGIN_ID);
        assertThat(result.destinationAccountId()).isEqualTo(DESTINATION_ID);

        verify(accountRepository, never()).findById(any());
        verifyNoInteractions(ledgerPort);
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldPersistIdempotencyKeyWhenProvided() {
        Account origin = activeAccount(ORIGIN_ID);
        Account destination = activeAccount(DESTINATION_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));
        when(ledgerPort.getBalanceProjection(Identifier.of(ORIGIN_ID))).thenReturn(Money.brl("500.00"));
        when(transferRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(validCommand(IDEMPOTENCY_KEY, null));

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        assertThat(transferCaptor.getValue().idempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
    }

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsZero() {
        Account origin = activeAccount(ORIGIN_ID);
        Account destination = activeAccount(DESTINATION_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));
        when(ledgerPort.getBalanceProjection(Identifier.of(ORIGIN_ID))).thenReturn(Money.brl("500.00"));

        TransferMoneyCommand command =
                new TransferMoneyCommand(ORIGIN_ID, DESTINATION_ID, BigDecimal.ZERO, CORRELATION_ID, null, null);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidAmountException.class);

        verify(ledgerPort, never()).recordTransfer(any(), any(), any(), any(), any());
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCommandIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Command is required");

        verifyNoInteractions(accountRepository, transferRepository, ledgerPort, eventPublisher);
    }

    @Test
    void shouldPublishTransferExecutedWithExpectedFields() {
        Account origin = activeAccount(ORIGIN_ID);
        Account destination = activeAccount(DESTINATION_ID);
        when(accountRepository.findById(Identifier.of(ORIGIN_ID))).thenReturn(Optional.of(origin));
        when(accountRepository.findById(Identifier.of(DESTINATION_ID))).thenReturn(Optional.of(destination));
        when(ledgerPort.getBalanceProjection(Identifier.of(ORIGIN_ID))).thenReturn(Money.brl("500.00"));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferMoneyResult result = useCase.execute(validCommand(null, null));

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        TransferExecuted event = (TransferExecuted) eventCaptor.getValue();
        assertThat(event.aggregateId().value()).isEqualTo(result.transferId());
        assertThat(event.originAccountId().value()).isEqualTo(ORIGIN_ID);
        assertThat(event.destinationAccountId().value()).isEqualTo(DESTINATION_ID);
        assertThat(event.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(event.currency()).isEqualTo("BRL");
        assertThat(event.correlationId()).isEqualTo(CORRELATION_ID.toString());
        assertThat(event.occurredAt()).isEqualTo(FIXED_TIME);
        assertThat(event.eventType()).isEqualTo("TransferExecuted");
    }

    private static TransferMoneyCommand validCommand(String idempotencyKey, String actor) {
        return new TransferMoneyCommand(ORIGIN_ID, DESTINATION_ID, AMOUNT, CORRELATION_ID, idempotencyKey, actor);
    }

    private static Account activeAccount(UUID accountId) {
        return account(accountId, AccountStatus.ACTIVE);
    }

    private static Account account(UUID accountId, AccountStatus status) {
        return Account.reconstitute(
                Identifier.of(accountId), Identifier.of(CUSTOMER_ID), status, FIXED_TIME, "system");
    }
}
