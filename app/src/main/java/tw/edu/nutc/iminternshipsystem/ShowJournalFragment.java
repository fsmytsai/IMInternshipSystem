package tw.edu.nutc.iminternshipsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ViewModel.JournalView;
import me.grantland.widget.AutofitTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowJournalFragment extends MySharedFragment {
    private JournalView.Journal journal;

    private TextView tv_StudentName;
    private TextView tv_StudentId;
    private TextView tv_CompanyName;
    private TextView tv_TeacherName;
    private TextView tv_InstructorName;
    private AutofitTextView atv_JournalStartTime;
    private AutofitTextView atv_JournalFinishTime;
    private TextView tv_JournalDetail1;
    private TextView tv_JournalDetail2;

    public ShowJournalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_journal, container, false);
        JournalActivity journalActivity = (JournalActivity) getActivity();
        this.journal = journalActivity.journal;
        initViews(view);
        return view;
    }

    private void initViews(final View view) {
        tv_StudentName = (TextView) view.findViewById(R.id.tv_StudentName);
        tv_StudentId = (TextView) view.findViewById(R.id.tv_StudentId);
        tv_CompanyName = (TextView) view.findViewById(R.id.tv_CompanyName);
        tv_TeacherName = (TextView) view.findViewById(R.id.tv_TeacherName);
        tv_InstructorName = (TextView) view.findViewById(R.id.tv_InstructorName);
        atv_JournalStartTime = (AutofitTextView) view.findViewById(R.id.atv_JournalStartTime);
        atv_JournalFinishTime = (AutofitTextView) view.findViewById(R.id.atv_JournalFinishTime);
        tv_JournalDetail1 = (TextView) view.findViewById(R.id.tv_JournalDetail1);
        tv_JournalDetail2 = (TextView) view.findViewById(R.id.tv_JournalDetail2);

        tv_StudentName.setText(journal.stuName);
        tv_StudentId.setText(journal.stuNum);
        tv_CompanyName.setText(journal.comName);
        tv_TeacherName.setText(journal.teaName);
        tv_InstructorName.setText(journal.journalInstructor);
        atv_JournalStartTime.setText(journal.journalStart);
        atv_JournalFinishTime.setText(journal.journalEnd);
        tv_JournalDetail1.setText(journal.journalDetail_1);
        tv_JournalDetail2.setText(journal.journalDetail_2);
    }

}
