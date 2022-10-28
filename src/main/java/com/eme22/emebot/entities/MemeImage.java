
package com.eme22.emebot.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import javax.persistence.Embeddable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "message",
    "meme"
})

@AllArgsConstructor
@With
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
@Data
public class MemeImage {

    @JsonProperty("message")
    private String message;
    @JsonProperty("meme")
    private String meme;

}
