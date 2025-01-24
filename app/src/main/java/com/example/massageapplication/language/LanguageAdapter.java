package com.example.massageapplication.language;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.R;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private final List<LanguageModel> languages;
    private int selectedPosition = -1; // To track the selected position
    private final Context context; // To access resources like colors
    private final SetUpInterFace itemClickListener; // Interface for item click

    public LanguageAdapter(Context context, List<LanguageModel> languages, SetUpInterFace itemClickListener) {
        this.context = context;
        this.languages = languages;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_item, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageModel language = languages.get(position);

        holder.flagIcon.setImageResource(language.getFlagIcon());
        holder.languageName.setText(language.getLanguageName());
        holder.languageNativeName.setText(language.getNativeName());

        holder.radioButton.setChecked(position == selectedPosition);
        if (position == selectedPosition) {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.color_primary_selection_bg));
        } else {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.gray_selection_bg));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();

            // Trigger the callback
            itemClickListener.onLanguageItemClick(selectedPosition, language);
        });

        holder.radioButton.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();

            // Trigger the callback
            itemClickListener.onLanguageItemClick(selectedPosition, language);
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {

        ImageView flagIcon;
        TextView languageName, languageNativeName;
        RadioButton radioButton;
        LinearLayout mainLayout;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            flagIcon = itemView.findViewById(R.id.flagIcon);
            languageName = itemView.findViewById(R.id.languageName);
            languageNativeName = itemView.findViewById(R.id.languageNativeName);
            radioButton = itemView.findViewById(R.id.radioButton);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }

    public interface SetUpInterFace {
        void onLanguageItemClick(int position, LanguageModel languageModel);
    }
}
