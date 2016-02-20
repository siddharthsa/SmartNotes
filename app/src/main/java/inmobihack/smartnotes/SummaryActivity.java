package inmobihack.smartnotes;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import inmobihack.smartnotes.summarizer.Intellexer;

public class SummaryActivity extends AppCompatActivity {

    EditText editText = null;

    private final View.OnClickListener saveSummaryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText et = (EditText) findViewById(R.id.summarizedoutput);
            String summary = et.getText().toString();

            Intent intent = new Intent(v.getContext(), SaveSummaries.class);
            intent.putExtra("finalSummary", summary);
            startActivity(intent);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        editText = (EditText) findViewById(R.id.summarizedoutput);
        Bundle p = getIntent().getExtras();
        String text = p.getString("recognizedString");
        Set<String> result = null;
        try {
            result = new BackGroundTask().execute(text).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        editText.setText(result.toString());

        findViewById(R.id.approve_button).setOnClickListener(saveSummaryListener);
    }

    private View.OnClickListener saveSummaryListener() {
        return null;
    }

    class BackGroundTask extends AsyncTask<String,Void,Set<String>>{

        @Override
        protected Set<String> doInBackground(String... params) {
            return new Intellexer().summarize(params[0], 33);
        }
    }
}
