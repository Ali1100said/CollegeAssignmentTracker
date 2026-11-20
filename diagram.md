# College Assignment Tracker — UML Class Diagram

```mermaid
classDiagram
    class Student {
        +int id
        +String username
        +String passwordHash
    }

    class Course {
        +int id
        +int studentId
        +String courseName
    }

    class Assignment {
        +int id
        +int courseId
        +String title
        +Date dueDate
        +boolean completed
    }

    class DashboardFacade {
        +loadDashboard(studentId, sortKey) DashboardData
    }

    class AssignmentSortStrategy {
        <<interface>>
        +key() String
        +sort(assignments) List
    }

    class DueDateSortStrategy
    class TitleSortStrategy
    class CompletionSortStrategy

    Student "1" --> "0..*" Course : owns
    Course "1" --> "0..*" Assignment : contains
    DashboardFacade --> Student : loads profile
    DashboardFacade --> Course : loads courses
    DashboardFacade --> Assignment : loads assignments
    DashboardFacade --> AssignmentSortStrategy : selects
    AssignmentSortStrategy <|.. DueDateSortStrategy
    AssignmentSortStrategy <|.. TitleSortStrategy
    AssignmentSortStrategy <|.. CompletionSortStrategy
```
