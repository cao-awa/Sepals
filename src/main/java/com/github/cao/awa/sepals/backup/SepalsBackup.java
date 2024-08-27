package com.github.cao.awa.sepals.backup;

import com.alibaba.fastjson2.JSONObject;
import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.apricot.util.digger.MessageDigger;
import com.github.cao.awa.apricot.util.io.IOUtil;
import com.github.cao.awa.lilium.mathematic.Mathematics;
import com.github.cao.awa.sepals.mixin.world.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

public class SepalsBackup {
    private static final Logger LOGGER = LogManager.getLogger("SepalsBackup");
    private final int id;
    private final long createTime;
    private String tips;
    private long sizeCount = 0;
    private boolean forceStop = false;

    public SepalsBackup(int id, long createTime) {
        this.id = id;
        this.createTime = createTime;
    }

    public static SepalsBackup fromJSON(JSONObject json) {
        return new SepalsBackup(
                json.getInteger("id"),
                json.getLong("create_time")
        );
    }

    public int id() {
        return this.id;
    }

    public void tips(String tips) {
        this.tips = tips;
    }

    public String tips() {
        return this.tips;
    }

    public long createTime() {
        return this.createTime;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.fluentPut("id", this.id)
                .fluentPut("create_time", this.createTime)
                .fluentPut("tips", this.tips);
        return json;
    }

    public void doBackup(MinecraftServer server, SepalsBackup diffTo, Set<String> excludes, BiConsumer<Text, Boolean> feedback) {
        try {
            long startTime = System.currentTimeMillis();
            this.sizeCount = 0;

            LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
            String path = session.getDirectory().path().toAbsolutePath().toString();

            File serverLevel = new File(path);

            // Get root and backup path
            File root = serverLevel.getParentFile();
            File backupPath = new File(root.getAbsoluteFile() + "/backups/" + this.id);

            Map<String, String> baseDiff = null;
            if (diffTo != null) {
                File diffPath = new File(root.getAbsoluteFile() + "/backups/" + diffTo.id());
                if (diffPath.isDirectory()) {
                    baseDiff = diffTo.loadDiff(server);
                }

                if (backupPath.isFile()) {
                    feedback(
                            feedback,
                            MutableText.of(PlainTextContent.of("The backup with id '" + this.id + "' already present, skipped!")),
                            false
                    );
                    return;
                }
            }

            MutableText startingBackupNotice = MutableText.of(PlainTextContent.of("Starting backup for '" + this.id + "'..."));
            startingBackupNotice.setStyle(startingBackupNotice.getStyle().withColor(Formatting.DARK_AQUA));
            feedback(feedback, startingBackupNotice, false);

            Map<String, String> selfDiff = ApricotCollectionFactor.hashMap();
            File[] files = serverLevel.listFiles();
            if (files != null) {
                writeTo(
                        serverLevel,
                        backupPath,
                        selfDiff,
                        baseDiff,
                        null,
                        "",
                        true
                );
            } else {
                feedback(
                        feedback,
                        MutableText.of(PlainTextContent.of("The target path '" + serverLevel.getPath() + "' has no any file!")),
                        false
                );
                return;
            }

            MutableText startExcludingNotice = MutableText.of(PlainTextContent.of("Start excluding..."));
            startExcludingNotice.setStyle(startExcludingNotice.getStyle().withColor(Formatting.DARK_AQUA));
            feedback(feedback, startExcludingNotice, false);

            if (!excludes.isEmpty()) {
                for (String exclude : excludes) {
                    boolean deleted = new File(exclude).delete();
                    if (deleted) {
                        MutableText excludingNotice = MutableText.of(PlainTextContent.of("Excluding target '" + exclude + "'"));
                        excludingNotice.setStyle(excludingNotice.getStyle().withColor(Formatting.DARK_AQUA));
                        feedback(feedback, excludingNotice, false);
                    }
                }
            }

            MutableText genDiffMapNotice = MutableText.of(PlainTextContent.of("Generating diff map..."));
            genDiffMapNotice.setStyle(genDiffMapNotice.getStyle().withColor(Formatting.DARK_AQUA));
            feedback(feedback, genDiffMapNotice, false);

            int noDiff = 0;
            int deleted = (int) selfDiff.values().stream().filter(s -> s.equals("^")).count();
            int hasDiff = selfDiff.size() - deleted;

            JSONObject diffFileJSON = new JSONObject();
            if (baseDiff != null) {
                for (Map.Entry<String, String> entry : baseDiff.entrySet()) {
                    String key = entry.getKey();
                    String hash = entry.getValue();
                    File file = new File(serverLevel.getAbsoluteFile() + "/" + key);
                    if (!selfDiff.containsKey(key)) {
                        if (file.isFile() && file.length() > 0) {
                            selfDiff.put(key, "*");
                            noDiff++;
                        } else {
                            selfDiff.put(key, "^");
                            deleted++;
                        }
                    } else {
                        if (!file.isFile() || file.length() == 0) {
                            selfDiff.put(key, "^");
                            deleted++;
                        }
                    }
                }

                diffFileJSON.put("based_from", diffTo.id());
            }

            JSONObject diffsJSON = new JSONObject(selfDiff);
            diffFileJSON.put("diffs", diffsJSON);

            IOUtil.write(
                    new FileWriter(backupPath + "/diff.json", StandardCharsets.UTF_8),
                    diffFileJSON.toString()
            );

            if (diffTo == null) {
                MutableText diffToNotice = MutableText.of(PlainTextContent.of("This backup has no based diff backup, '#" + this.id + "' will be create to the base diff"));
                diffToNotice.setStyle(diffToNotice.getStyle().withColor(Formatting.YELLOW));
                feedback(feedback, diffToNotice, false);
            } else {
                MutableText diffToNotice = MutableText.of(PlainTextContent.of("Made " + hasDiff + " files changed, " + deleted + " files deleted(or empty), " + noDiff + " keep it as is"));
                diffToNotice.setStyle(diffToNotice.getStyle().withColor(Formatting.YELLOW));
                feedback(feedback, diffToNotice, false);
            }

            String size;

            if (this.sizeCount / 1024 / 1024 / 1024 == 0) {
                size = ((double) this.sizeCount / 1024D / 1024D) + "MBb";
            } else {
                size = ((double) this.sizeCount / 1024D / 1024D / 1024D) + "GB";
            }

            MutableText doneNotice = MutableText.of(PlainTextContent.of("The backup '#" + this.id + "' already done now! used " + ((System.currentTimeMillis() - startTime) / 1000D) + "s, backup size is " + size));
            doneNotice.setStyle(doneNotice.getStyle().withColor(Formatting.GREEN));
            feedback(feedback, doneNotice, false);
        } catch (Exception e) {
            MutableText happenedError = MutableText.of(PlainTextContent.of("An error was happened without expect: "));
            MutableText errorDetails = MutableText.of(PlainTextContent.of(makeErrorString(e, true)));
            happenedError.setStyle(happenedError.getStyle().withColor(Formatting.DARK_RED));
            errorDetails.setStyle(errorDetails.getStyle().withColor(Formatting.RED));
            feedback(feedback, happenedError, false);
            feedback(feedback, errorDetails, false);
        }
    }

    public void doRebase(MinecraftServer server, SepalsBackupCenter center, BiConsumer<Text, Boolean> feedback) throws Exception {
        LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
        String path = session.getDirectory().path().toAbsolutePath().toString();

        File serverLevel = new File(path);

        // Get root and backup path
        File root = serverLevel.getParentFile();
        File backupPath = new File(root.getAbsoluteFile() + "/backups/" + this.id);

        int basedFrom = getDiffBasedFrom(server);
        if (basedFrom != -1) {
            Map<String, String> diffs = ApricotCollectionFactor.hashMap();
            center.getBackup(basedFrom).doRebaseTo(server, this, diffs, center, feedback);

            JSONObject diffFileJSON = new JSONObject();
            JSONObject diffsJSON = new JSONObject(diffs);
            diffFileJSON.put("diffs", diffsJSON);

            IOUtil.write(
                    new FileWriter(backupPath + "/diff.json", StandardCharsets.UTF_8),
                    diffFileJSON.toString()
            );
        }
    }

    public void doRebaseTo(MinecraftServer server, SepalsBackup destines, Map<String, String> destinesDiff, SepalsBackupCenter center, BiConsumer<Text, Boolean> feedback) throws Exception {
        LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
        String path = session.getDirectory().path().toAbsolutePath().toString();

        File serverLevel = new File(path);

        // Get root and backup path
        File root = serverLevel.getParentFile();
        File backupPath = new File(root.getAbsoluteFile() + "/backups/" + this.id);
        File destinesPath = new File(root.getAbsoluteFile() + "/backups/" + destines.id);

        Map<String, String> diffs = loadDiff(server);
        writeTo(
                backupPath,
                destinesPath,
                destinesDiff,
                null,
                diffs,
                "",
                true
        );

        int basedFrom = getDiffBasedFrom(server);
        if (basedFrom != -1) {
            center.getBackup(basedFrom).doRebaseTo(server, destines, destinesDiff, center, feedback);
        }

        doDelete(server, center, feedback);
    }

    public void doRollback(MinecraftServer server, Map<String, String> diffs, SepalsBackupCenter center) throws Exception {
        LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
        String path = session.getDirectory().path().toAbsolutePath().toString();

        File serverLevel = new File(path);

        // Get root and backup path
        File root = serverLevel.getParentFile();
        File backupPath = new File(root.getAbsoluteFile() + "/backups/" + this.id);

        serverLevel.delete();

        if (diffs == null) {
            diffs = loadDiff(server);
        } else {
            Map<String, String> inheritDiff = loadDiff(server);
            for (Map.Entry<String, String> entry : diffs.entrySet()) {
                String inheritValue = inheritDiff.get(entry.getKey());
                if ("*".equals(entry.getValue()) && !"*".equals(inheritValue)) {
                    inheritDiff.put(entry.getKey(), inheritValue);
                }
            }
        }

        File[] files = serverLevel.listFiles();
        if (files != null) {
            writeTo(
                    backupPath,
                    serverLevel,
                    null,
                    null,
                    diffs,
                    "",
                    true
            );
        }

        int basedFrom = getDiffBasedFrom(server);
        if (basedFrom != -1) {
            center.getBackup(basedFrom).doRollback(server, diffs, center);
        }
    }

    public void doDelete(MinecraftServer server, SepalsBackupCenter center, BiConsumer<Text, Boolean> feedback) throws IOException {
        LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
        String path = session.getDirectory().path().toAbsolutePath().toString();

        File serverLevel = new File(path);

        // Get root and backup path
        File root = serverLevel.getParentFile();
        File backupPath = new File(root.getAbsoluteFile() + "/backups/" + this.id);

        MutableText startingDeleteNotice = MutableText.of(PlainTextContent.of("Deleting backup for '" + this.id + "'..."));
        startingDeleteNotice.setStyle(startingDeleteNotice.getStyle().withColor(Formatting.DARK_AQUA));
        feedback(feedback, startingDeleteNotice, false);

        if (backupPath.isDirectory() || backupPath.isFile()) {
            delete(backupPath);
        }

        MutableText deletedNotice = MutableText.of(PlainTextContent.of("The backup '#" + this.id + "' already deleted"));
        deletedNotice.setStyle(deletedNotice.getStyle().withColor(Formatting.RED));
        feedback(feedback, deletedNotice, false);

        center.noticeDeleted(this.id);
    }

    public Map<String, String> loadDiff(MinecraftServer server) throws IOException {
        LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
        String path = session.getDirectory().path().toAbsolutePath().toString();

        File serverLevel = new File(path);

        // Get root and backup path
        File root = serverLevel.getParentFile();
        File diffPath = new File(root.getAbsoluteFile() + "/backups/" + this.id + "/diff.json");

        if (diffPath.isFile()) {
            JSONObject diff = JSONObject.parse(IOUtil.read(new FileReader(diffPath, StandardCharsets.UTF_8)));

            Map<String, String> diffMap = ApricotCollectionFactor.hashMap();

            diff.getJSONObject("diffs").forEach((key, hash) -> {
                diffMap.put(key, hash.toString());
            });

            return diffMap;
        }

        return null;
    }

    public int getDiffBasedFrom(MinecraftServer server) throws IOException {
        LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
        String path = session.getDirectory().path().toAbsolutePath().toString();

        File serverLevel = new File(path);

        // Get root and backup path
        File root = serverLevel.getParentFile();
        File diffPath = new File(root.getAbsoluteFile() + "/backups/" + this.id + "/diff.json");

        if (diffPath.isFile()) {
            JSONObject diff = JSONObject.parse(IOUtil.read(new FileReader(diffPath, StandardCharsets.UTF_8)));
            if (diff.containsKey("based_from")) {
                return diff.getInteger("based_from");
            }
        }

        return -1;
    }

    public static String makeErrorString(Throwable throwable, boolean printDetails) {
        if (printDetails) {
            LOGGER.error(throwable);
        }

        StringBuilder builder = new StringBuilder();

        builder.append(throwable.toString()).append("\n");

        // Print our stack trace
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement traceElement : trace) {
            builder.append(" at ").append(traceElement).append("\n");
            if (traceElement.toString().contains("com.github.cao.awa.sepals.command.SepalsBackupCommand.")) {
                break;
            }
        }

        builder.append("see more in console...");

        return builder.toString();
    }

    private void feedback(BiConsumer<Text, Boolean> feedback, Text text, boolean isError) {
        feedback.accept(text, isError);
    }

    private void forceStop() {
        this.forceStop = true;
    }

    private void delete(File target) {
        if (target.isDirectory()) {
            File[] files = target.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file);
                }
            }
        } else {
            target.delete();
        }
    }

    private boolean writeTo(File source, File destines, Map<String, String> diff, Map<String, String> diffTo, Map<String, String> checkerDiffs, String currentPath, boolean isDestinesDir) throws Exception {
        if (this.forceStop) {
            return false;
        }

        if (isDestinesDir) {
            destines.mkdirs();
        }

        if (source.isDirectory()) {
            File[] files = source.listFiles();
            if (files != null) {
                boolean isDirEmpty = true;

                for (File file : files) {
                    File destinesFile = new File(destines.getPath() + "/" + file.getName());

                    boolean isFileEmpty = writeTo(
                            file,
                            destinesFile,
                            diff,
                            diffTo,
                            checkerDiffs,
                            currentPath + "/" + file.getName(),
                            file.isDirectory()
                    );

                    if (isFileEmpty) {
                        if (file.isDirectory() || file.isFile()) {
                            destinesFile.delete();
                        }
                    }

                    isDirEmpty = isDirEmpty && isFileEmpty;
                }

                if (isDirEmpty) {
                    return true;
                }
            }
        } else {
            if (source.length() == 0 || source.getName().equals("session.lock") || source.getName().equals("diffs.json")) {
                if (diffTo != null) {
                    diff.put(currentPath, "^");
                }
                return true;
            }
            if (diff != null) {
                String hash = Mathematics.radix(MessageDigger.digestFile(source, MessageDigger.Sha3.SHA_256), 16, 36);
                if (diffTo != null) {
                    if (!hash.equals(diffTo.get(currentPath))) {
                        diff.put(currentPath, hash);
                        if (checkerDiffs != null) {
                            checkerDiffs.remove(currentPath);
                        }
                        IOUtil.copy(source, destines);
                        this.sizeCount += source.length();
                        return false;
                    }
                } else {
                    diff.put(currentPath, hash);
                    IOUtil.copy(source, destines);
                    this.sizeCount += source.length();
                    return false;
                }
            } else {
                String checkerDiff = checkerDiffs.get(currentPath);
                if (checkerDiff == null || "^".equals(checkerDiff) || "*".equals(checkerDiff)) {
                    return false;
                }
                IOUtil.copy(source, destines);
                this.sizeCount += source.length();
                checkerDiffs.remove(currentPath);
                return false;
            }
        }

        return true;
    }
}
