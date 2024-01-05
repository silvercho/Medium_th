package com.ll.medium.domain.member.member.entity;

import com.ll.medium.global.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Builder
@Getter
@Setter
public class Member extends BaseEntity {
    private String username;
    private String password;
    private int membershipLevel;

    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));

        if (membershipLevel > 0)
            authorities.add(new SimpleGrantedAuthority("ROLE_PAID"));

        if (List.of("system", "admin").contains(username))
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return authorities;
    }

    public boolean isAdmin() {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
