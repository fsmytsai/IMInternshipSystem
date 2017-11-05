package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/8/20.
 */

public class CompanyView {

    public List<Company> CompanyList;

    public class Company {
        public String c_account;
        public String ctypes;
        public String c_name;
        public String caddress;
        public String cfax;
        public String cintroduction;
        public int cempolyee_num;
        public String profilePic;
        public String introductionPic;
        public String tel;
    }

    public CompanyView() {
        CompanyList = new ArrayList<>();
    }
}
