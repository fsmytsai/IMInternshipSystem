package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/7/26.
 */

public class MyJobView {
    public int current_page;
    public int last_page;
    public List<MyJob> data;

    public class MyJob {
        public int joid;
        public String c_account;
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

        public boolean isDetail;
    }

    public MyJobView() {
        data = new ArrayList<>();
    }
}
