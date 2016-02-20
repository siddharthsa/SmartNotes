package inmobihack.smartnotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import inmobihack.smartnotes.summarizer.Intellexer;

public class SummaryActivity extends AppCompatActivity {

    EditText editText = null;
    Context context = this;
    Set<String> result;

    private final String LOG_TAG = SummaryActivity.class.getSimpleName();

    private final View.OnClickListener saveSummaryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogLayout= inflater.inflate(R.layout.dialog_save, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.saveFile);
            builder.setView(dialogLayout);
            final EditText fileNameEditText = (EditText) dialogLayout.findViewById(R.id.fileName);
            fileNameEditText.setText(String.valueOf(System.currentTimeMillis()));
            builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(LOG_TAG, "Clicked");
                    saveContentToDisk(String.valueOf(fileNameEditText.getText()));
                    Intent intent = new Intent(context, ListNotesActivity.class);
                    context.startActivity(intent);
                }
            });

            AlertDialog saveDialog = builder.create();
            saveDialog.show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        editText = (EditText) findViewById(R.id.summarizedoutput);
        Bundle p = getIntent().getExtras();
        String text = p.getString("recognizedString");
        result = null;
        try {
            result = new BackGroundTask().execute(text).get();
            editText.setText(result.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        findViewById(R.id.approve_button).setOnClickListener(saveSummaryListener);
    }

    private View.OnClickListener saveSummaryListener() {
        return null;
    }

    class BackGroundTask extends AsyncTask<String,Void,Set<String>>{

        @Override
        protected Set<String> doInBackground(String... params) {
            return new Intellexer().summarize(params[0], 10);
        }
    }

    private void saveContentToDisk(String fileName) {
        // Get the directory for the user's public pictures directory.
        // writeExternal();
        if (result != null){
            if (fileName.isEmpty())
                fileName = String.valueOf(System.currentTimeMillis());
            writeInternal(fileName);
        }
        else
            Toast.makeText(getApplicationContext(), "no result to be saved",Toast.LENGTH_SHORT);
    }

    private void writeInternal(String filename) {

        FileOutputStream outputStream;
        Log.d(LOG_TAG, "saveContentToDisk: Entered");

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(result.toString().getBytes());
            outputStream.close();
            Log.d(LOG_TAG, "wrote successfully at" + getFilesDir().getAbsolutePath());

            Toast.makeText(getApplicationContext(), "wrote successfully at" + getFilesDir().getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d(LOG_TAG, "exception", e);
            e.printStackTrace();
        }
    }
}
