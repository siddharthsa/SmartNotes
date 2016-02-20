package inmobihack.smartnotes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SummaryActivity extends AppCompatActivity {

    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        textView = (TextView) findViewById(R.id.summarizedoutput);
        Bundle p = getIntent().getExtras();
        textView.setText(p.getString("recognizedString"));

    }
}
