package inmobihack.smartnotes;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import inmobihack.smartnotes.summarizer.Intellexer;

public class SummaryActivity extends AppCompatActivity {

    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        textView = (TextView) findViewById(R.id.summarizedoutput);
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
        textView.setText(result.toString());


    }

    class BackGroundTask extends AsyncTask<String,Void,Set<String>>{

        @Override
        protected Set<String> doInBackground(String... params) {
            return new Intellexer().summarize(params[0], 10);
        }
    }
}
