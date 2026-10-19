package com.example.studentcrud;

public class Student {
    private int id;
    private String name;
    private String course;
    private int yearLevel;

    public Student(int id, String name, String course, int yearLevel) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.yearLevel = yearLevel;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCourse() {
        return course;
    }

    public int getYearLevel() {
        return yearLevel;
    }
}
