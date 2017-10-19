package ViewModel;

import java.util.List;

/**
 * Created by user on 2017/8/30.
 */

public class ExistVisitView {
    public List<ExistVisitCompany> InterviewComList;
    public List<ExistVisitStudent> InterviewStuList;
    public class ExistVisitCompany{
        public int insCId;
        public int SCid;
        public String insCDate;
        public int insCNum;
        public int insCVisitWay;
        public String insCAns;
        public int insCQuestionVer;
        public String insCComments;

        public String stuName;
        public String stuNum;
        public String comTel;
        public String comName;
        public String cAddress;
        public String profilePic;
        public List<VisitCompanyView.Question> questions;
    }
    public class ExistVisitStudent{
        public int insId;
        public int SCid;
        public String insDate;
        public int insNum;
        public String insStuClass;
        public int insVisitWay;
        public String insAns;
        public int insQuestionVer;
        public String insComments;

        public String stuName;
        public String stuNum;
        public String comTel;
        public String comName;
        public String cAddress;
        public String profilePic;
        public List<VisitStudentView.Question> questions;
    }
}
