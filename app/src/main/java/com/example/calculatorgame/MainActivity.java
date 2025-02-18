package com.example.calculatorgame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvOperation, tvTimer, tvScore, tvMaxScore;
    private Button btnSubmit, btnDelete, btnClearAll;
    private Button[] numberButtons = new Button[9];
    private List<Integer> currentNumbers = new ArrayList<>();
    private String currentOperation;
    private String originalLeftPart, originalRightPart;
    private int currentResult;
    private int score = 0;
    private CountDownTimer timer;
    private List<Integer> userInput = new ArrayList<>();
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_MAX_SCORE = "maxScore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        tvOperation = findViewById(R.id.tvOperation);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvMaxScore = findViewById(R.id.tvMaxScore);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnDelete = findViewById(R.id.btnDelete);
        btnClearAll = findViewById(R.id.btnClearAll);

        for (int i = 0; i < 9; i++) {
            String buttonID = "btn" + (i + 1);
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            numberButtons[i] = findViewById(resID);
            final int number = i + 1;
            numberButtons[i].setOnClickListener(v -> onNumberClick(number));
        }

        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnDelete.setOnClickListener(v -> deleteLastNumber());
        btnClearAll.setOnClickListener(v -> clearAllNumbers());

        updateScoreDisplay();
        startGame();
    }

    private void startGame() {
        generateOperation();
        startTimer();
        displayMaxScore();
    }

    private void generateOperation() {
        Random random = new Random();
        int numOperators = random.nextInt(3) + 1;
        List<Character> operators = new ArrayList<>();
        currentNumbers.clear();
        userInput.clear();

        boolean includeMultiply = random.nextBoolean();
        if (includeMultiply) {
            operators.add('*');
            numOperators--;
        }

        for (int i = 0; i < numOperators; i++) {
            operators.add(random.nextBoolean() ? '+' : '-');
        }

        for (int i = 0; i <= operators.size(); i++) {
            int num;
            do {
                num = random.nextInt(9) + 1;
            } while (currentNumbers.contains(num));
            currentNumbers.add(num);
        }

        do {
            currentResult = currentNumbers.get(0);
            for (int i = 0; i < operators.size(); i++) {
                switch (operators.get(i)) {
                    case '+':
                        currentResult += currentNumbers.get(i + 1);
                        break;
                    case '-':
                        if (currentResult > currentNumbers.get(i + 1)) {
                            currentResult -= currentNumbers.get(i + 1);
                        } else {
                            currentResult += currentNumbers.get(i + 1);
                            operators.set(i, '+');
                        }
                        break;
                    case '*':
                        currentResult *= currentNumbers.get(i + 1);
                        break;
                }
            }
            if (currentResult <= 0) {
                currentNumbers.clear();
                for (int i = 0; i <= operators.size(); i++) {
                    int num;
                    do {
                        num = random.nextInt(9) + 1;
                    } while (currentNumbers.contains(num));
                    currentNumbers.add(num);
                }
            }
        } while (currentResult <= 0);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < operators.size(); i++) {
            sb.append("_ ").append(operators.get(i)).append(" ");
        }
        sb.append("_");
        originalLeftPart = sb.toString();
        originalRightPart = String.valueOf(currentResult);
        currentOperation = originalLeftPart + " = " + originalRightPart;

        tvOperation.setText(currentOperation);
        resetButtons();
    }

    private void onNumberClick(int number) {
        if (originalLeftPart != null) {
            userInput.add(number);
            updateOperationDisplay();
            numberButtons[number - 1].setEnabled(false);
        }
    }

    private void updateOperationDisplay() {
        String newLeft = originalLeftPart;
        for (int num : userInput) {
            newLeft = newLeft.replaceFirst("_", String.valueOf(num));
        }
        currentOperation = newLeft + " = " + originalRightPart;
        tvOperation.setText(currentOperation);
    }

    private void checkAnswer() {
        String[] leftParts = currentOperation.split(" = ")[0].split(" ");
        int calculatedResult = Integer.parseInt(leftParts[0]);
        for (int i = 1; i < leftParts.length; i += 2) {
            int num = Integer.parseInt(leftParts[i + 1]);
            switch (leftParts[i]) {
                case "+":
                    calculatedResult += num;
                    break;
                case "-":
                    calculatedResult -= num;
                    break;
                case "*":
                    calculatedResult *= num;
                    break;
            }
        }
        if (calculatedResult == currentResult) {
            score++;
            updateScoreDisplay();
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show();
            generateOperation();
        } else {
            Toast.makeText(this, "Incorrecto. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            resetOperation();
        }
    }

    private void resetOperation() {
        userInput.clear();
        updateOperationDisplay();
        resetButtons();
    }

    private void resetButtons() {
        for (Button btn : numberButtons) {
            btn.setEnabled(true);
        }
    }

    private void deleteLastNumber() {
        if (!userInput.isEmpty()) {
            int lastNumber = userInput.remove(userInput.size() - 1);
            updateOperationDisplay();
            numberButtons[lastNumber - 1].setEnabled(true);
        }
    }

    private void clearAllNumbers() {
        userInput.clear();
        updateOperationDisplay();
        resetButtons();
    }

    private void startTimer() {
        timer = new CountDownTimer(360000, 1000) {
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 60000);
                int seconds = (int) ((millisUntilFinished % 60000) / 1000);
                tvTimer.setText(String.format("Tiempo: %d:%02d", minutes, seconds));
            }

            public void onFinish() {
                tvTimer.setText("¡Tiempo terminado!");
                endGame();
            }
        }.start();
    }

    private void endGame() {
        for (Button btn : numberButtons) {
            btn.setEnabled(false);
        }
        btnSubmit.setEnabled(false);
        btnDelete.setEnabled(false);
        btnClearAll.setEnabled(false);
        Toast.makeText(this, "Juego terminado. Puntaje final: " + score, Toast.LENGTH_LONG).show();
        updateMaxScore();
    }

    private void updateScoreDisplay() {
        tvScore.setText("Puntaje: " + score);
        updateMaxScore();
    }

    private void displayMaxScore() {
        int maxScore = prefs.getInt(KEY_MAX_SCORE, 0);
        tvMaxScore.setText("Máximo: " + maxScore);
    }

    private void updateMaxScore() {
        int maxScore = prefs.getInt(KEY_MAX_SCORE, 0);
        if (score > maxScore) {
            prefs.edit().putInt(KEY_MAX_SCORE, score).apply();
            tvMaxScore.setText("Máximo: " + score);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}