package com.example.managerclassroom.models;



import android.content.Context;


import com.example.managerclassroom.R;

import java.util.ArrayList;
import java.util.List;

public class ListSubject {
    public static List<Subject> getSubjectList(Context context){
        List<Subject> subjectList = new ArrayList<>();

        Subject s1 = new Subject();
        s1.setSubject(context.getString(R.string.s1));
        subjectList.add(s1);

        Subject s2 = new Subject();
        s2.setSubject(context.getString(R.string.s2));
        subjectList.add(s2);

        Subject s3 = new Subject();
        s3.setSubject(context.getString(R.string.s3));
        subjectList.add(s3);

        Subject s4 = new Subject();
        s4.setSubject(context.getString(R.string.s4));
        subjectList.add(s4);

        Subject s5 = new Subject();
        s5.setSubject(context.getString(R.string.s5));
        subjectList.add(s5);

        Subject s6 = new Subject();
        s6.setSubject(context.getString(R.string.s6));
        subjectList.add(s6);

        Subject s7 = new Subject();
        s7.setSubject(context.getString(R.string.s7));
        subjectList.add(s7);

        Subject s8 = new Subject();
        s8.setSubject(context.getString(R.string.s8));
        subjectList.add(s8);

        Subject s9 = new Subject();
        s9.setSubject(context.getString(R.string.s9));
        subjectList.add(s9);

        Subject s10 = new Subject();
        s10.setSubject(context.getString(R.string.s10));
        subjectList.add(s10);

        return subjectList;
    }
}
