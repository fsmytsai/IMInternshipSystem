package ViewModel;

import java.util.List;

/**
 * Created by user on 2017/8/19.
 */

public class ResumeView {
    //用於廠商審核履歷
    public int mid;
    public String jDuties;

    public BasicInfo stu_basic;
    public List<StudentAbility> stu_ability;
    public List<JobExperience> stu_jobExperience;
    public List<StudentWork> stu_works;

    public class BasicInfo{
        public int sid;
        public String chiName;
        public String engName;
        public String bornedPlace;
        public String birthday;
        public int gender;
        public String address;
        public String email;
        public String contact;
        public int ES;
        public int ER;
        public int EW;
        public int TOEIC;
        public int TOEFL;
        public String Oname;
        public int OS;
        public int OR;
        public int OW;
        public int eTypes;
        public int graduateYear;
        public String graduatedSchool;
        public String department;
        public String section;
        public String autobiography;
        public String profilePic;
    }

    public static class StudentAbility{
        public int abiid;
        public int sid;
        public int abiType;
        public String abiName;
    }

    public static class JobExperience{
        public int jid;
        public int sid;
        public String jobTitle;
        public String comName;
    }

    public static class StudentWork{
        public int wid;
        public int sid;
        public String wName;
        public String wLink;
        public String wCreatedDate;
    }
}
