package poealerts;

import java.util.List;

public class Alert {
    // Comment for improved readability of json file.
    public String comment;
    // Ability to disable part of alert configuration without deleting any text.
    // Useful if user wants to disable just physical reflect warning for current
    // build, but keep it in file for future builds.
    public boolean enabled;
    // All regex conditions in this list must be met.
    public List<String> matchAll;
    // Any regex condition in this list must be met.
    public List<String> matchAny;
    // Sound file to play. If file is missing (or empty): play beep.
    public String sound;
}