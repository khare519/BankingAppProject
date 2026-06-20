package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.dto.CardsDto;
import com.eazybytes.accounts.dto.CustomerDetailsDto;
import com.eazybytes.accounts.dto.LoansDto;
import com.eazybytes.accounts.entities.Accounts;
import com.eazybytes.accounts.entities.Customer;
import com.eazybytes.accounts.exceptions.ResourceNotFoundException;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.client.CardsFeignClient;
import com.eazybytes.accounts.service.client.LoansFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest
{
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private LoansFeignClient loansFeignClient;

    @Mock
    private CardsFeignClient cardsFeignClient;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private Accounts account;
    private LoansDto loansDto;
    private CardsDto cardsDto;
    private CustomerDetailsDto customerDetailsDto;

    @BeforeEach
    void setup(){
        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("Satyam");
        customer.setEmail("satyam@gmail.com");
        customer.setMobileNumber("9876543210");

        account = new Accounts();
        account.setCustomerId(1L);
        account.setAccountNumber(1234567890L);
        account.setAccountType("Savings");
        account.setBranchAddress("Delhi");

        loansDto = new LoansDto();
        loansDto.setLoanNumber("123456789012");
        loansDto.setLoanType("Home Loan");

        cardsDto = new CardsDto();
        cardsDto.setCardNumber("123456789012");
        cardsDto.setCardType("Credit Card");
    }
    @Test
    void shouldFetchCustomerDetailsSuccessfully(){
        // Arrange
        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(account));

        when(loansFeignClient.fetchLoan(
                "corr-123",
                "9876543210"
        )).thenReturn(ResponseEntity.ok(loansDto));

        when(cardsFeignClient.fetchCardsDetails(
                "corr-123",
                "9876543210"
        )).thenReturn(ResponseEntity.ok(cardsDto));

        //Act
        CustomerDetailsDto result = customerService.fetchCustomerDetails("9876543210","corr-123");

        //Assertions

        assertNotNull(result);

        assertEquals(
                "Satyam",
                result.getName()
        );

        assertEquals(
                "9876543210",
                result.getMobileNumber()
        );

        assertEquals(
                1234567890L,
                result.getAccountsDto().getAccountNumber()
        );

        assertEquals(
                "Home Loan",
                result.getLoansDto().getLoanType()
        );

        assertEquals(
                "Credit Card",
                result.getCardsDto().getCardType()
        );

        verify(customerRepository)
                .findByMobileNumber("9876543210");

        verify(accountsRepository)
                .findByCustomerId(1L);

        verify(loansFeignClient)
                .fetchLoan(
                        "corr-123",
                        "9876543210"
                );

        verify(cardsFeignClient)
                .fetchCardsDetails(
                        "corr-123",
                        "9876543210"
                );
    }
    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        // Act + Assert

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> customerService.fetchCustomerDetails(
                                "9876543210",
                                "corr-123")
                );

        assertEquals(
                "Customer not found with the given input data MobileNumber : '9876543210'",
                exception.getMessage()
        );

        // Verify downstream interactions never happen

        verify(accountsRepository, never())
                .findByCustomerId(anyLong());

        verifyNoInteractions(loansFeignClient);

        verifyNoInteractions(cardsFeignClient);
    }
    @Test
    void shouldThrowExceptionWhenAccountNotFound() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.empty());

        // Act + Assert

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> customerService.fetchCustomerDetails(
                                "9876543210",
                                "corr-123")
                );

        assertEquals(
                "Account not found with the given input data Customer : '1'",
                exception.getMessage()
        );

        // Verify interactions

        verify(customerRepository)
                .findByMobileNumber("9876543210");

        verify(accountsRepository)
                .findByCustomerId(1L);

        // Downstream services should never execute

        verifyNoInteractions(loansFeignClient);

        verifyNoInteractions(cardsFeignClient);
    }
    @Test
    void shouldReturnCustomerDetailsWhenLoansResponseIsNull() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(account));

        when(loansFeignClient.fetchLoan(
                "corr-123",
                "9876543210"))
                .thenReturn(null);

        when(cardsFeignClient.fetchCardsDetails(
                "corr-123",
                "9876543210"))
                .thenReturn(ResponseEntity.ok(cardsDto));

        // Act

        CustomerDetailsDto result =
                customerService.fetchCustomerDetails(
                        "9876543210",
                        "corr-123"
                );

        // Assert

        assertNotNull(result);

        // Customer data still exists

        assertEquals(
                "Satyam",
                result.getName()
        );

        // Account data still exists

        assertNotNull(
                result.getAccountsDto()
        );

        // Loans should be absent

        assertNull(
                result.getLoansDto()
        );

        // Cards should still exist

        assertNotNull(
                result.getCardsDto()
        );

        verify(loansFeignClient)
                .fetchLoan(
                        "corr-123",
                        "9876543210"
                );
    }
    @Test
    void shouldReturnCustomerDetailsWhenCardsResponseIsNull() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(account));

        when(loansFeignClient.fetchLoan(
                "corr-123",
                "9876543210"))
                .thenReturn(ResponseEntity.ok(loansDto));

        when(cardsFeignClient.fetchCardsDetails(
                "corr-123",
                "9876543210"))
                .thenReturn(null);

        // Act

        CustomerDetailsDto result =
                customerService.fetchCustomerDetails(
                        "9876543210",
                        "corr-123"
                );

        // Assert

        assertNotNull(result);

        // Core data exists

        assertEquals(
                "Satyam",
                result.getName()
        );

        assertNotNull(
                result.getAccountsDto()
        );

        // Loans still available

        assertNotNull(
                result.getLoansDto()
        );

        // Cards missing

        assertNull(
                result.getCardsDto()
        );

        verify(cardsFeignClient)
                .fetchCardsDetails(
                        "corr-123",
                        "9876543210"
                );
    }
    @Test
    void shouldReturnCustomerDetailsWhenBothDownstreamResponsesAreNull() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(account));

        when(loansFeignClient.fetchLoan(
                "corr-123",
                "9876543210"))
                .thenReturn(null);

        when(cardsFeignClient.fetchCardsDetails(
                "corr-123",
                "9876543210"))
                .thenReturn(null);

        // Act

        CustomerDetailsDto result =
                customerService.fetchCustomerDetails(
                        "9876543210",
                        "corr-123"
                );

        // Assert

        assertNotNull(result);

        // Core customer information survives

        assertEquals(
                "Satyam",
                result.getName()
        );

        assertEquals(
                "9876543210",
                result.getMobileNumber()
        );

        // Account still available

        assertNotNull(
                result.getAccountsDto()
        );

        // Both enrichments unavailable

        assertNull(
                result.getLoansDto()
        );

        assertNull(
                result.getCardsDto()
        );

        verify(loansFeignClient)
                .fetchLoan(
                        "corr-123",
                        "9876543210"
                );

        verify(cardsFeignClient)
                .fetchCardsDetails(
                        "corr-123",
                        "9876543210"
                );
    }
}