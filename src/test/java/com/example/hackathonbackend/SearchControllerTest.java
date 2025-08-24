package com.example.hackathonbackend;

import com.example.hackathonbackend.model.SearchHistory;
import com.example.hackathonbackend.model.User;
import com.example.hackathonbackend.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("kakao_123", "테스트유저");
    }

    @Test
    void testAddSearch() {
        String query = "서울 맛집";
        ResponseEntity<Void> response = searchController.addSearch(user, query);

        verify(searchService, times(1)).addSearchHistory(user, query);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetRecentSearchHistory() {
        List<SearchHistory> histories = Arrays.asList(
                new SearchHistory(user, "검색1"),
                new SearchHistory(user, "검색2")
        );

        when(searchService.getRecentSearchHistory(user)).thenReturn(histories);

        ResponseEntity<List<SearchHistory>> response = searchController.getRecentSearchHistory(user);

        assertEquals(2, response.getBody().size());
        assertEquals("검색1", response.getBody().get(0).getQuery());
    }
}
