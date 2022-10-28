
package com.eme22.emebot.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "message",
        "emojilist"
})

@AllArgsConstructor
@With
@Getter
@Setter
@NoArgsConstructor
@Entity
public class RoleManager {

    @JsonProperty("id")
    @Id
    private Long id;
    @JsonProperty("message")
    @Transient
    private String message;
    @JsonProperty("emojilist")
    @ElementCollection
    private Map<String, String> emoji;
    @JsonProperty("toggled")
    private boolean toggled;
}
