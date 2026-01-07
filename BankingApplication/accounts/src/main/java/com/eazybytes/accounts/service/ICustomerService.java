package com.eazybytes.accounts.service;

import com.eazybytes.accounts.dto.CustomerDetailsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICustomerService
{
    /**
     *
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber,String correlationId);
}
