
package com.eme22.emebot.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "question",
    "answers"
})

@AllArgsConstructor
@With
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Poll {

    @JsonProperty("id")
    @Id
    private Long id;
    @JsonProperty("question")
    private String question;
    @JsonProperty("answers")
    @ManyToMany(cascade = CascadeType.ALL)
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private List<Answer> answers = new ArrayList<>();

    public void addAnswer(Answer answer){
        answers.add(answer);
    }

    @JsonIgnore
    @Transient
    public Integer getAllVoteCount(){
        return answers.stream().mapToInt(answer -> answer.getVotes().size()).sum();
    }

    public void addVoteToAnswer(int answer,Long userId){
        Answer answer1 = answers.get(answer);
        if (answer1 == null) return;
        answer1.getVotes().add(userId);
    }

    public void removeVoteFromAnswer(int answer,Long userId){
        Answer answer1 = answers.get(answer);
        if (answer1 == null) return;
        answer1.getVotes().remove(userId);
    }

    @JsonIgnore
    public Integer getUserAnswer(Long userId){
        Answer answer = answers.stream().filter(answer1 -> answer1.getVotes().contains(userId)).findFirst().orElse(null);
        return answers.indexOf(answer);
    }

    public boolean isUserParticipating(Long userId){
        return answers.stream().anyMatch( answer -> answer.getVotes().contains(userId));
    }

    public boolean isUserParticipatingInAnswer(int answer, Long userId){
        return answers.get(answer).getVotes().contains(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Poll poll = (Poll) o;
        return id != null && Objects.equals(id, poll.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
