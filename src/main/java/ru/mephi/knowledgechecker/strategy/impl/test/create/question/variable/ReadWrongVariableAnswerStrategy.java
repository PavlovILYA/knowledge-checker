package ru.mephi.knowledgechecker.strategy.impl.test.create.question.variable;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.mephi.knowledgechecker.dto.telegram.income.Update;
import ru.mephi.knowledgechecker.dto.telegram.outcome.MessageParams;
import ru.mephi.knowledgechecker.model.answer.VariableAnswer;
import ru.mephi.knowledgechecker.model.question.VariableQuestion;
import ru.mephi.knowledgechecker.service.VariableAnswerService;
import ru.mephi.knowledgechecker.service.VariableQuestionService;
import ru.mephi.knowledgechecker.state.impl.test.create.question.variable.WrongVariableAnswerAddingState;
import ru.mephi.knowledgechecker.strategy.impl.AbstractMessageStrategy;

import java.util.Map;

import static ru.mephi.knowledgechecker.common.KeyboardMarkups.getAddWrongVariableAnswerInlineKeyboardMarkup;
import static ru.mephi.knowledgechecker.common.ParamsWrapper.wrapMessageParams;

@Component
public class ReadWrongVariableAnswerStrategy extends AbstractMessageStrategy {
    private final VariableQuestionService variableQuestionService;
    private final VariableAnswerService variableAnswerService;

    public ReadWrongVariableAnswerStrategy(VariableQuestionService variableQuestionService,
                                           VariableAnswerService variableAnswerService,
                                           @Lazy WrongVariableAnswerAddingState wrongVariableAnswerAddingState) {
        this.variableQuestionService = variableQuestionService;
        this.variableAnswerService = variableAnswerService;
        this.nextState = wrongVariableAnswerAddingState;
    }

    @Override
    public boolean apply(Update update) {
        return super.apply(update);
    }

    @Override
    public void process(Update update, Map<String, Object> data) {
        VariableQuestion question = variableQuestionService.get((Long) data.get("questionId"));
        VariableAnswer answer = VariableAnswer.builder()
                .text(update.getMessage().getText())
                .build();
        answer = variableAnswerService.save(answer);
        question.getWrongAnswers().add(answer);
        question = variableQuestionService.save(question);
        MessageParams params =
                wrapMessageParams(update.getMessage().getFrom().getId(),
                        "???????????????? ???????????????? ??????????\n\n" +
                             "???? ???????????? ???????????? ?????????????????? " + question.getWrongAnswers().size() + " ???????????????? ??????????????\n" +
                             "???????????????????????? ???????????????????? ???????????????????????? ???????????????? ????????????????: " + question.getMaxAnswerNumber(),
                        getAddWrongVariableAnswerInlineKeyboardMarkup());
        putStateToContext(update.getMessage().getFrom().getId(), nextState, data);
        telegramApiClient.sendMessage(params);
    }
}
