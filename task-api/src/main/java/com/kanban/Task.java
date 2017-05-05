package com.kanban;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.annotation.concurrent.Immutable;

@Value
@Builder
@Immutable
@JsonDeserialize

public final class Task {
    String id;
    String name;
    String details;
    String color;
    String boardId;
    String state;

    public Task(String id, String name, String details, String color, String boardId, String state) {
        this.id = id;
        this.name = name;
        this.details = details;
        this.color = color;
        this.boardId = boardId;
        this.state = state;
    }
}
