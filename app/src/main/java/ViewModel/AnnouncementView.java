package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/8/22.
 */

public class AnnouncementView {
    public int current_page;
    public int last_page;
    public List<Announcement> data;

    public class Announcement {
        public int anId;
        public String anTittle;
        public String anContent;
        public String anFile;
        public String created_at;
        public String updated_at;
    }

    public AnnouncementView() {
        data = new ArrayList<>();
    }
}
