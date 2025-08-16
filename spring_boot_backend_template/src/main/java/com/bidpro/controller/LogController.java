package com.bidpro.controller;

import com.bidpro.dto.ApiResponse;
import com.bidpro.dto.LogDTO;
import com.bidpro.entities.Log;
import com.bidpro.service.LogService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/entity/{entityType}/{entityID}")
    public ResponseEntity<ApiResponse<List<LogDTO>>> getLogsByEntity(@PathVariable String entityType, @PathVariable String entityID) {
        List<Log> logs = logService.findLogsByEntity(entityType, entityID);
        List<LogDTO> logDTOs = logs.stream()
                .map(log -> modelMapper.map(log, LogDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(logDTOs, "Logs retrieved successfully"));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<LogDTO>>> getLogsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<Log> logs = logService.findLogsByDateRange(startDate, endDate);
        List<LogDTO> logDTOs = logs.stream()
                .map(log -> modelMapper.map(log, LogDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(logDTOs, "Logs retrieved successfully"));
    }
}