package com.example.shadowcrypt;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import shadowcryptconsole.Controller.ShadowCrypt;
import shadowcryptconsole.ENUM_Utilities.CIPHER_TYPE;
import shadowcryptconsole.ENUM_Utilities.TYPE;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private EditText keyInput;
    private TextView outputText;
    private Spinner cipherSpinner;
    private RadioGroup operationGroup;
    private Button processButton;
    private Button copyButton;
    private Button clearButton;

    private ShadowCrypt shadowCrypt;
    private CIPHER_TYPE selectedCipherType;
    private TYPE selectedOperationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize default values
        selectedCipherType = CIPHER_TYPE.SHIFT_CIPHER;
        selectedOperationType = TYPE.ENCRYPTION;

        // Initialize ShadowCrypt library
        shadowCrypt = new ShadowCrypt();

        // Initialize views
        initializeViews();

        // Setup cipher spinner
        setupCipherSpinner();

        // Setup listeners
        setupListeners();

        // Show developer info
        Toast.makeText(this, ShadowCrypt.DevInfo(), Toast.LENGTH_LONG).show();
    }

    private void initializeViews() {
        inputText = findViewById(R.id.inputText);
        keyInput = findViewById(R.id.keyInput);
        outputText = findViewById(R.id.outputText);
        cipherSpinner = findViewById(R.id.cipherSpinner);
        operationGroup = findViewById(R.id.operationGroup);
        processButton = findViewById(R.id.processButton);
        copyButton = findViewById(R.id.copyButton);
        clearButton = findViewById(R.id.clearButton);
        TextView keyLabel = findViewById(R.id.keyLabel);
    }

    private void setupCipherSpinner() {
        String[] cipherTypes = {
                "Shift Cipher",
                "Shift Cipher ASCII",
                "Vigenere Cipher",
                "Vigenere Cipher ASCII",
                "Vernam Cipher ASCII",
                "Binary"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.spinner_selected_item,
                cipherTypes
        );
        adapter.setDropDownViewResource(R.layout.spinner_item_white);
        cipherSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        // Cipher type selection
        cipherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedCipherType = CIPHER_TYPE.SHIFT_CIPHER;
                        updateKeyInputType(true, "Enter shift key (number)");
                        break;
                    case 1:
                        selectedCipherType = CIPHER_TYPE.SHIFT_CIPHER_ASCII;
                        updateKeyInputType(true, "Enter shift key (number)");
                        break;
                    case 2:
                        selectedCipherType = CIPHER_TYPE.VIGENERE_CIPHER;
                        updateKeyInputType(false, "Enter key (text)");
                        break;
                    case 3:
                        selectedCipherType = CIPHER_TYPE.VIGENERE_CIPHER_ASCII;
                        updateKeyInputType(false, "Enter key (text)");
                        break;
                    case 4:
                        selectedCipherType = CIPHER_TYPE.VERNAM_CIPHER_ASCII;
                        updateKeyInputType(false, "Enter key (text)");
                        break;
                    case 5:
                        selectedCipherType = CIPHER_TYPE.BINARY;
                        updateKeyInputType(true, "No key required");
                        keyInput.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Operation type selection
        operationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.encryptRadio) {
                    selectedOperationType = TYPE.ENCRYPTION;
                    processButton.setText("ENCRYPT");
                } else {
                    selectedOperationType = TYPE.DECRYPTION;
                    processButton.setText("DECRYPT");
                }
            }
        });

        // Process button
        processButton.setOnClickListener(v -> processText());

        // Copy button
        copyButton.setOnClickListener(v -> copyToClipboard());

        // Clear button
        clearButton.setOnClickListener(v -> clearAll());
    }

    private void updateKeyInputType(boolean isNumber, String hint) {
        keyInput.setEnabled(true);
        keyInput.setHint(hint);
        if (isNumber) {
            keyInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                    android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        } else {
            keyInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        }
    }

    private void processText() {
        String input = inputText.getText().toString();
        String key = keyInput.getText().toString();

        // Validate input
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter text to process", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate key (except for BINARY which doesn't need a key)
        if (selectedCipherType != CIPHER_TYPE.BINARY && key.isEmpty()) {
            Toast.makeText(this, "Please enter a key", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Prepare key object
            Object keyObject;
            if (selectedCipherType == CIPHER_TYPE.BINARY) {
                keyObject = null; // Binary doesn't use a key
            } else if (selectedCipherType == CIPHER_TYPE.SHIFT_CIPHER ||
                    selectedCipherType == CIPHER_TYPE.SHIFT_CIPHER_ASCII) {
                keyObject = Integer.parseInt(key);
            } else {
                keyObject = key;
            }

            // Process using ShadowCrypt library
            String result = ShadowCrypt.ProcessCipher(
                    selectedCipherType,
                    selectedOperationType,
                    input,
                    keyObject
            );

            // Display result
            outputText.setText(result);
            outputText.setVisibility(View.VISIBLE);
            copyButton.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Processing completed successfully!", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid key format. Please enter a valid number.",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void copyToClipboard() {
        String text = outputText.getText().toString();
        if (!text.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Encrypted/Decrypted Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAll() {
        inputText.setText("");
        keyInput.setText("");
        outputText.setText("");
        outputText.setVisibility(View.GONE);
        copyButton.setVisibility(View.GONE);
    }
}