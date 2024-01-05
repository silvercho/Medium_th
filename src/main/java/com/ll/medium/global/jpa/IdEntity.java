package com.ll.medium.global.jpa;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static jakarta.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class IdEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
}
