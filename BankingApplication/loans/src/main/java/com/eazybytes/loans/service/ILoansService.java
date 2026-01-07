package com.eazybytes.loans.service;

import com.eazybytes.loans.dto.LoansDto;

public interface ILoansService
{
    void createLoan(String mobileNumber);
    LoansDto fetchLoanDetails(String mobileNumber);
    boolean updateLoanDetails(LoansDto loansDto);
    boolean deleteLoan(String mobileNumber);
}
