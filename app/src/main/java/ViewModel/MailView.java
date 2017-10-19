package ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/9/5.
 */

public class MailView {
    public int current_page;
    public int last_page;
    public List<Mail> data;

    public class Mail {
        public int slId;
        public int lStatus;
        public String lSender;
        public String lSenderName;
        public String lRecipient;
        public String lRecipientName;
        public String lTitle;
        public String lContent;
        public String lNotes;
        public String created_at;
        public boolean read = true;
        public boolean favourite;
        public boolean expired;

        public boolean isDelete;
    }

    public MailView() {
        data = new ArrayList<>();
    }
}
