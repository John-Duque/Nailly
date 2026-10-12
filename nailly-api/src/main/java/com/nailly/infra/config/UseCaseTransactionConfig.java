package com.nailly.infra.config;

import org.springframework.aop.Advisor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Cada chamada a {@code execute} de um caso de uso roda numa transação, sem que a camada
 * application precise conhecer o Spring (o equivalente a um {@code @Transactional} em cada um).
 * Exceção = rollback de tudo; a trava da profissional ao agendar dura o caso de uso inteiro.
 */
@Configuration(proxyBeanMethods = false)
public class UseCaseTransactionConfig {

    static final String USE_CASE_PACKAGE = "com.nailly.application.";

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static TransactionInterceptor useCaseTransactionInterceptor() {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());
        return interceptor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor useCaseTransactionAdvisor(
            @Qualifier("useCaseTransactionInterceptor") TransactionInterceptor interceptor) {
        NameMatchMethodPointcut executeMethod = new NameMatchMethodPointcut();
        executeMethod.setMappedName("execute");
        ComposablePointcut pointcut = new ComposablePointcut(useCases()).intersection((MethodMatcher) executeMethod);
        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }

    static ClassFilter useCases() {
        return type -> type.getName().startsWith(USE_CASE_PACKAGE) && type.getSimpleName().endsWith("UseCase");
    }
}
