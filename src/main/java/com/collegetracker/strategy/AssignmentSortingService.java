package com.collegetracker.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.collegetracker.AssignmentDao.AssignmentView;

/** Context that selects and executes an assignment sorting strategy. */
public final class AssignmentSortingService {
    public static final String DEFAULT_SORT = "dueDate";

    private final Map<String, AssignmentSortStrategy> strategies;

    public AssignmentSortingService() {
        this(List.of(new DueDateSortStrategy(), new TitleSortStrategy(),
                new CompletionSortStrategy()));
    }

    public AssignmentSortingService(List<AssignmentSortStrategy> strategies) {
        Map<String, AssignmentSortStrategy> byKey = new LinkedHashMap<>();
        for (AssignmentSortStrategy strategy : strategies) {
            byKey.put(strategy.key(), strategy);
        }
        this.strategies = Map.copyOf(byKey);
    }

    public SortResult sort(List<AssignmentView> assignments, String requestedKey) {
        String normalizedKey = requestedKey == null || requestedKey.isBlank()
                ? DEFAULT_SORT : requestedKey;
        AssignmentSortStrategy strategy = strategies.getOrDefault(
                normalizedKey, strategies.get(DEFAULT_SORT));
        if (strategy == null) {
            throw new IllegalStateException("The default sorting strategy is missing.");
        }
        return new SortResult(strategy.key(), strategy.sort(assignments));
    }

    public record SortResult(String key, List<AssignmentView> assignments) {}
}
