package com.bidpro.dao;

import com.bidpro.entities.SupportThread;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface SupportThreadDao extends JpaRepository<SupportThread,String> {
    List<SupportThread> findAllByOrderByUpdatedAtDesc();
    List<SupportThread> findByOwnerIDOrderByUpdatedAtDesc(String ownerID);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from SupportThread t where t.threadID=:id")
    Optional<SupportThread> findForUpdate(@Param("id") String id);
}
