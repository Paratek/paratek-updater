package io.paratek.rs.util;

import com.google.common.flogger.FluentLogger;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.paratek.rs.analysis.hook.Game;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class JarHandler {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private static final Pattern PARAM_PATTERN = Pattern.compile("document\\.write\\('<param name=\"(.{1,2})\" value=\"(.+)\">'\\);");

    private final Game game;
    private final HashMap<String, ClassNode> classNodeMap = new HashMap<>();

    public static JarHandler getRemoteOSRS() {
        return new JarHandler(Game.OSRS);
    }

    public static JarHandler getRemoteRS3() {
        return new JarHandler(Game.RS3);
    }

    public JarHandler(final Game game) {
        this.game = game;
        logger.atInfo().log("Loading JarHandler");
        try {
            this.loadRemote();
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }
    }

    public JarHandler(final Game game, final File local) {
        this.game = game;
        logger.atInfo().log("Loading JarHandler");
        try {
            this.loadLocal(local);
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Write out the ClassNodes to a jar file
     * @param location
     */
    public void dumpTo(final String location) {
        try {
            JarOutputStream out = new JarOutputStream(new FileOutputStream(location));
            for (ClassNode cn : this.classNodeMap.values()) {
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cn.accept(cw);
                out.putNextEntry(new ZipEntry(cn.name + ".class"));
                out.write(cw.toByteArray());
                out.closeEntry();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Opens the JarInputStream from a FileInputStream
     * @param jarFile
     * @throws UnirestException
     * @throws IOException
     */
    private void loadLocal(final File jarFile) throws UnirestException, IOException {
        final Map<String, String> params = this.getParams();
        final JarInputStream inputStream = new JarInputStream(new FileInputStream(jarFile));
        readJarEntries(params, inputStream);
        logger.atInfo().log("JarHandler loaded from " + jarFile.getAbsolutePath() + " found " + this.getClassMap().size() + " classes");
    }


    /**
     * Opens the JarInputStream from the internet
     * @throws UnirestException
     * @throws IOException
     */
    private void loadRemote() throws UnirestException, IOException {
        final Map<String, String> params = this.getParams();
        final String jarPath = params.get("jarpath");
        final URL url = new URL(jarPath);
        final JarInputStream inputStream = new JarInputStream(url.openStream());
        readJarEntries(params, inputStream);
        logger.atInfo().log("JarHandler loaded from " + jarPath + " found " + this.getClassMap().size() + " classes");
    }


    /**
     * Reads the entries of the JarInputStream JarHandler#classNodeMap
     * @param params
     * @param inputStream
     * @throws IOException
     */
    private void readJarEntries(Map<String, String> params, JarInputStream inputStream) throws IOException {
        boolean isDecrypting = false;
        JarEntry entry;
        while ((entry = inputStream.getNextJarEntry()) != null) {
            if (this.game.equals(Game.OSRS)) {
                readEntryStream(inputStream, entry);
            } else if (this.game.equals(Game.RS3)) {
                if (entry.getName().contains("META-INF")) {
                    isDecrypting = true;
                    continue;
                }
                if (isDecrypting) {
                    if (entry.getName().equals("inner.pack.gz")) {
                        for (Map.Entry<String, byte[]> innerEntry : InnerPackDecrypter.decryptPack(inputStream, params.get("0"), params.get("-1")).entrySet()) {
                            final ClassReader reader = new ClassReader(innerEntry.getValue());
                            final ClassNode classNode = new ClassNode();
                            reader.accept(classNode, 0);
                            this.getClassMap().put(innerEntry.getKey()
                                    .replace(".class", "")
                                    .replace("/", "."), classNode);
                        }
                    }
                } else {
                    readEntryStream(inputStream, entry);
                }
            }
            inputStream.closeEntry();
        }
        inputStream.close();
    }


    /**
     * Reads JarEntry from JarInputStream
     * @param inputStream
     * @param entry
     * @throws IOException
     */
    private void readEntryStream(JarInputStream inputStream, JarEntry entry) throws IOException {
        if (entry.getName().endsWith(".class")) {
            final ClassReader reader = new ClassReader(inputStream);
            final ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);
            this.getClassMap().put(entry.getName()
                    .replace(".class", "")
                    .replace("/", "."), classNode);
        }
    }


    /**
     * Gets the parameters
     *  jar url is stored in key "jarpath"
     * @return
     * @throws UnirestException
     */
    private Map<String, String> getParams() throws UnirestException {
        final HashMap<String, String> params = new HashMap<>();
        final String webPage = Unirest.get(this.getURL())
                .asString()
                .getBody();
        final Matcher matcher = this.getPattern().matcher(webPage);
        if (matcher.find()) {
            final String matched = matcher.group(1);
            params.put("jarpath", this.getURL() + "/" + matched);
        }
        final Matcher paramMatcher = PARAM_PATTERN.matcher(webPage);
        while (paramMatcher.find()) {
            final String key = paramMatcher.group(1);
            final String value = paramMatcher.group(2);
            params.put(key, value);
        }
        return params;
    }

    /**
     * Get the game that's being loaded
     * @return
     */
    public Game getGame() {
        return game;
    }

    /**
     * Get the appropriate URL for selected game
     * @return
     */
    private String getURL() {
        return this.game.getWorldUrl();
    }


    /**
     * Get appropriate URL for selected game
     * @return
     */
    private Pattern getPattern() {
        return this.game.getArchivePattern();
    }


    /**
     * Get the map of ClassNodes
     * @return
     */
    public HashMap<String, ClassNode> getClassMap() {
        return this.classNodeMap;
    }

}
