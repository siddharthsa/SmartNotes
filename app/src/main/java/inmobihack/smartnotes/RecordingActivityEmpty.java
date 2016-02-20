package inmobihack.smartnotes;

import android.content.Intent;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import inmobihack.smartnotes.views.WaveFormView;

public class RecordingActivityEmpty extends AppCompatActivity implements RecognitionListener {

    private TextView returnedText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "RecordingActivityEmpty";
    private WaveFormView waveFormView;
    private final int pauseinMillis = 1000;
    private String previousResult="";

    private AtomicLong previousCall = new AtomicLong(0);
    private ConcurrentLinkedQueue<Integer> fullStops = new ConcurrentLinkedQueue<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_activity_empty);

        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton.setBackgroundResource(R.drawable.default_mic);
        waveFormView = (WaveFormView) findViewById(R.id.wave);
        waveFormView.updateAmplitude(0, false);

        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);

        speech.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        previousCall = new AtomicLong(0);
        fullStops = new ConcurrentLinkedQueue<>();

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    toggleButton.setBackgroundResource(R.drawable.pressed_mic);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                }
                else{
                    toggleButton.setBackgroundResource(R.drawable.default_mic);
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                }
            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Resumed");
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "Paused");
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }

        super.onPause();
    }

    /// RecognitionListener interface implementations

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
//        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
        waveFormView.updateAmplitude(rmsdB / 12, true);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //TODO: String.format for logging
        Log.i(LOG_TAG, "onBufferReceived of length " + buffer.length + ": " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        toggleButton.setChecked(false);
        waveFormView.updateAmplitude(0, false);
    }

    @Override
    public void onError(int errorCode) {

        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        toggleButton.setChecked(false);
        waveFormView.updateAmplitude(0, false);
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // TODO: StringBuilder
        String finalResult = matches.get(0);
        finalResult = punctuate(finalResult);
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra("recognizedString",finalResult );
        startActivity(intent);
        returnedText.setText(finalResult);
    }

    private String punctuate(String text){
        Log.i(LOG_TAG,"Text to punctuate "+text);
        String result ="";
        if(fullStops.size() ==0)
            return text;
        String tmp[] = text.split(" ");
        Integer fullStopAt = fullStops.poll();
        for (int i = 0 ;i<tmp.length ;i++){
            result+=" "+tmp[i];
        if(fullStopAt!=null && fullStopAt-1 == i){
            result+=".";
            fullStopAt = fullStops.poll();
        }
        }
        return result;
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(LOG_TAG, "onPartialResults" + partialResults.size());
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null || matches.size() > 0){
            Log.i(LOG_TAG,"PartialResult: " + matches.get(0));
            if(previousCall.get()!=0 && System.currentTimeMillis() - previousCall.get() >= pauseinMillis){
                Log.i(LOG_TAG, "Adding full stop");
                fullStops.add(previousResult.split(" ").length);
            }
//            String partialString = matches.get(0);
//            if (prevLength + 1 < partialString.length()) {
//
//                String newWord = partialString.substring((prevLength == 0) ? 0 : prevLength + 1);
//                finalResult += " " + newWord;
//
//                returnedText.setText(newWord);
//                Log.i(LOG_TAG, "newWord: " + newWord);
//                prevLength = partialString.length();
//
//            }
            previousResult = matches.get(0);
            previousCall.set(System.currentTimeMillis());
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(LOG_TAG, "onEvent");
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
