package ru.mephi.knowledgechecker.strategy.impl.menu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.mephi.knowledgechecker.common.CallbackDataType;
import ru.mephi.knowledgechecker.dto.telegram.income.Update;
import ru.mephi.knowledgechecker.dto.telegram.outcome.keyboard.KeyboardMarkup;
import ru.mephi.knowledgechecker.model.course.Course;
import ru.mephi.knowledgechecker.model.test.Test;
import ru.mephi.knowledgechecker.model.test.VisibilityType;
import ru.mephi.knowledgechecker.model.user.CurrentData;
import ru.mephi.knowledgechecker.service.TestService;
import ru.mephi.knowledgechecker.state.impl.menu.TestListState;
import ru.mephi.knowledgechecker.strategy.StrategyProcessException;
import ru.mephi.knowledgechecker.strategy.impl.AbstractCallbackQueryStrategy;

import static ru.mephi.knowledgechecker.common.CallbackDataType.*;
import static ru.mephi.knowledgechecker.common.KeyboardMarkups.*;
import static ru.mephi.knowledgechecker.common.MenuTitleType.MANAGE_COURSE;
import static ru.mephi.knowledgechecker.common.MenuTitleType.PUBLIC_TEST_LIST;

@Slf4j
@Component
public class ToTestListStrategy extends AbstractCallbackQueryStrategy {

    private final TestService testService;

    public ToTestListStrategy(TestService testService,
                              @Lazy TestListState testListState) {
        this.nextState = testListState;
        this.testService = testService;
    }

    @Override
    public boolean apply(CurrentData data, Update update) {
        return super.apply(data, update)
                && (
                        update.getCallbackQuery().getData().equals(TO_PUBLIC_TEST_LIST.name())
                        ||
                        update.getCallbackQuery().getData().equals(DELETE_TEST.name())
                        ||
                        update.getCallbackQuery().getData().equals(TO_PRIVATE_TEST_LIST.name())
                        ||
                        update.getCallbackQuery().getData().equals(TO_TRAIN_TESTS.name())
                        ||
                        update.getCallbackQuery().getData().equals(TO_ESTIMATED_TESTS.name())
                );
    }

    @Override
    public void process(CurrentData data, Update update) throws StrategyProcessException {
        Test test = data.getTest();
        if (update.getCallbackQuery().getData().equals(DELETE_TEST.name())) {
            testService.delete(test.getId());
        }
        if (data.isNeedCheck()) {
            if (test.getOpenQuestions().size() + test.getVariableQuestions().size() == 0) {
                throw new StrategyProcessException(data.getUser().getId(),
                        "Необходимо добавить как минимум один вопрос",
                        update.getCallbackQuery().getId());
            }
            data.setNeedCheck(false);
        }
        data.setTest(null);

        data.setState(nextState);
        if (data.getCourse() == null) {
            Page<String> publicTests = testService.getCreatedTests(data.getUser().getId());
            sendMenuAndSave(data, PUBLIC_TEST_LIST.getTitle(), getPublicTestMenuKeyboardMarkup(publicTests));
        } else {
            Course course = data.getCourse();
            String message;
            KeyboardMarkup markup;
            switch (CallbackDataType.valueOf(update.getCallbackQuery().getData())) {
                case DELETE_TEST:
                case TO_PRIVATE_TEST_LIST:
                    message = MANAGE_COURSE.getTitle() + course.getTitle() + " – ТЕСТЫ";
                    Page<String> privateTestsPage = testService.getTestsByCourse(course);
                    markup = getOwnPrivateTestListKeyboardMarkup(privateTestsPage, course.getId());
                    break;
                case TO_TRAIN_TESTS:
                    message = MANAGE_COURSE.getTitle() + course.getTitle() + " – ТРЕНИРОВОЧНЫЕ ТЕСТЫ";
                    Page<String> trainTestsPage = testService.getTestsByCourseAndVisibility(
                            course, VisibilityType.TRAIN);
                    markup = getStudiedPrivateTestListKeyboardMarkup(
                            trainTestsPage, course.getId(), VisibilityType.TRAIN);
                    break;
                case TO_ESTIMATED_TESTS:
                    message = MANAGE_COURSE.getTitle() + course.getTitle() + " – ТЕСТЫ НА ОЦЕНКУ";
                    Page<String> estimatedTestsPage =
                            testService.getTestsByCourseAndVisibility(course, VisibilityType.ESTIMATED);
                    markup = getStudiedPrivateTestListKeyboardMarkup(
                            estimatedTestsPage, course.getId(), VisibilityType.ESTIMATED);
                    break;
                default:
                    return;
            }

            sendMenuAndSave(data, message, markup);
        }
    }
}
