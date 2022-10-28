package com.eme22.emebot.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Embeddable;
import java.util.Date;

@AllArgsConstructor
@With
@NoArgsConstructor
@Data
@Embeddable
public class Birthday {

    @JsonProperty("message")
    private String message;

    @JsonProperty("date")
    private Date date;

    @JsonProperty("user")
    private long userId;

    @JsonProperty("active")
    private boolean active;

}
