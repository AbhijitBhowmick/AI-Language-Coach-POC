package com.coach.identity;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class TenantAwareUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public TenantAwareUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String tenantId = TenantContext.getTenantId();
        
        if (tenantId == null || tenantId.isBlank()) {
            throw new UsernameNotFoundException("Tenant context not set");
        }

        return userRepository.findByEmailAndTenantId(username, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public UserDetails loadUserByUsernameAndTenant(String username, String tenantId) {
        return userRepository.findByEmailAndTenantId(username, tenantId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username + " in tenant " + tenantId));
    }
}