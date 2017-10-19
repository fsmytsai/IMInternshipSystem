package ViewModel;

import java.util.List;

/**
 * Created by user on 2017/8/30.
 */

public class VisitCompanyView {
    public String stuName;
    public String comTel;
    public String comName;
    public String comAddress;

    public List<Question> InterviewQ;

    public class Question {
        public int insCQId;
        public String insCQuestion;
        public int insCAnswerType;
        public int insCQuestionVer;
    }
}
