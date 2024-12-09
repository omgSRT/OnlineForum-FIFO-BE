package com.FA24SE088.OnlineForum.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataNotification {
    UUID id;
    String entity;
}
