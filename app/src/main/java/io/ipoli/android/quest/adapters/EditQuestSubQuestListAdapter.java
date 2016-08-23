package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.subquests.DeleteSubQuestEvent;
import io.ipoli.android.quest.events.subquests.UpdateSubQuestNameEvent;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/28/16.
 */
public class EditQuestSubQuestListAdapter extends RecyclerView.Adapter<EditQuestSubQuestListAdapter.ViewHolder> {
    protected Context context;
    protected final Bus evenBus;
    protected List<SubQuest> subQuests;

    public EditQuestSubQuestListAdapter(Context context, Bus evenBus, List<SubQuest> subQuests) {
        this.context = context;
        this.evenBus = evenBus;
        this.subQuests = subQuests;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_quest_sub_quest_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SubQuest sq = subQuests.get(holder.getAdapterPosition());

        holder.delete.setOnClickListener(v -> {
            removeSubquest(holder.getAdapterPosition());
            evenBus.post(new DeleteSubQuestEvent(sq, EventSource.EDIT_QUEST));
        });

        holder.name.setText(sq.getName());

        hideUnderline(holder.name);
        holder.name.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                showUnderline(holder.name);
                holder.name.requestFocus();
            } else {
                hideUnderline(holder.name);
                evenBus.post(new UpdateSubQuestNameEvent(sq, EventSource.EDIT_QUEST));
            }
        });
        holder.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sq.setName(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void removeSubquest(int position) {
        subQuests.remove(position);
        notifyItemRemoved(position);
    }

    private void showUnderline(TextInputEditText editText) {
        editText.getBackground().clearColorFilter();
    }

    private void hideUnderline(TextInputEditText editText) {
        editText.getBackground().setColorFilter(ContextCompat.getColor(context, android.R.color.transparent), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return subQuests.size();
    }

    public void addSubQuest(SubQuest subQuest) {
        subQuests.add(subQuest);
        notifyItemInserted(subQuests.size() - 1);
    }

    public void setSubQuests(List<SubQuest> subQuests) {
        this.subQuests.clear();
        this.subQuests.addAll(subQuests);
        notifyDataSetChanged();
    }

    public List<SubQuest> getSubQuests() {
        return subQuests;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.sub_quest_name)
        TextInputEditText name;

        @BindView(R.id.sub_quest_delete)
        ImageButton delete;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
