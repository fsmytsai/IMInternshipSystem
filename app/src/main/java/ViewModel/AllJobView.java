package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/7/28.
 */

public class AllJobView {
    public int current_page;
    public int last_page;
    public List<AllJob> data;

    public class AllJob {
        public int joid;
        public String c_account;
        public String profilePic;
        public String c_name;
        public int jtypes;
        public String jduties;
        public String jdetails;
        public int jsalary_up;
        public int jsalary_low;
        public String jcontact_name;
        public String jcontact_phone;
        public String jcontact_email;
        public String jaddress;
        public String jStartDutyTime;
        public String jEndDutyTime;
        public String jdeadline;
        public int jNOP;
        public int jResume_num;
        public boolean jResume_submitted;
        public String created_at;

        public boolean isDetail;
    }

    public AllJobView() {
        data = new ArrayList<>();
    }
}
