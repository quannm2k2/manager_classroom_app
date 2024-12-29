package com.example.managerclassroom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.managerclassroom.R;
import com.example.managerclassroom.models.Subject;

import java.util.List;

public class SubjectAdapter extends BaseAdapter {
    private Context context;
    private List<Subject> subjectList;

    public SubjectAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    @Override
    public int getCount() {
        return subjectList!=null ? subjectList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return subjectList != null ? subjectList.get(position).getSubject() : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.subject_item, viewGroup, false);

        TextView txtSubjectItem = rootView.findViewById(R.id.txtSubjectItem);
        txtSubjectItem.setText(subjectList.get(position).getSubject());

        return rootView;
    }
}
