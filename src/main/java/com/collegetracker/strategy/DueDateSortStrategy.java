package com.collegetracker.strategy;

import java.util.Comparator;

import com.collegetracker.AssignmentDao.AssignmentView;

/** Orders dated work first, with undated assignments at the end. */
public final class DueDateSortStrategy implements AssignmentSortStrategy {
    @Override
    public String key() {
        return "dueDate";
    }

    @Override
    public Comparator<AssignmentView> comparator() {
        return AssignmentComparators.dueDate()
                .thenComparing(AssignmentComparators.stableTieBreakers());
    }
}
