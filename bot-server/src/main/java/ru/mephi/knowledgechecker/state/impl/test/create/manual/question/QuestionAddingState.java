package ru.mephi.knowledgechecker.state.impl.test.create.manual.question;

import org.springframework.stereotype.Component;
import ru.mephi.knowledgechecker.state.impl.AbstractBotState;
import ru.mephi.knowledgechecker.strategy.impl.menu.ToTestListStrategy;
import ru.mephi.knowledgechecker.strategy.impl.test.create.manual.question.open.AddOpenQuestionStrategy;
import ru.mephi.knowledgechecker.strategy.impl.test.create.manual.question.variable.AddVariableQuestionStrategy;

@Component
public class QuestionAddingState extends AbstractBotState {
    public QuestionAddingState(ToTestListStrategy toTestListStrategy,
                               AddOpenQuestionStrategy addOpenQuestionStrategy,
                               AddVariableQuestionStrategy addVariableQuestionStrategy) {
        availableStrategies.add(toTestListStrategy);
        availableStrategies.add(addOpenQuestionStrategy);
        availableStrategies.add(addVariableQuestionStrategy);
    }
}
