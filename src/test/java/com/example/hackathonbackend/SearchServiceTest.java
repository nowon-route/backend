package com.example.hackathonbackend.service;

import com.example.hackathonbackend.model.SearchHistory;
import com.example.hackathonbackend.model.User;
import com.example.hackathonbackend.repository.SearchHistoryRepository;
import com.example.hackathonbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SearchServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchService searchService;  // 실제 서비스 클래스

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("kakao_123", "테스트유저");
    }

    @Test
    void testAddSearchHistory() {
        String query = "서울 맛집";
        searchService.addSearchHistory(user, query);

        verify(searchHistoryRepository, times(1))
                .save(any(SearchHistory.class));
    }

    @Test
    void testGetRecentSearchHistory() {
        List<SearchHistory> histories = Arrays.asList(
                new SearchHistory(user, "검색1"),
                new SearchHistory(user, "검색2")
        );

        when(searchHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(user))
                .thenReturn(histories);

        List<SearchHistory> result = searchService.getRecentSearchHistory(user);

        assertEquals(2, result.size());
        assertEquals("검색1", result.get(0).getQuery());
    }
}
