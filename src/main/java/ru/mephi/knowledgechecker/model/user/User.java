package ru.mephi.knowledgechecker.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mephi.knowledgechecker.model.answer.open.OpenAnswer;
import ru.mephi.knowledgechecker.model.test.Test;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder(toBuilder = true)
public class User {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String username;

    @ManyToMany // todo: fetch, cascade
    @JoinTable(
            name = "users_tests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "test_id")
    )
    private Set<Test> addedTests;

    @OneToMany(mappedBy = "creator", // todo: fetch, cascade
            fetch = FetchType.LAZY)
    private Set<Test> createdTests;

    @OneToMany(mappedBy = "user") // todo: fetch, cascade
    Set<OpenAnswer> openAnswers;
}
