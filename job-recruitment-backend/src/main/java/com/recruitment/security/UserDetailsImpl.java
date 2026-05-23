package com.recruitment.security;

import com.recruitment.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private Integer role;
    private Integer status;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.status = user.getStatus();
        // 使用英文角色名用于Spring Security权限检查
        String roleName = getRoleNameForSecurity(user.getRole());
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleName)
        );
    }

    /**
     * 获取用于Spring Security的英文角色名
     */
    private String getRoleNameForSecurity(Integer role) {
        return switch (role) {
            case 1 -> "ADMIN";
            case 2 -> "OPERATOR";
            case 3 -> "COMPANY";
            case 4 -> "USER";
            default -> "USER";
        };
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
