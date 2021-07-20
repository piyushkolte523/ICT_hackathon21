package com.example.chronology;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Objects;

public class ReminderFragment extends Fragment {
    FloatingActionButton addTaskButton;
    RecyclerView recyclerView;
    ProgressDialog loader;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String uID;
    String key="";
    String task;
    String description;
    String time;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reminder_fragment, container, false);

        mAuth=FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String Date = dayOfMonth + "-" + (month + 1) + "-" + year;
            }
        });

        loader=new ProgressDialog(getContext());

        mUser=mAuth.getCurrentUser();
        uID = mUser.getUid();
        reference= FirebaseDatabase.getInstance("https://chronology-88080-default-rtdb.firebaseio.com/").getReference().child(uID).child("reminderTasks");

        addTaskButton = view.findViewById(R.id.addTaskButton);
        addTaskButton.bringToFront();
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseTask();
            }
        });

        return view;
    }

    private void chooseTask() {
        String[] taskTypeList = {"Medicinal Task", "Other Task"};
        AlertDialog.Builder taskDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater =LayoutInflater.from(getContext());

        View createTaskView =inflater.inflate(R.layout.create_task,null);
        taskDialog.setView(createTaskView);

        final AlertDialog dialog = taskDialog.create();
        dialog.setCancelable(false);
        final Spinner taskType = createTaskView.findViewById(R.id.taskType);
        Button nextButton =createTaskView.findViewById(R.id.nextButton);
        Button cancelButton =createTaskView.findViewById(R.id.cancelButton);

        ArrayAdapter<String> taskAdapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, taskTypeList);
        taskType.setAdapter(taskAdapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskType.getSelectedItem().toString().equals("Medicinal Task")) {
                    createMedTask();
                } else if (taskType.getSelectedItem().toString().equals("Other Task")) {
                    createOtherTask();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //-----------------------------------Medicinal Task--------------------------------
    private void createMedTask() {
        AlertDialog.Builder medTaskDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater =LayoutInflater.from(getContext());

        View createMedTaskView =inflater.inflate(R.layout.medicinal_task,null);
        medTaskDialog.setView(createMedTaskView);

        final AlertDialog dialog = medTaskDialog.create();
        dialog.setCancelable(false);

        final EditText medEditText = createMedTaskView.findViewById(R.id.medicineEditText);
        final EditText descriptionEditText = createMedTaskView.findViewById(R.id.descriptionEditText);

        final EditText timeEditText = createMedTaskView.findViewById(R.id.timeEditText);
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCurrentTime = Calendar.getInstance();
                int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mCurrentTime.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if(minute >=0 && minute <=9) {
                            timeEditText.setText(hourOfDay + ":0" + minute);
                        } else {
                            timeEditText.setText(hourOfDay + ":" + minute);
                        }
                    }
                } , hour, minute, true);

                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        Button previousButton = createMedTaskView.findViewById(R.id.previousButton);
        Button saveButton = createMedTaskView.findViewById(R.id.saveButton);

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                chooseTask();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String medName = medEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String time = timeEditText.getText().toString().trim();
                String id = reference.push().getKey();

                if (TextUtils.isEmpty(medName)) {
                    medEditText.setError("Medicine Name Required");
                } else if (TextUtils.isEmpty(description)) {
                    descriptionEditText.setError("Medicine Description Required");
                } else if (TextUtils.isEmpty(time)) {
                    timeEditText.setError("Time to take medicine Required");
                } else {
                    Log.i("OUTPUT", medName + "---" + description + "---" + time);
                    loader.setMessage("Saving Data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    Tasks tasks = new Tasks(medName, description, time);
                    reference.child(id).setValue(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Task is scheduled", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(getContext(), "Failed: "+error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //----------------------------------------------Other Task------------------------------------
    private void createOtherTask() {
        AlertDialog.Builder otherTaskDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater =LayoutInflater.from(getContext());

        View otherTaskView =inflater.inflate(R.layout.other_tasks,null);
        otherTaskDialog.setView(otherTaskView);

        final AlertDialog dialog = otherTaskDialog.create();
        dialog.setCancelable(false);

        final EditText otherTaskEditText = otherTaskView.findViewById(R.id.otherTaskEditText);
        final EditText otherDescEditText = otherTaskView.findViewById(R.id.otherDescEditText);

        final EditText otherTimeEditText = otherTaskView.findViewById(R.id.otherTimeEditText);
        otherTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCurrentTime = Calendar.getInstance();
                int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mCurrentTime.get(Calendar.MINUTE);

                TimePickerDialog  timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if(minute >=0 && minute <=9) {
                            otherTimeEditText.setText(hourOfDay + ":0" + minute);
                        } else {
                            otherTimeEditText.setText(hourOfDay + ":" + minute);
                        }
                    }
                }, hour, minute, true);

                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        Button previousButton = otherTaskView.findViewById(R.id.prevBtn);
        Button saveButton = otherTaskView.findViewById(R.id.saveBtn);

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                chooseTask();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = otherTaskEditText.getText().toString();
                String description = otherDescEditText.getText().toString().trim();
                String time = otherTimeEditText.getText().toString();
                String id = reference.push().getKey();

                if (TextUtils.isEmpty(taskName)) {
                    otherTaskEditText.setError("Medicine Name Required");
                } else if (TextUtils.isEmpty(description)) {
                    otherDescEditText.setError("Medicine Description Required");
                }else if (TextUtils.isEmpty(time)) {
                    otherTimeEditText.setError("Time to take medicine Required");
                } else {
                    Log.i("OUTPUT", taskName + "---" + time);
                    loader.setMessage("Saving Data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    Tasks tasks = new Tasks(taskName, description, time);
                    reference.child(id).setValue(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Task is scheduled", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(getContext(), "Failed: "+error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Tasks> options = new FirebaseRecyclerOptions.Builder<Tasks>().setQuery(reference, Tasks.class).build();
        FirebaseRecyclerAdapter<Tasks, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Tasks, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Tasks model) {
                holder.setTask(model.getTaskName());
                holder.setDescription(model.getTaskDescriptionName());
                holder.setTime(model.getTaskTimeName());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        time = model.getTaskTimeName();
                        task = model.getTaskName();
                        description = model.getTaskDescriptionName();
                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieved_layout, parent, false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        public void setTask(String task) {
            TextView taskTextView = myView.findViewById(R.id.taskT);
            taskTextView.setText(task);
        }

        public void setDescription(String description) {
            TextView descTextView = myView.findViewById(R.id.descT);
            descTextView.setText(description);
        }

        public void setTime(String time) {
            TextView timeTextView = myView.findViewById(R.id.timeT);
            timeTextView.setText(time);
        }
    }

    private  void updateTask(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View updateTaskView = inflater.inflate(R.layout.update_task, null);
        myDialog.setView(updateTaskView);

        final AlertDialog dialog = myDialog.create();

        final EditText updateTaskText = updateTaskView.findViewById(R.id.updateTask);
        final EditText updateDescription = updateTaskView.findViewById(R.id.updateDescription);
        final EditText updateTime = updateTaskView.findViewById(R.id.updateTime);

        updateTaskText.setText(task);
        updateDescription.setText(description);
        updateTime.setText(time);

        final String[] taskTime = time.split(":");

        updateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mCurrentTime = Calendar.getInstance();
                int hour = Integer.parseInt(taskTime[0]);
                int minute = Integer.parseInt(taskTime[1]);

                TimePickerDialog  timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if(minute >=0 && minute <=9) {
                            updateTime.setText(hourOfDay + ":0" + minute);
                        } else {
                            updateTime.setText(hourOfDay + ":" + minute);
                        }
                    }
                }, hour, minute, true);

                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        Button deleteButton = updateTaskView.findViewById(R.id.deleteButton);
        Button updateButton = updateTaskView.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = updateTaskText.getText().toString().trim();
                description = updateDescription.getText().toString().trim();
                time = updateTime.getText().toString().trim();

                Tasks tasks = new Tasks(task, description, time);
                reference.child(key).setValue(tasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Task has been updated successfully", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String err=task.getException().toString();
                            Toast.makeText(getContext(), "update failed"+err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String err=task.getException().toString();
                            Toast.makeText(getContext(), "Failed to delete task"+err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
