package com.bidpro.dao;
import com.bidpro.entities.EmailOutbox;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface EmailOutboxDao extends JpaRepository<EmailOutbox, Long> {
    List<EmailOutbox> findTop100ByOrderByCreatedAtDesc();
    List<EmailOutbox> findTop25ByStatusAndAttemptsLessThanAndNextAttemptAtLessThanEqualOrderByIdAsc(EmailOutbox.Status status, int attempts, LocalDateTime now);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EmailOutbox e where e.id = :id")
    Optional<EmailOutbox> findForUpdate(@Param("id") Long id);
}
