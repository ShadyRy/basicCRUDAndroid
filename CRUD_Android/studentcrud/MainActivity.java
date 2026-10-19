package com.example.studentcrud;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // 10.0.2.2 = your computer's localhost, as seen from the Android emulator
    private static final String BASE_URL = "http://10.0.2.2/student_api/";

    private final List<Student> students = new ArrayList<>();
    private StudentAdapter adapter;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentAdapter(students, new StudentAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Student student) {
                showStudentDialog(student);
            }

            @Override
            public void onDelete(Student student) {
                confirmDelete(student);
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showStudentDialog(null));

        loadStudents();
    }

    // ---------- READ ----------
    private void loadStudents() {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, BASE_URL + "read.php", null,
                response -> {
                    students.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject o = response.getJSONObject(i);
                            students.add(new Student(
                                    o.getInt("id"),
                                    o.getString("name"),
                                    o.getString("course"),
                                    o.getInt("year_level")));
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this,
                        "Failed to load: " + error.getMessage(), Toast.LENGTH_LONG).show());
        queue.add(request);
    }

    // ---------- CREATE / UPDATE dialog ----------
    private void showStudentDialog(Student student) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_student, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etCourse = view.findViewById(R.id.etCourse);
        EditText etYear = view.findViewById(R.id.etYear);

        boolean isEdit = student != null;
        if (isEdit) {
            etName.setText(student.getName());
            etCourse.setText(student.getCourse());
            etYear.setText(String.valueOf(student.getYearLevel()));
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Student" : "Add Student")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String course = etCourse.getText().toString().trim();
                    String year = etYear.getText().toString().trim();

                    if (name.isEmpty() || course.isEmpty() || year.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    params.put("course", course);
                    params.put("year_level", year);

                    if (isEdit) {
                        params.put("id", String.valueOf(student.getId()));
                        sendRequest("update.php", params, "Student updated");
                    } else {
                        sendRequest("create.php", params, "Student added");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- DELETE ----------
    private void confirmDelete(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Delete " + student.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", String.valueOf(student.getId()));
                    sendRequest("delete.php", params, "Student deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- Shared POST helper ----------
    private void sendRequest(String file, Map<String, String> params, String successMsg) {
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL + file,
                response -> {
                    try {
                        boolean ok = new JSONObject(response).getBoolean("success");
                        Toast.makeText(this, ok ? successMsg : "Operation failed",
                                Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        String raw = response.length() > 200 ? response.substring(0, 200) : response;
                        Toast.makeText(this, "Server said: " + raw, Toast.LENGTH_LONG).show();
                    }
                    loadStudents();
                },
                error -> Toast.makeText(this,
                        "Request failed: " + error.getMessage(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        queue.add(request);
    }
}