package com.bidpro.service;

import com.bidpro.dao.LogDao;
import com.bidpro.entities.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogDao logDao;

    @Transactional
    public void logAction(String entityType, String entityID, String operation, String performedBy, String details) {
        Log log = new Log();
        log.setEntityType(entityType);
        log.setEntityID(entityID);
        log.setOperation(operation);
        log.setPerformedBy(performedBy);
        log.setLogTimestamp(LocalDateTime.now());
        log.setDetails(details);
        logDao.save(log);
    }

    public List<Log> findLogsByEntity(String entityType, String entityID) {
        return logDao.findByEntityTypeAndEntityID(entityType, entityID);
    }

    public List<Log> findLogsByEntityType(String entityType) {
        return logDao.findByEntityType(entityType);
    }

    public List<Log> findLogsByEntityID(String entityID) {
        return logDao.findByEntityID(entityID);
    }

    public List<Log> findLogsByPerformedBy(String performedBy) {
        return logDao.findByPerformedBy(performedBy);
    }

    public List<Log> findLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return logDao.findByDateRange(startDate, endDate);
    }
}