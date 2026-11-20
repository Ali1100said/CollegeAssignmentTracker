package com.collegetracker.strategy;

import java.util.Comparator;

import com.collegetracker.AssignmentDao.AssignmentView;

final class AssignmentComparators {
    private AssignmentComparators() {}

    static Comparator<AssignmentView> dueDate() {
        return Comparator.comparing(AssignmentView::dueDate,
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    static Comparator<AssignmentView> title() {
        return Comparator.comparing(AssignmentView::title,
                String.CASE_INSENSITIVE_ORDER);
    }

    static Comparator<AssignmentView> stableTieBreakers() {
        return title().thenComparing(AssignmentView::id);
    }
}
