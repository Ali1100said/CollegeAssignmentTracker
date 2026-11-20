package com.collegetracker.strategy;

import java.util.Comparator;

import com.collegetracker.AssignmentDao.AssignmentView;

/** Orders titles case-insensitively, then resolves ties predictably. */
public final class TitleSortStrategy implements AssignmentSortStrategy {
    @Override
    public String key() {
        return "title";
    }

    @Override
    public Comparator<AssignmentView> comparator() {
        return AssignmentComparators.title()
                .thenComparing(AssignmentComparators.dueDate())
                .thenComparing(AssignmentView::id);
    }
}
