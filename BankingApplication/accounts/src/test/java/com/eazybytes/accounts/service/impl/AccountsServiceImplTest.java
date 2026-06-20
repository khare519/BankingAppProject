package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.AccountsMsgDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entities.Accounts;
import com.eazybytes.accounts.entities.Customer;
import com.eazybytes.accounts.exceptions.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exceptions.ResourceNotFoundException;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private AccountsServiceImpl accountsService;

    private CustomerDto customerDto;
    private Customer customer;
    private Accounts account;

    @BeforeEach
    void setUp() {

        customerDto = new CustomerDto();
        customerDto.setName("Satyam");
        customerDto.setEmail("satyam@test.com");
        customerDto.setMobileNumber("9876543210");

        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("Satyam");
        customer.setEmail("satyam@test.com");
        customer.setMobileNumber("9876543210");

        account = new Accounts();
        account.setAccountNumber(1234567890L);
        account.setCustomerId(1L);
        account.setAccountType("Savings");
        account.setBranchAddress("123 Main Street, New York");
    }
    // Below 6 TCs are for createAccount() method. Concepts covered:
    /*
    1. Mocking	✅
       Stubbing	✅
       thenReturn()	✅
       thenThrow()	✅
       thenAnswer()	✅
       verify()	✅
       never()	✅
       ArgumentCaptor	✅
       Exception testing	✅
       Failure-path testing	✅
       Event testing	✅
       Interaction testing	✅
       InOrder verification ✅
     */
    @Test
    void shouldCreateAccountSuccessfully() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);

        when(accountsRepository.save(any(Accounts.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(streamBridge.send(anyString(), any()))
                .thenReturn(true);

        // Act

        accountsService.createAccount(customerDto);

        // Assert

        verify(customerRepository, times(1))
                .save(any(Customer.class));

        // Capture Accounts object

        ArgumentCaptor<Accounts> accountsCaptor =
                ArgumentCaptor.forClass(Accounts.class);

        //verify(accountsRepository).save(accountsCaptor.capture());
        verify(accountsRepository,times(1)).save(accountsCaptor.capture());

        Accounts capturedAccount = accountsCaptor.getValue();

        assertEquals(1L, capturedAccount.getCustomerId());

        assertEquals(
                "Savings",
                capturedAccount.getAccountType()
        );

        assertEquals(
                "123 Main Street, New York",
                capturedAccount.getBranchAddress()
        );

        assertNotNull(capturedAccount.getAccountNumber());

        verify(streamBridge, times(1))
                .send(eq("sendCommunication-out-0"), any());
    }
    @Test
    void shouldThrowExceptionWhenCustomerAlreadyExists() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        // Act + Assert

        CustomerAlreadyExistsException exception =
                assertThrows(
                        CustomerAlreadyExistsException.class,
                        () -> accountsService.createAccount(customerDto)
                );

        assertEquals(
                "Customer already exists with this mobile number 9876543210",
                exception.getMessage()
        );

        // Verify no further interactions happened

        verify(customerRepository, never())
                .save(any(Customer.class));

        verify(accountsRepository, never())
                .save(any(Accounts.class));

        verify(streamBridge, never())
                .send(anyString(), any());
    }
    @Test
    void shouldThrowExceptionWhenCustomerSaveFails(){
        // Arrange
        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenThrow(new RuntimeException("Database failure while saving customer"));

        //Act + Assert
        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () ->accountsService.createAccount(customerDto));

        assertEquals(
                "Database failure while saving customer",
                exception.getMessage()
        );
        verify(customerRepository,times(1))
                .save(any(Customer.class));
        // Verify account was never saved
        verify(accountsRepository,never())
                .save(any(Accounts.class));
        // Verify event was never published
        verify(streamBridge,never())
                .send(anyString(),any());
    }
    @Test
    void shouldThrowExceptionWhenAccountSaveFails() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);

        when(accountsRepository.save(any(Accounts.class)))
                .thenThrow(new RuntimeException("Database failure while saving account"));

        // Act + Assert

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> accountsService.createAccount(customerDto)
        );

        assertEquals(
                "Database failure while saving account",
                exception.getMessage()
        );

        // Verify customer save happened

        verify(customerRepository, times(1))
                .save(any(Customer.class));

        // Verify account save attempted

        verify(accountsRepository, times(1))
                .save(any(Accounts.class));

        // Verify communication event was NOT triggered

        verify(streamBridge, never())
                .send(anyString(), any());
    }
    @Test
    void shouldPublishCommunicationEventWithCorrectPayload(){
        // Arrange
        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);
        when(accountsRepository.save(any(Accounts.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(streamBridge.send(anyString(),any()))
                .thenReturn(true);

        // Act
        accountsService.createAccount(customerDto);

        // Assert
        verify(customerRepository,times(1))
                .save(any(Customer.class));

        ArgumentCaptor<Accounts> accountsCaptor = ArgumentCaptor.forClass(Accounts.class);
        verify(accountsRepository,times(1))
                .save(accountsCaptor.capture());
        Accounts savedAccount = accountsCaptor.getValue();

        ArgumentCaptor<AccountsMsgDto> accountsDtoCaptor = ArgumentCaptor.forClass(AccountsMsgDto.class);
        verify(streamBridge,times(1))
                .send(eq("sendCommunication-out-0"),accountsDtoCaptor.capture());
        AccountsMsgDto accountsMsgDto = accountsDtoCaptor.getValue();

        // Verify payload fields

        assertEquals(
                savedAccount.getAccountNumber(),
                accountsMsgDto.accountNumber()
        );

        assertEquals(
                "Satyam",
                accountsMsgDto.name()
        );

        assertEquals(
                "satyam@test.com",
                accountsMsgDto.email()
        );

        assertEquals(
                "9876543210",
                accountsMsgDto.mobileNumber()
        );
    }
    @Test
    void shouldExecuteCreateAccountFlowInCorrectOrder() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);

        when(accountsRepository.save(any(Accounts.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(streamBridge.send(anyString(), any()))
                .thenReturn(true);

        // Act

        accountsService.createAccount(customerDto);

        // Assert

        InOrder inOrder = inOrder(
                customerRepository,
                accountsRepository,
                streamBridge
        );

        // Verify sequence

        inOrder.verify(customerRepository)
                .findByMobileNumber("9876543210");

        inOrder.verify(customerRepository)
                .save(any(Customer.class));

        inOrder.verify(accountsRepository)
                .save(any(Accounts.class));

        inOrder.verify(streamBridge)
                .send(eq("sendCommunication-out-0"), any());
    }

    // getCustomerDetails() TCs
    @Test
    void shouldFetchCustomerDetailsSuccessfully() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(account));

        // Act

        CustomerDto result =
                accountsService.getCustomerDetails("9876543210");

        // Assert

        assertNotNull(result);

        // Customer assertions

        assertEquals("Satyam", result.getName());

        assertEquals(
                "satyam@test.com",
                result.getEmail()
        );

        assertEquals(
                "9876543210",
                result.getMobileNumber()
        );

        // Nested AccountsDto assertions

        assertNotNull(result.getAccountsDto());

        assertEquals(
                1234567890L,
                result.getAccountsDto().getAccountNumber()
        );

        assertEquals(
                "Savings",
                result.getAccountsDto().getAccountType()
        );

        assertEquals(
                "123 Main Street, New York",
                result.getAccountsDto().getBranchAddress()
        );

        // Verify repository interactions

        verify(customerRepository,times(1))
                .findByMobileNumber("9876543210");

        verify(accountsRepository,times(1))
                .findByCustomerId(1L);
    }
    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        // Act + Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountsService.getCustomerDetails("9876543210")
        );

        assertEquals(
                "Customer not found with the given input data MobileNumber : '9876543210'",
                exception.getMessage()
        );

        // Verify accounts repository was never called

        verify(accountsRepository, never())
                .findByCustomerId(anyLong());
    }
    @Test
    void shouldThrowExceptionWhenAccountNotFound() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.empty());

        // Act + Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountsService.getCustomerDetails("9876543210")
        );

        assertEquals(
                "Accounts not found with the given input data Customer : '1'",
                exception.getMessage()
        );

        // Verify interactions

        verify(customerRepository,times(1))
                .findByMobileNumber("9876543210");

        verify(accountsRepository,times(1))
                .findByCustomerId(1L);
    }
    // updateAccount() method
    @Test
    void shouldUpdateAccountSuccessfully() {

        // Arrange

        AccountsDto accountsDto = new AccountsDto();
        accountsDto.setAccountNumber(1234567890L);
        accountsDto.setAccountType("Current");
        accountsDto.setBranchAddress("Mumbai Branch");

        customerDto.setName("Updated Satyam");
        customerDto.setEmail("updated@test.com");
        customerDto.setAccountsDto(accountsDto);

        when(accountsRepository.findById(1234567890L))
                .thenReturn(Optional.of(account));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        // Act

        boolean result = accountsService.updateAccount(customerDto);

        // Assert

        assertTrue(result);

        // Verify saves happened

        verify(accountsRepository,times(1))
                .save(account);

        verify(customerRepository,times(1))
                .save(customer);

        // Verify updated account fields

        assertEquals(
                "Current",
                account.getAccountType()
        );

        assertEquals(
                "Mumbai Branch",
                account.getBranchAddress()
        );

        // Verify updated customer fields

        assertEquals(
                "Updated Satyam",
                customer.getName()
        );

        assertEquals(
                "updated@test.com",
                customer.getEmail()
        );
    }
    /*
    Difference between never() and verifyNoInteractions()
    never() : checks specific method never called like on customerRepo the save() method is never called
    verifyNoInteractions() : checks NO methods at all called like on customerRepo.
     */
    @Test
    void shouldReturnFalseWhenAccountsDtoIsNull() {

        // Arrange

        customerDto.setAccountsDto(null);

        // Act

        boolean result = accountsService.updateAccount(customerDto);

        // Assert

        assertFalse(result);

        // Verify no repository interactions

        verifyNoInteractions(accountsRepository);

        verifyNoInteractions(customerRepository);
    }
    @Test
    void shouldThrowExceptionWhenAccountNotFoundDuringUpdate() {

        // Arrange

        AccountsDto accountsDto = new AccountsDto();
        accountsDto.setAccountNumber(1234567890L);
        accountsDto.setAccountType("Current");
        accountsDto.setBranchAddress("Mumbai Branch");

        customerDto.setAccountsDto(accountsDto);

        when(accountsRepository.findById(1234567890L))
                .thenReturn(Optional.empty());

        // Act + Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountsService.updateAccount(customerDto)
        );

        assertEquals(
                "Accounts not found with the given input data AccountNumber : '1234567890'",
                exception.getMessage()
        );

        // Verify customer repository never called

        verifyNoInteractions(customerRepository);

        // Verify save never happened

        verify(accountsRepository, never())
                .save(any(Accounts.class));
    }
    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringUpdate() {

        // Arrange

        AccountsDto accountsDto = new AccountsDto();
        accountsDto.setAccountNumber(1234567890L);
        accountsDto.setAccountType("Current");
        accountsDto.setBranchAddress("Mumbai Branch");

        customerDto.setAccountsDto(accountsDto);

        when(accountsRepository.findById(1234567890L))
                .thenReturn(Optional.of(account));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act + Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountsService.updateAccount(customerDto)
        );

        assertEquals(
                "Customer not found with the given input data CustomerId : '1'",
                exception.getMessage()
        );

        // Verify account save happened BEFORE failure

        verify(accountsRepository)
                .save(account);

        // Verify customer save never happened

        verify(customerRepository, never())
                .save(any(Customer.class));
    }
    //deleteAccount()
    @Test
    void shouldDeleteAccountSuccessfully() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        // Act

        boolean result =
                accountsService.deleteAccount("9876543210");

        // Assert

        assertTrue(result);

        // Verify customer lookup

        verify(customerRepository)
                .findByMobileNumber("9876543210");

        // Verify account deletion

        verify(accountsRepository)
                .deleteByCustomerId(1L);

        // Verify customer deletion

        verify(customerRepository)
                .deleteById(1L);
    }
    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringDelete() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        // Act + Assert

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountsService.deleteAccount("9876543210")
        );

        assertEquals(
                "Customer not found with the given input data MobileNumber : '9876543210'",
                exception.getMessage()
        );

        // Verify delete operations never happened

        verify(accountsRepository, never())
                .deleteByCustomerId(anyLong());

        verify(customerRepository, never())
                .deleteById(anyLong());
    }
    @Test
    void shouldExecuteDeleteWorkflowInCorrectOrder() {

        // Arrange

        when(customerRepository.findByMobileNumber("9876543210"))
                .thenReturn(Optional.of(customer));

        // Act

        accountsService.deleteAccount("9876543210");

        // Assert

        InOrder inOrder = inOrder(
                customerRepository,
                accountsRepository
        );

        // Verify sequence

        inOrder.verify(customerRepository)
                .findByMobileNumber("9876543210");

        inOrder.verify(accountsRepository)
                .deleteByCustomerId(1L);

        inOrder.verify(customerRepository)
                .deleteById(1L);
    }
    //updateCommunicationStatus() method
    @Test
    void shouldUpdateCommunicationStatusSuccessfully() {

        // Arrange

        account.setCommunicationSw(false);

        when(accountsRepository.findById(1234567890L))
                .thenReturn(Optional.of(account));

        // Act

        boolean result =
                accountsService.updateCommunicationStatus(1234567890L);

        // Assert

        assertTrue(result);

        // Verify state mutation

        assertTrue(account.getCommunicationSw());

        // Verify interactions

        verify(accountsRepository)
                .findById(1234567890L);

        verify(accountsRepository)
                .save(account);
    }
    @Test
    void shouldReturnFalseWhenAccountNumberIsNull() {

        // Act

        boolean result =
                accountsService.updateCommunicationStatus(null);

        // Assert

        assertFalse(result);

        // Verify no repository interactions

        verifyNoInteractions(accountsRepository);
    }
    @Test
    void shouldThrowExceptionWhenAccountNotFoundDuringCommunicationUpdate(){
        //Arrange
        account.setCommunicationSw(false);
        when(accountsRepository.findById(1234567890L)).thenReturn(Optional.empty());

        //Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountsService.updateCommunicationStatus(1234567890L)
        );
        assertEquals(
                "Account not found with the given input data AccountNumber : '1234567890'",
                exception.getMessage()
        );
        verify(accountsRepository,never()).save(any(Accounts.class));
    }
}