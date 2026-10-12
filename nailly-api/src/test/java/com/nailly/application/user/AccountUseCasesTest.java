package com.nailly.application.user;

import com.nailly.application.customer.RegisterCustomerUseCase;
import com.nailly.application.professional.RegisterProfessionalUseCase;
import com.nailly.domain.customer.Customer;
import com.nailly.domain.customer.CustomerGateway;
import com.nailly.domain.professional.ApprovalPolicy;
import com.nailly.domain.professional.HomeVisitPolicy;
import com.nailly.domain.professional.Professional;
import com.nailly.domain.professional.ProfessionalGateway;
import com.nailly.domain.shared.DomainException;
import com.nailly.domain.user.InvalidCredentialsException;
import com.nailly.domain.user.PasswordHasher;
import com.nailly.domain.user.Role;
import com.nailly.domain.user.User;
import com.nailly.domain.user.UserGateway;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Cadastro de conta (cliente e profissional) e login. */
@ExtendWith(MockitoExtension.class)
class AccountUseCasesTest {

    private static final String PASSWORD = "senha-forte-123";

    @Mock
    private UserGateway userGateway;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private ProfessionalGateway professionalGateway;
    @Mock
    private CustomerGateway customerGateway;

    private AccountRegistration registration() {
        return new AccountRegistration(userGateway, passwordHasher);
    }

    /** Simula o banco: a conta salva ganha o id informado. */
    private void givenNewUserGetsId(long id) {
        when(userGateway.existsByEmail("ana@nailly.com")).thenReturn(false);
        when(passwordHasher.hash(PASSWORD)).thenReturn("hash");
        when(userGateway.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(id, user.getEmail(), user.getPasswordHash(), user.getRole());
        });
    }

    private static User storedUser(Role role) {
        return new User(7L, "ana@nailly.com", "hash", role);
    }

    @Nested
    class Registration {

        @Test
        void registersCustomerWithAccount() {
            givenNewUserGetsId(7L);
            when(customerGateway.save(any())).thenAnswer(invocation -> {
                Customer customer = invocation.getArgument(0);
                return new Customer(20L, customer.getUserId(), customer.getName(), customer.getPhone());
            });

            AuthenticatedAccount account = new RegisterCustomerUseCase(registration(), customerGateway)
                    .execute(" Ana@Nailly.com ", PASSWORD, "Ana", "11999990000");

            assertThat(account.getUserId()).isEqualTo(7L);
            assertThat(account.getRole()).isEqualTo(Role.CUSTOMER);
            assertThat(account.getCustomerId()).isEqualTo(20L);
            assertThat(account.getProfessionalId()).isNull();
            ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
            verify(userGateway).save(user.capture());
            assertThat(user.getValue().getEmail()).isEqualTo("ana@nailly.com");
            assertThat(user.getValue().getPasswordHash()).isEqualTo("hash");
        }

        @Test
        void registersProfessionalWithDefaultRules() {
            givenNewUserGetsId(7L);
            when(professionalGateway.save(any())).thenAnswer(invocation -> {
                Professional professional = invocation.getArgument(0);
                return new Professional(10L, professional.getUserId(), professional.getName(), null, null,
                        professional.getApprovalPolicy(), professional.getHomeVisitPolicy());
            });

            AuthenticatedAccount account = new RegisterProfessionalUseCase(registration(), professionalGateway)
                    .execute("ana@nailly.com", PASSWORD, "Ana", null, null);

            assertThat(account.getRole()).isEqualTo(Role.PROFESSIONAL);
            assertThat(account.getProfessionalId()).isEqualTo(10L);
            assertThat(account.getCustomerId()).isNull();
            ArgumentCaptor<Professional> professional = ArgumentCaptor.forClass(Professional.class);
            verify(professionalGateway).save(professional.capture());
            assertThat(professional.getValue().getUserId()).isEqualTo(7L);
            assertThat(professional.getValue().getApprovalPolicy()).isEqualTo(ApprovalPolicy.defaults());
        }

        @Test
        void refusesEmailAlreadyRegistered() {
            when(userGateway.existsByEmail("ana@nailly.com")).thenReturn(true);
            AccountRegistration registration = registration();

            assertThatThrownBy(() -> registration.create("ANA@nailly.com", PASSWORD, Role.CUSTOMER))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Este e-mail já está cadastrado.");
            verify(userGateway, never()).save(any());
        }

        @Test
        void refusesWeakPasswordBeforeHashing() {
            when(userGateway.existsByEmail("ana@nailly.com")).thenReturn(false);
            AccountRegistration registration = registration();

            assertThatThrownBy(() -> registration.create("ana@nailly.com", "curta", Role.CUSTOMER))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("A senha deve ter pelo menos 8 caracteres.");
            verifyNoInteractions(passwordHasher);
        }
    }

    @Nested
    class Login {

        private AuthenticateUseCase useCase() {
            return new AuthenticateUseCase(userGateway, passwordHasher, professionalGateway, customerGateway);
        }

        @Test
        void professionalReceivesHerProfile() {
            when(userGateway.findByEmail("ana@nailly.com")).thenReturn(Optional.of(storedUser(Role.PROFESSIONAL)));
            when(passwordHasher.matches(PASSWORD, "hash")).thenReturn(true);
            when(professionalGateway.findByUserId(7L)).thenReturn(Optional.of(new Professional(10L, 7L, "Ana",
                    null, null, ApprovalPolicy.defaults(), HomeVisitPolicy.notOffered())));

            AuthenticatedAccount account = useCase().execute(" ANA@nailly.com", PASSWORD);

            assertThat(account.getUserId()).isEqualTo(7L);
            assertThat(account.getProfessionalId()).isEqualTo(10L);
            assertThat(account.getCustomerId()).isNull();
            verifyNoInteractions(customerGateway);
        }

        @Test
        void customerReceivesHerProfile() {
            when(userGateway.findByEmail("ana@nailly.com")).thenReturn(Optional.of(storedUser(Role.CUSTOMER)));
            when(passwordHasher.matches(PASSWORD, "hash")).thenReturn(true);
            when(customerGateway.findByUserId(7L)).thenReturn(Optional.of(new Customer(20L, 7L, "Ana", "119")));

            AuthenticatedAccount account = useCase().execute("ana@nailly.com", PASSWORD);

            assertThat(account.getCustomerId()).isEqualTo(20L);
            assertThat(account.getProfessionalId()).isNull();
            verifyNoInteractions(professionalGateway);
        }

        @Test
        void adminHasNoProfile() {
            when(userGateway.findByEmail("ana@nailly.com")).thenReturn(Optional.of(storedUser(Role.ADMIN)));
            when(passwordHasher.matches(PASSWORD, "hash")).thenReturn(true);

            AuthenticatedAccount account = useCase().execute("ana@nailly.com", PASSWORD);

            assertThat(account.getRole()).isEqualTo(Role.ADMIN);
            assertThat(account.getProfessionalId()).isNull();
            assertThat(account.getCustomerId()).isNull();
        }

        @Test
        void wrongPasswordIsRefused() {
            when(userGateway.findByEmail("ana@nailly.com")).thenReturn(Optional.of(storedUser(Role.CUSTOMER)));
            when(passwordHasher.matches("errada123", "hash")).thenReturn(false);
            AuthenticateUseCase useCase = useCase();

            assertThatThrownBy(() -> useCase.execute("ana@nailly.com", "errada123"))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("E-mail ou senha inválidos.");
        }

        @Test
        void deactivatedAccountIsRefusedWithSameMessage() {
            User deactivated = storedUser(Role.CUSTOMER);
            deactivated.deactivate();
            when(userGateway.findByEmail("ana@nailly.com")).thenReturn(Optional.of(deactivated));
            AuthenticateUseCase useCase = useCase();

            assertThatThrownBy(() -> useCase.execute("ana@nailly.com", PASSWORD))
                    .isInstanceOf(InvalidCredentialsException.class);
            verifyNoInteractions(passwordHasher);
        }

        @Test
        void unknownOrMalformedEmailIsRefusedWithSameMessage() {
            when(userGateway.findByEmail("nao@existe.com")).thenReturn(Optional.empty());
            AuthenticateUseCase useCase = useCase();

            assertThatThrownBy(() -> useCase.execute("nao@existe.com", PASSWORD))
                    .isInstanceOf(InvalidCredentialsException.class);
            assertThatThrownBy(() -> useCase.execute("sem-arroba", PASSWORD))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("E-mail ou senha inválidos.");
        }
    }
}
