package ru.mephi.knowledgechecker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mephi.knowledgechecker.dto.telegram.income.Update;
import ru.mephi.knowledgechecker.state.StateContext;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UpdateController {
    private final StateContext stateContext;

    @PostMapping
    public void giveUpdate(@RequestBody Update update) {
        log.info("Update: {}", update);
        stateContext.process(update);
        // todo: update validation
    }
}
