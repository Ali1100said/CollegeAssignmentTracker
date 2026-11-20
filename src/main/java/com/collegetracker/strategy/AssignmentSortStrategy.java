package com.collegetracker.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.collegetracker.AssignmentDao.AssignmentView;

/** Strategy contract for ordering assignments without changing persistence. */
public interface AssignmentSortStrategy {
    String key();

    Comparator<AssignmentView> comparator();

    default List<AssignmentView> sort(List<AssignmentView> assignments) {
        List<AssignmentView> sorted = new ArrayList<>(assignments);
        sorted.sort(comparator());
        return sorted;
    }
}
