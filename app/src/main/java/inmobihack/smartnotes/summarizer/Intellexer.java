package inmobihack.smartnotes.summarizer;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rohit.kochar on 20/02/16.
 */
public class Intellexer {
    private static final String apiEndpoint = "http://api.intellexer.com/summarizeText?apikey=00b85c4c-398d-447b-ad5e-edb0caca211c&usePercentRestriction=true";
    public List<String> summarize(String text,int outputLength){
        RestClient restClient = new RestClient();
        ObjectMapper mapper = new ObjectMapper();
        List<String> result=null;
        String url = apiEndpoint+"&summaryRestriction="+outputLength;
        System.out.println("URL IS"+url);
        String summary="";
        try {
            Log.i("Intellexer API","Summarizing text"+text);
            summary = restClient.post(url,text);
            if(summary!=null && summary.length()!=0) {
                IntellexerOutput output = mapper.readValue(summary, IntellexerOutput.class);
                result = getSummary(output);
            }
        } catch (IOException e) {
            Log.e("Intellexer API","IOException while summarize call",e);
        }
    return result;
    }

    private List<String> getSummary(IntellexerOutput output){
        List<String> result = new ArrayList<>();
        for(IntellexerOutput.Item item : output.items){
            result.add(item.text);
        }
        return result;
    }

    public static void main(String args[]) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/Users/rohit.kochar/Desktop/sample.txt"));
        String text="";
        String line;
        while ((line=bufferedReader.readLine())!=null) {
            text += line;
            text += "\n";
        }
        new Intellexer().summarize(text, 1);
    }
}
