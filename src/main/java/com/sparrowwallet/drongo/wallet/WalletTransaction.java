package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.protocol.Transaction;
import com.sparrowwallet.drongo.psbt.PSBT;

import java.util.Map;

/**
 * WalletTransaction contains a draft transaction along with associated metadata. The draft transaction has empty signatures but is otherwise complete.
 * This object represents an intermediate step before the transaction is signed or a PSBT is created from it.
 */
public class WalletTransaction {
    private final Wallet wallet;
    private final Transaction transaction;
    private final Map<BlockTransactionHashIndex, WalletNode> selectedUtxos;
    private final Address recipientAddress;
    private final long recipientAmount;
    private final WalletNode changeNode;
    private final long changeAmount;
    private final long fee;

    public WalletTransaction(Wallet wallet, Transaction transaction, Map<BlockTransactionHashIndex, WalletNode> selectedUtxos, Address recipientAddress, long recipientAmount, long fee) {
        this(wallet, transaction, selectedUtxos, recipientAddress, recipientAmount, null, 0L, fee);
    }

    public WalletTransaction(Wallet wallet, Transaction transaction, Map<BlockTransactionHashIndex, WalletNode> selectedUtxos, Address recipientAddress, long recipientAmount, WalletNode changeNode, long changeAmount, long fee) {
        this.wallet = wallet;
        this.transaction = transaction;
        this.selectedUtxos = selectedUtxos;
        this.recipientAddress = recipientAddress;
        this.recipientAmount = recipientAmount;
        this.changeNode = changeNode;
        this.changeAmount = changeAmount;
        this.fee = fee;
    }

    public PSBT createPSBT() {
        //TODO: Create PSBT
        return null;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Map<BlockTransactionHashIndex, WalletNode> getSelectedUtxos() {
        return selectedUtxos;
    }

    public Address getRecipientAddress() {
        return recipientAddress;
    }

    public long getRecipientAmount() {
        return recipientAmount;
    }

    public WalletNode getChangeNode() {
        return changeNode;
    }

    public long getChangeAmount() {
        return changeAmount;
    }

    public long getFee() {
        return fee;
    }
}