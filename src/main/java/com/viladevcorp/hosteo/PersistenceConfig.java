package com.viladevcorp.hosteo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.viladevcorp.hosteo.auth.AuditorAwareImpl;
import com.viladevcorp.hosteo.model.User;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {
    
    @Bean
    AuditorAware<User> auditorProvider() {
        return new AuditorAwareImpl();
    }

}
