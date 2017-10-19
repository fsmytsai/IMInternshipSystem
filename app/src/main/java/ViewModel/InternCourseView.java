package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/8/21.
 */

public class InternCourseView {
    public List<InternCourse> intern_list;

    public class InternCourse {
        public int SCid;
        public String com_name;
        public String courseName;
        public String profilePic;
        public String passDeadLine;

        public boolean IsFill;
        public boolean IsOpen;
    }

    public InternCourseView() {
        intern_list = new ArrayList<>();
    }
}
