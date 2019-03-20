package net.helix.sbx.service.transactionpruning;

/**
 * Represents the different states a {@link TransactionPrunerJob} can be in.
 */
public enum TransactionPrunerJobStatus {
    PENDING,
    RUNNING,
    DONE,
    FAILED
}
