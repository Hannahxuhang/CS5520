package edu.neu.madcourse.hangxu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class Instructions extends AppCompatActivity {
    private static final String PHASE1_INSTRUCTION = "1. At the start of the game, a 9x9 grid with 9x9 " +
            "letters in the grid is displayed. Each smaller\n" + "grid has been populated with 9-letter " +
            "words;" + System.lineSeparator() + "2. In phase 1, you have 1.5 min to play the game;" +
            "for each small tile, you have to select a word then press the ok button, the score will calculated based on the length and" +
            "first letter of the word you selected;" + System.lineSeparator() + "3. Once a letter has been selected, " +
            "the letters in other small tiles will be locked and cannot be selected in this round";
    private static final String PHASE2_INSTRUCTION = "1. After 1.5 min you will automatically enter into phase 2;"
            + System.lineSeparator() + "2. In this phase, only the valid letters you have selected will be available" +
            "and you can selected from any of them";

    TextView phase1_instruction;
    TextView phase2_instruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        phase1_instruction = (TextView) findViewById(R.id.textView12);
        phase2_instruction = (TextView) findViewById(R.id.textView15);

        phase1_instruction.setText(PHASE1_INSTRUCTION);
        phase2_instruction.setText(PHASE2_INSTRUCTION);
    }
}
