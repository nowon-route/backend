package com.example.hackathonbackend.store;

import com.example.hackathonbackend.dto.ItineraryResponse;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryItineraryStore {

    private static class Entry {
        final ItineraryResponse itinerary;
        final Instant createdAt;
        Entry(ItineraryResponse it) {
            this.itinerary = it;
            this.createdAt = Instant.now();
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public String save(ItineraryResponse itinerary) {
        String id = UUID.randomUUID().toString();
        store.put(id, new Entry(itinerary));
        return id;
    }

    public Optional<ItineraryResponse> find(String id) {
        Entry e = store.get(id);
        return Optional.ofNullable(e == null ? null : e.itinerary);
    }

    public void clearOld(long seconds) {
        Instant cut = Instant.now().minusSeconds(seconds);
        store.entrySet().removeIf(en -> en.getValue().createdAt.isBefore(cut));
    }
}
