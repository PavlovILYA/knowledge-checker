package ru.mephi.knowledgechecker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mephi.knowledgechecker.model.user.CurrentData;
import ru.mephi.knowledgechecker.model.user.User;
import ru.mephi.knowledgechecker.repository.CurrentDataRepository;
import ru.mephi.knowledgechecker.state.impl.InitialState;

import static ru.mephi.knowledgechecker.dto.telegram.income.mapper.UserMapper.mapStateToBeanName;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentDataService {
    private final CurrentDataRepository dataRepository;

    public CurrentData getByUserId(Long userId) {
        return dataRepository.findByUserId(userId);
    }

    public CurrentData createForUser(User user) {
        CurrentData data = CurrentData.builder()
                .user(user)
                .state(mapStateToBeanName(InitialState.class))
                .build();
        data = dataRepository.save(data);
        log.info("Saved data: {}", data);
        return data;
    }

    public CurrentData update(CurrentData data) {
        data = dataRepository.save(data);
        log.info("Updated data: {}", data);
        return data;
    }
}
