package inmobihack.smartnotes.summarizer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * Created by rohit.kochar on 20/02/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntellexerOutput {
    @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Item {
      @JsonProperty
      public String text;
      @JsonProperty
      public int rank;
      @JsonProperty
      public double weight;
  }
    @JsonProperty
    public Set<Item> items;
}
