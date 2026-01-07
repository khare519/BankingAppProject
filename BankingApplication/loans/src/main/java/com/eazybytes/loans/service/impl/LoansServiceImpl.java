package com.eazybytes.loans.service.impl;

import com.eazybytes.loans.constants.LoansConstants;
import com.eazybytes.loans.dto.LoansDto;
import com.eazybytes.loans.entity.Loans;
import com.eazybytes.loans.exceptions.LoanAlreadyExistsException;
import com.eazybytes.loans.exceptions.ResourceNotFoundException;
import com.eazybytes.loans.mapper.LoansMapper;
import com.eazybytes.loans.repository.LoansRepository;
import com.eazybytes.loans.service.ILoansService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class LoansServiceImpl implements ILoansService
{
 private LoansRepository loansRepository;
    @Override
    public void createLoan(String mobileNumber) {
        Optional<Loans> loan= loansRepository.findByMobileNumber(mobileNumber);
        if(loan.isPresent()){
            throw new LoanAlreadyExistsException("Loan already exists for the mobile number: "+mobileNumber);
        }
      Loans newLoan = createNewLoan(mobileNumber);
        loansRepository.save(newLoan);
    }
    private Loans createNewLoan(String mobileNumber) {
        Loans newLoan = new Loans();
        long randomLoanNumber = 100000000000L + new Random().nextInt(900000000);
        newLoan.setLoanNumber(Long.toString(randomLoanNumber));
        newLoan.setMobileNumber(mobileNumber);
        newLoan.setLoanType(LoansConstants.HOME_LOAN);
        newLoan.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
        return newLoan;
    }
    @Override
    public LoansDto fetchLoanDetails(String mobileNumber) {
        Loans existingLoan=loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan not found for the mobile number: "+mobileNumber)
        );
        LoansDto loansDto = LoansMapper.mapToLoansDto(existingLoan,new LoansDto());
        return loansDto;
    }

    @Override
    public boolean updateLoanDetails(LoansDto loansDto) {
        Loans existingLoan=loansRepository.findByLoanNumber(loansDto.getLoanNumber()).orElseThrow(
                ()-> new ResourceNotFoundException("Loan not found for the loan number: "+loansDto.getLoanNumber())
        );
        Loans updatedLoan=LoansMapper.mapToLoans(loansDto,existingLoan);
        loansRepository.save(updatedLoan);
        return true;
    }

    @Override
    public boolean deleteLoan(String mobileNumber) {
        Loans existingLoan=loansRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Loan not found for the mobile number: "+mobileNumber)
        );
        loansRepository.deleteById(existingLoan.getLoanId());
        return true;
    }
}
