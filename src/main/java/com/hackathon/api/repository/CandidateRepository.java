package com.hackathon.api.repository;

import com.hackathon.api.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Database access for candidate master profiles.
 */
public interface CandidateRepository extends JpaRepository<Candidate, String> {

    Optional<Candidate> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<Candidate> findByFullNameContainingIgnoreCase(
            String nameFragment,
            Pageable pageable
    );
}
