package ru.mephi.knowledgechecker.model.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "variable_answers")
public class VariableAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String text;

//    @ManyToMany(mappedBy = "wrongAnswers") // а надо оно здесь вообще?
//    Set<VariableQuestion> wrongQuestions;
}
