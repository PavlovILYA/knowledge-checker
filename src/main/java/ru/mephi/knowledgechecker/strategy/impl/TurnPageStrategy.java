package ru.mephi.knowledgechecker.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.mephi.knowledgechecker.dto.telegram.income.Update;
import ru.mephi.knowledgechecker.dto.telegram.outcome.keyboard.KeyboardMarkup;
import ru.mephi.knowledgechecker.model.course.Course;
import ru.mephi.knowledgechecker.model.user.CurrentData;
import ru.mephi.knowledgechecker.service.CourseService;
import ru.mephi.knowledgechecker.service.TestService;
import ru.mephi.knowledgechecker.strategy.StrategyProcessException;

import static ru.mephi.knowledgechecker.common.Constants.*;
import static ru.mephi.knowledgechecker.common.KeyboardMarkups.*;
import static ru.mephi.knowledgechecker.common.MenuTitleType.*;

@Slf4j
@Component
public class TurnPageStrategy extends AbstractCallbackQueryStrategy {
    private final TestService testService;
    private final CourseService courseService;

    public TurnPageStrategy(TestService testService,
                            CourseService courseService) {
        this.testService = testService;
        this.courseService = courseService;
    }

    @Override
    public boolean apply(CurrentData data, Update update) {
        if (!super.apply(data, update)) {
            return false;
        }

        String prefix = update.getCallbackQuery().getData().split(COLON)[0];
        return prefix.equals(CREATED_TESTS_PAGE_PREFIX)
                || prefix.equals(SEARCH_TESTS_PAGE_PREFIX)
                || prefix.equals(OWN_COURSE_PAGE_PREFIX)
                || prefix.equals(PRIVATE_TESTS_PAGE_PREFIX)
                || prefix.equals(SEARCH_COURSES_PAGE_PREFIX)
                || prefix.equals(OUTPUT_QUERIES_PAGE_PREFIX);
    }

    @Override
    public void process(CurrentData data, Update update) throws StrategyProcessException {
        String prefix = update.getCallbackQuery().getData().split(COLON)[0];
        int pageNumber = Integer.parseInt(update.getCallbackQuery().getData().split(COLON)[1]);
        KeyboardMarkup markup;
        String message;

        switch (prefix) {
            case CREATED_TESTS_PAGE_PREFIX:
                message = PUBLIC_TEST_LIST.getTitle();
                Page<String> publicTests = testService.getCreatedTests(data.getUser().getId(), pageNumber);
                markup = getPublicTestMenuInlineKeyboardMarkup(publicTests);
                break;
            case SEARCH_TESTS_PAGE_PREFIX:
                message = TEST_SEARCH_RESULT.getTitle();
                Page<String> testTitlesPage = testService.findTests(data.getSearchKeyWords(),
                        data.getUser().getId(), pageNumber);
                markup = getTestSearchResultsInlineKeyboardMarkup(testTitlesPage);
                break;
            case PRIVATE_TESTS_PAGE_PREFIX:
                message = MANAGE_COURSE.getTitle() + data.getCourse().getTitle();
                Page<String> privateTestsPage = testService.getTestsByCourse(data.getCourse(), pageNumber);
                markup = getPrivateTestListInlineKeyboardMarkup(privateTestsPage, data.getCourse().getId());
                break;
            case OWN_COURSE_PAGE_PREFIX:
                message = ADMIN_MENU.getTitle();
                Page<Course> ownCoursesPage = courseService.getCoursesByCreatorId(data.getUser().getId(), pageNumber);
                markup = getOwnCoursesInlineKeyboardMarkup(ownCoursesPage);
                break;
            case SEARCH_COURSES_PAGE_PREFIX:
                message = COURSE_SEARCH_RESULT.getTitle();
                Page<Course> coursesPage = courseService.findCourses(data.getSearchKeyWords(),
                        data.getUser().getId(), pageNumber);
                markup = getCourseSearchResultsInlineKeyboardMarkup(coursesPage);
                break;
            case OUTPUT_QUERIES_PAGE_PREFIX:
                message = OUTPUT_COURSE_QUERIES.getTitle();
                Page<Course> outputQueriesPage = courseService.getCoursesByParticipantId(data.getUser().getId(),
                        false, pageNumber);
                markup = getOutputCourseQueriesInlineKeyboardMarkup(outputQueriesPage);
                break;
            default:
                return;
        }

        sendMenuAndSave(data, message, markup);
    }
}
