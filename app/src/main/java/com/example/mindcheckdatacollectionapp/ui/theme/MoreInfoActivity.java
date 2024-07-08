package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mindcheckdatacollectionapp.R;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.annotations.NonNull;

public class MoreInfoActivity extends AppCompatActivity {

    private static class InstructionItem {
        private String itemText;
        private ArrayList<String> subItems;

        public InstructionItem(String itemText, ArrayList<String> subItems) {
            this.itemText = itemText;
            this.subItems = subItems;
        }

        public String getItemText() {
            return itemText;
        }

        public ArrayList<String> getSubItems() {
            return subItems;
        }
    }
    private static class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.InstructionViewHolder> {
        private Context context;
        private ArrayList<InstructionItem> items;
        public InstructionAdapter(Context context, ArrayList<InstructionItem> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_instruction, parent, false);
            return new InstructionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {
            InstructionItem item = items.get(position);
            holder.itemText.setText(item.getItemText());
            holder.subItemsLayout.removeAllViews();
            for (String subItem : item.getSubItems()) {
                TextView textView = new TextView(context);
                textView.setText(subItem);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                holder.subItemsLayout.addView(textView);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class InstructionViewHolder extends RecyclerView.ViewHolder {
            TextView itemText;
            LinearLayout subItemsLayout;

            public InstructionViewHolder(@NonNull View itemView) {
                super(itemView);
                itemText = itemView.findViewById(R.id.instruction);
                subItemsLayout = itemView.findViewById(R.id.steps);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);
        Log.d("DEBUG", "onCreate: Initializing instruction items");

        ArrayList<InstructionItem> instructionItems = new ArrayList<>();
        //Add instructions here
        instructionItems.add(new InstructionItem("Setting Up The Keyboard",
                new ArrayList<>(Arrays.asList("1. Go to Settings.\n",
                        "2. Go to System -> Languages & Input.\n",
                        "3. Go to Input Methods / On-Screen Keyboards, and select MindCheckDC as the default keyboard.\n",
                        "4. Agree to data collection. (Don't worry, we do not collect text data, refer below to see what data we collect.)",
                        "5. Use the keyboard like your normal keyboard! (You can use it inside or outside the app)"))));
        instructionItems.add(new InstructionItem("Changing The Keyboard",
                new ArrayList<>(Arrays.asList("In case you want to temporarily change to the system keyboard:\n",
                        "1. Open the keyboard.\n",
                        "2. Click the keyboard icon on the bottom-right corner of your screen.\n",
                        "3. Select the keyboard.\n",
                        "4. Remember to switch back to the MindCheckDC keyboard after use."))));
        instructionItems.add(new InstructionItem("Features",
                new ArrayList<>(Arrays.asList("This application is a pre-released version of a final product with the purpose of collecting typing data from subjects to train a model to predict depression occurrence, some features are not available in this version.\n",
                        "Features available now include: \n",
                        "1. Custom Keyboard (to record typing data)\n",
                        "2. Notifications (to remind users to record depression data)\n",
                        "3. Journal feature for users to type in-app\n"))));
        instructionItems.add(new InstructionItem("Data We Collect",
                new ArrayList<>(Arrays.asList("The data we collect consists of:\n",
                        "1. Time related data (key press time, key release time)\n",
                        "2. Relative coordinates data (Normalized coordinates of the key relative to the keyboard size)\n",
                        "The data is aggregated using second, third, and fourth, order statistics after each typing session, and only these statistics will be collected and stored.\n",
                        "Rest assured, we do not know which key you typed, since the data is aggregated by each typing session, and keyboard size is unknown to us\n"))));
        instructionItems.add(new InstructionItem("Typing Session",
                new ArrayList<>(Arrays.asList("A legitimate typing session ends as:\n",
                        "1. The keyboard is closed or the screen is rotated\n",
                        "2. The session consists of 8 or more keystrokes\n",
                        "* Sometimes the keyboard is not closed properly, resulting in a prolonged typing session, so we recommend rotating the screen every time before ending your typing session to ensure the data is properly recorded.\n",
                        "* Each user needs to contribute at least 42 days (6 weeks) worth of typing data to be considered a legitimate subject, to ensure data integrity and model accuracy in predicting depresssion.\n"))));
        instructionItems.add(new InstructionItem("Data Privacy and Miscellaneous Issues",
                new ArrayList<>(Arrays.asList("Text data is not collected unless you use the journal feature within the app.\n",
                        "We do not collect textual data when the typing session is recorded outside the application.\n",
                        "The data collected is used to train a classification model to look at typing data to predict depression occurrence only, and will not be used for other purposes.\n",
                        "If there are any bugs within the app, or if there are any privacy concerns, do not hesitate to contact Zhan-Hang at chongzhanhang@student.usm.my\n"))));

        Log.d("DEBUG", "onCreate: Initializing recyclerview");
        RecyclerView recyclerView = findViewById(R.id.instruction_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new InstructionAdapter(this, instructionItems));
    }
}



