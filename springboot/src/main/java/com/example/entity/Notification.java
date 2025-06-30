package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    private Long id;
    private Long userId;
    private String type;      // POPUP, EMAIL, etc.
    private String title;
    private String content;
    private String status;    // UNREAD, READ
    private Timestamp createdAt;
    private Timestamp readAt;
}
