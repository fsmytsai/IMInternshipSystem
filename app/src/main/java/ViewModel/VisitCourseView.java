package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/8/29.
 */

public class VisitCourseView {
    public List<Course> CourseList;

    public class Course {
        public int courseId;
        public String courseName;
        public boolean passDeadLine;
        public List<Student> studentList;
        public boolean IsOpen;
        public boolean IsFill;

        public Course() {
            studentList = new ArrayList<>();
        }
    }

    public class Student {
        public int SCid;
        public int sid;
        public String stuName;
        public String stuAccount;
        public String profilePic;
    }

    public VisitCourseView() {
        CourseList = new ArrayList<>();
    }
}
