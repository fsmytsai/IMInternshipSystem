package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/8/30.
 */

public class VisitStudentView {

    public String stuName;
    public String comTel;
    public String comName;
    public String comAddress;

    public List<Question> InterviewQ;

    public class Question {
        public int insQId;
        public String insQuestion;
        public int insAnswerType;
        public int insQuestionVer;
    }
}
