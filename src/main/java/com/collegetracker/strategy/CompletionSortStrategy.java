package com.collegetracker.strategy;

import java.util.Comparator;

import com.collegetracker.AssignmentDao.AssignmentView;

/** Groups pending work before completed work, then orders each group by date. */
public final class CompletionSortStrategy implements AssignmentSortStrategy {
    @Override
    public String key() {
        return "completion";
    }

    @Override
    public Comparator<AssignmentView> comparator() {
        return Comparator.comparing(AssignmentView::completed)
                .thenComparing(AssignmentComparators.dueDate())
                .thenComparing(AssignmentComparators.stableTieBreakers());
    }
}
