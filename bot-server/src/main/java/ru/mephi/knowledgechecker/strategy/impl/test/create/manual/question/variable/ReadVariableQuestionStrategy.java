package ru.mephi.knowledgechecker.strategy.impl.test.create.manual.question.variable;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.mephi.knowledgechecker.common.TextType;
import ru.mephi.knowledgechecker.dto.telegram.income.Update;
import ru.mephi.knowledgechecker.dto.telegram.outcome.MessageEntity;
import ru.mephi.knowledgechecker.dto.telegram.outcome.params.SendMessageParams;
import ru.mephi.knowledgechecker.model.answer.VariableAnswer;
import ru.mephi.knowledgechecker.model.question.VariableQuestion;
import ru.mephi.knowledgechecker.model.test.Test;
import ru.mephi.knowledgechecker.model.user.CreationPhaseType;
import ru.mephi.knowledgechecker.model.user.CurrentData;
import ru.mephi.knowledgechecker.service.VariableAnswerService;
import ru.mephi.knowledgechecker.service.VariableQuestionService;
import ru.mephi.knowledgechecker.state.impl.test.create.manual.question.variable.WrongVariableAnswerAddingState;
import ru.mephi.knowledgechecker.strategy.StrategyProcessException;
import ru.mephi.knowledgechecker.strategy.impl.AbstractMessageStrategy;

import java.util.List;

import static ru.mephi.knowledgechecker.common.CommonMessageParams.addingWrongAnswerParams;
import static ru.mephi.knowledgechecker.common.ParamsWrapper.wrapMessageParams;

@Component
public class ReadVariableQuestionStrategy extends AbstractMessageStrategy {
    private final VariableQuestionService variableQuestionService;
    private final VariableAnswerService variableAnswerService;

    public ReadVariableQuestionStrategy(VariableQuestionService variableQuestionService,
                                        VariableAnswerService variableAnswerService,
                                        @Lazy WrongVariableAnswerAddingState wrongVariableAnswerAddingState) {
        this.variableQuestionService = variableQuestionService;
        this.variableAnswerService = variableAnswerService;
        this.nextState = wrongVariableAnswerAddingState;
    }

    @Override
    public boolean apply(CurrentData data, Update update) {
        return super.apply(data, update);
    }

    @Override
    public void process(CurrentData data, Update update) throws StrategyProcessException {
        String userText = update.getMessage().getText();
        switch (data.getNextPhase()) {
            case TEXT:
                readText(data, userText);
                break;
            case CORRECT_ANSWER:
                if (userText.length() > 30) {
                    throw new StrategyProcessException(data.getUser().getId(),
                            "Максимальная длина вариативного ответа 30 символов, попробуйте еще раз");
                }
                readCorrectAnswer(data, userText);
                break;
            case MAX_ANSWER_NUMBER:
                try {
                    int maxAnswerNumber = Integer.parseInt(userText);
                    if (maxAnswerNumber <= 0 || maxAnswerNumber > 9) {
                        throw new NumberFormatException();
                    }
                    readMaxAnswerNumber(data, maxAnswerNumber);
                } catch (NumberFormatException e) {
                    throw new StrategyProcessException(data.getUser().getId(),
                            "Неверный формат, попробуйте еще раз\nВведите число от 1 до 9");
                }
                break;
            default: // todo: add attachment
        }
    }

    private void readText(CurrentData data, String text) {
        Test test = data.getTest();
        VariableQuestion question = VariableQuestion.builder()
                .text(text)
                .test(test)
                .build();
        question = variableQuestionService.save(question);
        data.setVariableQuestion(question);
        data.setNextPhase(CreationPhaseType.CORRECT_ANSWER);

        String boldMessage = "Введите правильный ответ (максимум 30 символов)";
        String italicMessage = "\n\nПредпочтительно вводить короткие варианты ответа: A, B, etc.";
        SendMessageParams params = wrapMessageParams(data.getUser().getId(), boldMessage + italicMessage,
                List.of(new MessageEntity(TextType.BOLD, 0, boldMessage.length()),
                        new MessageEntity(TextType.UNDERLINE, 8, 10),
                        new MessageEntity(TextType.ITALIC, boldMessage.length(), italicMessage.length())),
                null);
        sendMessageAndSave(params, data);
    }

    private void readCorrectAnswer(CurrentData data, String correctAnswerText) throws StrategyProcessException {
        VariableQuestion question = data.getVariableQuestion();
        VariableAnswer answer = VariableAnswer.builder()
                .text(correctAnswerText)
                .build();
        try {
            answer = variableAnswerService.save(answer);
        } catch (RuntimeException e) {
            throw new StrategyProcessException(data.getUser().getId(),
                    "Максимальная длина варианта ответа – 30 символов, попробуйте еще раз");
        }
        question.setCorrectAnswer(answer);
        question = variableQuestionService.save(question);
        data.setVariableQuestion(question);
        data.setNextPhase(CreationPhaseType.MAX_ANSWER_NUMBER);

        String message = "Введите максимальное количество неверных ответов (от 1 до 9)";
        sendMessageAndSave(message, data);
    }

    private void readMaxAnswerNumber(CurrentData data, Integer maxAnswerNumber) {
        VariableQuestion question = data.getVariableQuestion();
        question.setMaxAnswerNumber(maxAnswerNumber + 1);
        question = variableQuestionService.save(question);
        data.setVariableQuestion(question);
        data.setNextPhase(null);

        SendMessageParams params = addingWrongAnswerParams(question, data.getUser().getId());
        data.setState(nextState);
        sendMessageAndSave(params, data);
    }
}
