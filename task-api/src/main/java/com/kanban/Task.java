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
    String state;

    public Task(String id, String name, String state){
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public Task(String name){
        //Create a random id (quite awful this but kassandra is a monster on its own and there's no time to learn it properly.')
        this.id = Long.toString(System.currentTimeMillis());
        this.name = name;
        this.state = "CREATED";
    }
}
