package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.entities.SessionType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionHeaderDTO {
    private Long id;
    private String name;
    private String host;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionType type;
}
