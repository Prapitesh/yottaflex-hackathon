package com.hackathon.api.repository;
import com.hackathon.api.entity.Offer;
import com.hackathon.api.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/** Persistence operations for employment offers and their lifecycle. */
public interface OfferRepository extends JpaRepository<Offer, String> {
 List<Offer> findByApplication_IdOrderByCreatedAtDesc(String applicationId);
 List<Offer> findByStatus(OfferStatus status);
 List<Offer> findAllByOrderByCreatedAtDesc();
}