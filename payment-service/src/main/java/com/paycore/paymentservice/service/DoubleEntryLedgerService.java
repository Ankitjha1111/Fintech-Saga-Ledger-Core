package com.paycore.paymentservice.service;

import com.paycore.paymentservice.model.Account;
import com.paycore.paymentservice.model.LedgerEntry;
import com.paycore.paymentservice.repository.AccountRepository;
import com.paycore.paymentservice.repository.LedgerEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DoubleEntryLedgerService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public void recordTransaction(String transactionId, String senderAccountNo, String receiverAccountNo, BigDecimal amount) {
        // 1. Fetch sender and receiver accounts
        Account sender = accountRepository.findByAccountNumber(senderAccountNo)
                .orElseThrow(() -> new RuntimeException("Sender account not found: " + senderAccountNo));

        Account receiver = accountRepository.findByAccountNumber(receiverAccountNo)
                .orElseThrow(() -> new RuntimeException("Receiver account not found: " + receiverAccountNo));

        // 2. Validate sender balance
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in account: " + senderAccountNo);
        }

        // 3. Update balances
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 4. Create immutable ledger entries (Double-Entry Bookkeeping)
        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setTransactionId(transactionId);
        debitEntry.setAccountNumber(senderAccountNo);
        debitEntry.setAmount(amount);
        debitEntry.setEntryType(LedgerEntry.EntryType.DEBIT);

        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setTransactionId(transactionId);
        creditEntry.setAccountNumber(receiverAccountNo);
        creditEntry.setAmount(amount);
        creditEntry.setEntryType(LedgerEntry.EntryType.CREDIT);

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);
    }
}