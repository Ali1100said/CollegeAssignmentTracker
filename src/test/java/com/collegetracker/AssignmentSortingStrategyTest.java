package com.collegetracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.collegetracker.AssignmentDao.AssignmentView;
import com.collegetracker.strategy.AssignmentSortingService;

class AssignmentSortingStrategyTest {
    private final AssignmentSortingService sorting = new AssignmentSortingService();

    @Test
    void dueDateSortPlacesNullDatesLastAndUsesTitleForTies() {
        List<AssignmentView> result = sorting.sort(List.of(
                assignment(1, "Undated", null, false),
                assignment(2, "Zulu", "2026-12-01", false),
                assignment(3, "alpha", "2026-12-01", true),
                assignment(4, "Earlier", "2026-11-20", false)),
                "dueDate").assignments();

        assertEquals(List.of(4, 3, 2, 1), ids(result));
    }

    @Test
    void titleSortIsCaseInsensitiveAndUsesDateThenIdForTies() {
        List<AssignmentView> result = sorting.sort(List.of(
                assignment(3, "Beta", null, false),
                assignment(2, "alpha", "2026-12-02", false),
                assignment(1, "Alpha", "2026-12-01", true)),
                "title").assignments();

        assertEquals(List.of(1, 2, 3), ids(result));
    }

    @Test
    void completionSortKeepsPendingBeforeCompleted() {
        List<AssignmentView> result = sorting.sort(List.of(
                assignment(1, "Done", "2026-11-20", true),
                assignment(2, "Pending", "2026-12-01", false)),
                "completion").assignments();

        assertEquals(List.of(2, 1), ids(result));
    }

    @Test
    void unknownSortKeyFallsBackToDueDate() {
        var result = sorting.sort(List.of(
                assignment(1, "Later", "2026-12-01", false),
                assignment(2, "Earlier", "2026-11-20", false)), "invalid");

        assertEquals("dueDate", result.key());
        assertEquals(List.of(2, 1), ids(result.assignments()));
    }

    @Test
    void missingSortKeyUsesDefaultStrategy() {
        var result = sorting.sort(List.of(
                assignment(1, "Undated", null, false)), null);

        assertEquals("dueDate", result.key());
        assertEquals(List.of(1), ids(result.assignments()));
    }

    private static AssignmentView assignment(int id, String title,
            String date, boolean completed) {
        return new AssignmentView(id, 1, title,
                date == null ? null : Date.valueOf(date), completed, "Course");
    }

    private static List<Integer> ids(List<AssignmentView> assignments) {
        return assignments.stream().map(AssignmentView::id).toList();
    }
}
