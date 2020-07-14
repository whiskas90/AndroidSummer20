package com.example.androidsummerproject20.activityToDoList.taskDetail;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.androidsummerproject20.R;
import com.example.androidsummerproject20.data.App;
import com.example.androidsummerproject20.models.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.Date;

public class TaskDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE = "NoteDetailsActivity.EXTRA_NOTE";

    //заметка
    private Task task;

    //текстовое поле для ввода
    private EditText editText;

    private CheckBox checkBox;

    private Calendar notificationTime = Calendar.getInstance();

    //метод для запуска активити
    public static void start(Activity caller, Task task) {
        Intent intent = new Intent(caller, TaskDetailsActivity.class);
        //если заметка существует
        if (task != null) {
            intent.putExtra(EXTRA_NOTE, task);
        }
        //запускаем активити через intent
        caller.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //приложение читает файл разметки и создаёт файл по шаблону, данному в скобках
        //далее с этим активити можно уже работать
        setContentView(R.layout.activity_note_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //добавляем кнопку назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Текст заголовка, который содержится в values/strings
        setTitle(getString(R.string.note_details_title));

        editText = findViewById(R.id.text);

        checkBox = findViewById(R.id.checkBox);
        checkBox.setText("Установить напоминание");

        Button button = findViewById(R.id.button);
        button.setText("Выбрать дату");

        //getIntent возвращает intent созданный при запуске активити
        //если заметка есть,то есть есть intent
        if (getIntent().hasExtra(EXTRA_NOTE)) {
            //за счёт интерфейса Parcelable достаём заметку
            task = getIntent().getParcelableExtra(EXTRA_NOTE);
            //изменяем(задаём) текст заметки
            editText.setText(task.text);
        } else {
            //если заметки нет, то создаём новую заметку
            task = new Task();
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.note:

                        break;
                    case R.id.todo:
                        break;
                    case R.id.trash:
                        Toast.makeText(TaskDetailsActivity.this, "Trash", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    //для кнопки сохранения в меню
    //создание меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //обработка для событий при нажатии на кнопку сохранения
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //если пользователь нажал на кнопку "назад", то просто завершить работу
            case android.R.id.home:
                finish();
                break;
            //если пользователь нажал на кнопку "сохранения"
            case R.id.action_save:
                //если пользователь что-то ввёл в заметку
                if (editText.getText().length() > 0) {
                    //сохраняем текстом то, что ввёл пользователь
                    task.text = editText.getText().toString();
                    //дело "не завершено"
                    task.active = false;
                    //время создания
                    task.time = System.currentTimeMillis();
                    //если какая-то заметка передавалась в активити, то обновляем её
                    if (getIntent().hasExtra(EXTRA_NOTE)) {
                        App.getInstance().getTaskDao().update(task);
                    }
                    //если заметка новая, то добавляем её в список
                    else {
                        App.getInstance().getTaskDao().insert(task);
                    }
                    //происходит обновление базы данных, и автоматически livedata всё изменит,
                    //поэтому обратно передавать ничего не нужно
                    //все изменения покажутся на экране
                    if (checkBox.isChecked()) {

                        long time = notificationTime.getTimeInMillis();

                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Напоминане установлено на: \n" + new Date(time).toString(),
                                Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        scheduleNotification(getNotification(
                                task.text.substring(0, Math.min(task.text.length(), 40))), time);
                    }
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTime(View v) {
        TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                notificationTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                notificationTime.set(Calendar.MINUTE, minute);
                notificationTime.set(Calendar.SECOND, 0);
            }
        };

        new TimePickerDialog(this, time,
                notificationTime.get(Calendar.HOUR_OF_DAY),
                notificationTime.get(Calendar.MINUTE), true)
                .show();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                notificationTime.set(Calendar.YEAR, year);
                notificationTime.set(Calendar.MONTH, monthOfYear);
                notificationTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        };

        new DatePickerDialog(this, date,
                notificationTime.get(Calendar.YEAR),
                notificationTime.get(Calendar.MONTH),
                notificationTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void scheduleNotification(Notification notification, long delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class)
                .putExtra(NotificationPublisher.NOTIFICATION_ID, 101)
                .putExtra(NotificationPublisher.NOTIFICATION, notification);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 0,
                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
    }

    private Notification getNotification(String content) {
        return new Notification.Builder(this)
                .setContentTitle("Notification")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build();
    }
}