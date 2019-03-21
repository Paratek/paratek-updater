package io.paratek.rs.analysis.hook;

import java.util.regex.Pattern;

public enum Game {

    RS3("http://world1.runescape.com", Pattern.compile("document.write\\('archive=(gamepack.+\\.jar) '\\);")),
    OSRS("http://oldschool1.runescape.com", Pattern.compile("document.write\\('archive=(gamepack_\\d+.jar) '\\);"));

    private final String worldUrl;
    private final Pattern archivePattern;

    Game(String worldUrl, Pattern archivePattern) {
        this.worldUrl = worldUrl;
        this.archivePattern = archivePattern;
    }

    public Pattern getArchivePattern() {
        return archivePattern;
    }

    public String getWorldUrl() {
        return worldUrl;
    }

}
