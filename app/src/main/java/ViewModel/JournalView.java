package ViewModel;

import java.util.List;

/**
 * Created by user on 2017/8/21.
 */

public class JournalView {
    public List<Journal> journalList;
    public Review reviews;
    public InternProposal internProposal;

    public class Journal {
        public int journalID;
        public int SCid;
        public int journalOrder;
        public String journalDetail_1;//實習內容
        public String journalDetail_2;//觀察心得與個人看法
        public String journalStart;
        public String journalEnd;
        public String journalInstructor;
        public String journalComments_ins;
        public String journalComments_teacher;
        public int grade_ins;
        public int grade_teacher;
        public String scoredTime_com;
        public String scoredTime_tea;
        public String stuName;
        public String stuNum;
        public String comName;
        public String teaName;
        public String passDeadLine;
    }

    public class Review {
        public int reId;
        public int SCid;
        public boolean reRead;
        public String reContent;
        public String googleForm;
        public String created_at;
        public String updated_at;
    }

    public class InternProposal {
        public int IPId;
        public int SCid;
        public String IPStart;
        public int IPRead;
    }
}
