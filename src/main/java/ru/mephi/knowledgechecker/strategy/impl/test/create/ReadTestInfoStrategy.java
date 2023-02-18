package ru.mephi.knowledgechecker.strategy.impl.test.create;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.mephi.knowledgechecker.common.CreationPhaseType;
import ru.mephi.knowledgechecker.common.TextType;
import ru.mephi.knowledgechecker.dto.telegram.income.Update;
import ru.mephi.knowledgechecker.dto.telegram.outcome.MessageEntity;
import ru.mephi.knowledgechecker.dto.telegram.outcome.MessageParams;
import ru.mephi.knowledgechecker.model.test.Test;
import ru.mephi.knowledgechecker.model.user.CurrentData;
import ru.mephi.knowledgechecker.model.user.User;
import ru.mephi.knowledgechecker.service.TestService;
import ru.mephi.knowledgechecker.state.impl.test.create.question.QuestionAddingState;
import ru.mephi.knowledgechecker.strategy.StrategyProcessException;
import ru.mephi.knowledgechecker.strategy.impl.AbstractMessageStrategy;

import java.util.List;

import static ru.mephi.knowledgechecker.common.CommonMessageParams.addingQuestionParams;
import static ru.mephi.knowledgechecker.common.ParamsWrapper.wrapMessageParams;

@Component
public class ReadTestInfoStrategy extends AbstractMessageStrategy {
    private final TestService testService;

    public ReadTestInfoStrategy(TestService testService,
                                @Lazy QuestionAddingState questionAddingState) {
        this.testService = testService;
        this.nextState = questionAddingState;
    }

    @Override
    public boolean apply(Update update) {
        return super.apply(update);
    }

    @Override
    public void process(User user, Update update) throws StrategyProcessException {
        CurrentData data = user.getData();
        Test test = data.getTest();
        CreationPhaseType nextPhase = data.getNextPhase();
        switch (nextPhase) {
            case TITLE:
                readTitle(data, test, update.getMessage().getText());
                break;
            case MAX_QUESTION_NUMBER:
                try {
                    int maxQuestionNumber = Integer.parseInt(update.getMessage().getText());
                    if (maxQuestionNumber <= 0 || maxQuestionNumber > 50) {
                        throw new NumberFormatException();
                    }
                    readMaxQuestionsNumber(data, test, maxQuestionNumber);
                } catch (NumberFormatException e) {
                    throw new StrategyProcessException(user.getId(),
                            "Неверный формат, попробуйте еще раз:\n" +
                            "Введите число от 1 до 50");
                }
                break;
            default: // todo: add attachment
        }
    }

    private void readTitle(CurrentData data, Test test, String title) {
        test.setTitle(title);
        test = testService.save(test);
        data.setTest(test);
        data.setNextPhase(CreationPhaseType.MAX_QUESTION_NUMBER);
        saveToContext(data);

        String boldMessage = "Введите количество отображаемых вопросов (от 1 до 50)";
        String italicMessage =
                "\n\n(Вопросов можно будет создать больше, тогда будет браться случайная выборка)";
        MessageParams params = wrapMessageParams(data.getUser().getId(), boldMessage + italicMessage,
                List.of(new MessageEntity(TextType.BOLD, 0, boldMessage.length()),
                        new MessageEntity(TextType.UNDERLINE, 19, 12),
                        new MessageEntity(TextType.ITALIC, boldMessage.length(), italicMessage.length())),
                null);
        telegramApiClient.sendMessage(params);
    }

    private void readMaxQuestionsNumber(CurrentData data, Test test, Integer maxQuestionsNumber) {
        test.setMaxQuestionsNumber(maxQuestionsNumber);
        test = testService.save(test);
        data.setTest(test);
        data.setNextPhase(null);
        data.setNeedCheck(true);
        saveToContext(nextState, data);

        MessageParams params = addingQuestionParams(test, data.getUser().getId());
        telegramApiClient.sendMessage(params);
    }
}
