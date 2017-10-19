package tw.edu.nutc.iminternshipsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ViewModel.JournalView;


/**
 * A simple {@link Fragment} subclass.
 */
public class JournalGradeFragment extends Fragment {
    private JournalView.Journal journal;

    private TextView tv_InstructorComment;
    private TextView tv_InstructorGrade;
    private TextView tv_InstructorGradeDate;
    private TextView tv_TeacherComment;
    private TextView tv_TeacherGrade;
    private TextView tv_TeacherGradeDate;

    public JournalGradeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_journal_grade, container, false);
        JournalActivity journalActivity = (JournalActivity) getActivity();
        this.journal = journalActivity.journal;
        initViews(view);
        return view;
    }

    private void initViews(final View view) {
        tv_InstructorComment = (TextView) view.findViewById(R.id.tv_InstructorComment);
        tv_InstructorGrade = (TextView) view.findViewById(R.id.tv_InstructorGrade);
        tv_InstructorGradeDate = (TextView) view.findViewById(R.id.tv_InstructorGradeDate);
        tv_TeacherComment = (TextView) view.findViewById(R.id.tv_TeacherComment);
        tv_TeacherGrade = (TextView) view.findViewById(R.id.tv_TeacherGrade);
        tv_TeacherGradeDate = (TextView) view.findViewById(R.id.tv_TeacherGradeDate);

        tv_InstructorComment.setText(journal.journalComments_ins);
        tv_InstructorGrade.setText(journal.grade_ins + "");
        tv_InstructorGradeDate.setText(journal.scoredTime_com);
        tv_TeacherComment.setText(journal.journalComments_teacher);
        tv_TeacherGrade.setText(journal.grade_teacher + "");
        tv_TeacherGradeDate.setText(journal.scoredTime_tea);
    }
}
