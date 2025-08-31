package com.example.asset_management.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(String message) {
        super(message);
    }

    public LoanNotFoundException(Long loanId) {
        super("Loan not found with ID: " + loanId);
    }

    public LoanNotFoundException(Long assetId, String type) {
        super("No active loan found for asset ID: " + assetId);
    }
}
