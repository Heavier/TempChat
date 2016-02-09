package com.example.dam.tempchat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText editText;
    private TextView textView, txThink;
    private ImageButton imageButton;
    private ChatterBotFactory factory;
    private ChatterBotSession bot2session;
    private ChatterBot bot2;

    private TextToSpeech tts;
    private double tono = 1.0;
    private double velocidad = 1.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageButton = (ImageButton) findViewById(R.id.imageButton);
        this.textView = (TextView) findViewById(R.id.textView);
        this.editText = (EditText) findViewById(R.id.editText);
        this.txThink = (TextView)findViewById(R.id.textView2);

        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, 1);

        factory = new ChatterBotFactory();

        try {
            bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
            bot2session = bot2.createSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviar(View view) throws Exception {
        Tarea tarea = new Tarea();
        tarea.execute(editText.getText().toString());
    }

    public void hablar(View view) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-EN");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.prompt);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        startActivityForResult(i, 2);
    }

    public class Tarea extends AsyncTask<String, Integer, Boolean>{
        String respuesta = "";
        String pregunta;
        TextView textView = (TextView) findViewById(R.id.textView);

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                pregunta = params[0];
                respuesta = bot2session.think(params[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txThink.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            textView.append(respuesta + "\n");
            frase(respuesta);
            txThink.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        } else if (requestCode == 2) {
            ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Tarea tarea = new Tarea();
            tarea.execute(textos.get(0));
        }
    }

    private void frase(String frase) {
        tts.setPitch((float) tono);
        tts.setSpeechRate((float) velocidad);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(frase, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(frase, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ENGLISH);
        } else {
            // No dice nada
        }
    }
}
