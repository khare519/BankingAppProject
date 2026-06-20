package com.eazybytes.accounts.controller;

import com.eazybytes.accounts.dto.AccountsContactInfoDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.service.IAccountsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = AccountsController.class,
        excludeAutoConfiguration = {
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
        }
)
@ActiveProfiles("test")
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAccountsService accountsService;

    @MockitoBean
    private AccountsContactInfoDto accountsContactInfoDto;

    @TestConfiguration
    static class TestAuditConfig {

        @Bean(name = "auditorAwareImpl")
        AuditorAware<String> auditorAware() {

            return () -> Optional.of("test-user");
        }
    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {

        String requestBody = """
                {
                  "name":"Satyam Khare",
                  "email":"satyam@gmail.com",
                  "mobileNumber":"9876543210"
                }
                """;

        doNothing()
                .when(accountsService)
                .createAccount(any(CustomerDto.class));

        mockMvc.perform(
                        post("/api/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.statusCode")
                        .value("201"))

                .andExpect(jsonPath("$.statusMessage")
                        .value("Account created successfully"));

        verify(accountsService)
                .createAccount(any(CustomerDto.class));
    }
}