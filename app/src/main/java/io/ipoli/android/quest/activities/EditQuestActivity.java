package io.ipoli.android.quest.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DateSelectedEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestedEvent;
import io.ipoli.android.quest.events.QuestContextUpdatedEvent;
import io.ipoli.android.quest.events.QuestDurationUpdatedEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.TimeSelectedEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.events.UpdateQuestEndDateRequestEvent;
import io.ipoli.android.quest.events.UpdateQuestStartTimeRequestEvent;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.dialogs.TimePickerFragment;
import io.ipoli.android.quest.ui.formatters.DueDateFormatter;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

public class EditQuestActivity extends BaseActivity {

    @Bind(R.id.edit_quest_container)
    CoordinatorLayout rootContainer;

    @Bind(R.id.appbar)
    AppBarLayout appBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quest_text)
    EditText nameText;

    @Bind(R.id.quest_duration)
    Spinner questDuration;

    @Bind(R.id.quest_due_date)
    Button dueDateBtn;

    @Bind(R.id.quest_start_time)
    Button startTimeBtn;

    @Bind(R.id.quest_context_name)
    TextView contextName;

    @Bind(R.id.quest_context_container)
    LinearLayout contextContainer;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    private Quest quest;
    private DurationMatcher durationMatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quest);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        appComponent().inject(this);
        durationMatcher = new DurationMatcher();

        String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId).subscribe(q -> {
            quest = q;
            initUI();
        });

        eventBus.post(new ScreenShownEvent("edit_quest"));
    }

    private void initUI() {
        nameText.setText(quest.getName());
        nameText.setSelection(nameText.getText().length());

        List<String> durationSuggestions = new ArrayList<>();

        String qDurationTxt = DurationFormatter.formatReadable(quest.getDuration());
        if (!TextUtils.isEmpty(qDurationTxt)) {
            durationSuggestions.add(qDurationTxt);
        }
        durationSuggestions.addAll(createAutoSuggestions(qDurationTxt));
        questDuration.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, durationSuggestions));
        questDuration.setSelection(0, false);
        questDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eventBus.post(new QuestDurationUpdatedEvent(quest, questDuration.getSelectedItem().toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setStartTimeText(Quest.getStartTime(quest));
        setDueDateText(DateUtils.UTCToLocalDate(quest.getEndDate()));

        initContextUI();
    }

    @NonNull
    private List<String> createAutoSuggestions(String qDurationTxt) {
        String[] questDurations = getResources().getStringArray(R.array.quest_durations);
        List<String> autoSuggestions = new ArrayList<>(Arrays.asList(questDurations));

        if (TextUtils.isEmpty(qDurationTxt)) {
            return autoSuggestions;
        }

        Iterator<String> it = autoSuggestions.iterator();
        while (it.hasNext()) {
            if (it.next().equals(qDurationTxt)) {
                it.remove();
            }
        }
        return autoSuggestions;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_quest_menu, menu);
        return true;
    }

    private void initContextUI() {
        changeContext(Quest.getContext(quest));

        final QuestContext[] ctxs = QuestContext.values();
        for (int i = 0; i < contextContainer.getChildCount(); i++) {
            final ImageView iv = (ImageView) contextContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) iv.getBackground();
            drawable.setColor(ContextCompat.getColor(this, ctxs[i].resLightColor));

            final QuestContext ctx = ctxs[i];
            iv.setOnClickListener(v -> {
                removeSelectedContextCheck();
                changeContext(ctx);
                eventBus.post(new QuestContextUpdatedEvent(quest, ctx));
            });
        }
    }

    private void changeContext(QuestContext ctx) {
        setBackgroundColors(ctx);
        Quest.setContext(quest, ctx);
        setSelectedContext();
    }

    private void setSelectedContext() {
        getCurrentContextImageView().setImageResource(Quest.getContext(quest).whiteImage);
        setContextName();
    }

    private void removeSelectedContextCheck() {
        getCurrentContextImageView().setImageDrawable(null);
    }

    private ImageView getCurrentContextImageView() {
        String ctxId = "quest_context_" + quest.getContext().toLowerCase();
        int ctxResId = getResources().getIdentifier(ctxId, "id", getPackageName());
        return (ImageView) findViewById(ctxResId);
    }

    private void setBackgroundColors(QuestContext ctx) {
        appBar.setBackgroundColor(ContextCompat.getColor(this, ctx.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, ctx.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, ctx.resDarkColor));
    }

    private void setContextName() {
        contextName.setText(quest.getContext().substring(0, 1) + quest.getContext().substring(1).toLowerCase());
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackButton();
                finish();
                return true;
            case R.id.action_remove:
                eventBus.post(new DeleteQuestRequestedEvent(quest, "edit_quest"));
                AlertDialog d = new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_remove_quest_title)).setMessage(getString(R.string.dialog_remove_quest_message)).create();
                d.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.remove_it), (dialogInterface, i) -> {
                    questPersistenceService.delete(quest);
                    Toast.makeText(EditQuestActivity.this, R.string.quest_removed, Toast.LENGTH_SHORT).show();
                    setResult(Constants.RESULT_REMOVED);
                    finish();
                });
                d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> {
                    eventBus.post(new UndoDeleteQuestEvent(quest, "edit_quest"));
                });
                d.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButton() {
        saveQuest();
        Intent data = new Intent();
        data.putExtras(getIntent());
        setResult(RESULT_OK, data);
    }

    @OnClick(R.id.quest_due_date)
    public void onDueDateClick(Button button) {
        eventBus.post(new UpdateQuestEndDateRequestEvent(quest));
        Date dueDate = (Date) dueDateBtn.getTag();
        Calendar c = Calendar.getInstance();
        c.setTime(dueDate);
        DialogFragment f = DatePickerFragment.newInstance(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        f.show(this.getSupportFragmentManager(), "datePicker");
    }

    @OnClick(R.id.quest_start_time)
    public void onStartTimeClick(Button button) {
        eventBus.post(new UpdateQuestStartTimeRequestEvent(quest));
        DialogFragment f = new TimePickerFragment();
        f.show(this.getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onBackPressed() {
        onBackButton();
        super.onBackPressed();
    }

    private void saveQuest() {
        String name = nameText.getText().toString().trim();
        int duration = durationMatcher.parseShort(questDuration.getSelectedItem().toString());
        quest.setName(name);
        quest.setDuration(duration);
        quest.setEndDate((Date) dueDateBtn.getTag());
        Quest.setStartTime(quest, ((Time) startTimeBtn.getTag()));
        questPersistenceService.save(quest);
        eventBus.post(new QuestUpdatedEvent(quest));
    }

    @Subscribe
    public void onDueDateSelected(DateSelectedEvent e) {
        setDueDateText(e.date);
    }

    @Subscribe
    public void onStartTimeSelected(TimeSelectedEvent e) {
        setStartTimeText(e.time);
    }

    private void setDueDateText(Date date) {
        String text = "";
        if (date == null) {
            text = getString(R.string.due_date_default);
        } else {
            text = DateUtils.isToday(date) ? getString(R.string.today) : DueDateFormatter.format(date);
        }
        dueDateBtn.setText(text);
        dueDateBtn.setTag(date);
    }

    private void setStartTimeText(Time time) {
        if (time == null) {
            startTimeBtn.setText(R.string.start_time_default);
        } else {
            startTimeBtn.setText(StartTimeFormatter.format(time.toDate()));
        }
        startTimeBtn.setTag(time);
    }
}
