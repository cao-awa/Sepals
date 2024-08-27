package com.github.cao.awa.sepals.backup;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.apricot.util.io.IOUtil;
import com.github.cao.awa.sepals.mixin.world.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;

public class SepalsBackupCenter {
    private static final Logger LOGGER = LogManager.getLogger("SepalBackupCenter");
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd,hh:mm:ss");
    private int currentId;
    private int currentUsedBase;
    private int wantRollback = -1;
    private boolean backupInProgress = false;
    private final File metadataFile;
    private final Map<Integer, SepalsBackup> backups = ApricotCollectionFactor.hashMap();
    private JSONObject metadata;

    private SepalsBackupCenter(String backupPath) throws IOException {
        this.metadataFile = new File(backupPath + "/metadata.json");
        this.metadata = JSONObject.parse(IOUtil.read(new FileReader(this.metadataFile, StandardCharsets.UTF_8)));
        this.currentId = this.metadata.getInteger("current_id");
        this.currentUsedBase = this.metadata.getInteger("current_base");
        this.metadata.getJSONObject("backups").forEach((id, backup) -> {
            this.backups.put(Integer.parseInt(id), SepalsBackup.fromJSON((JSONObject) backup));
        });
    }

    private SepalsBackupCenter(String backupPath, int currentId, int currentUsedBase) throws IOException {
        this.metadataFile = new File(backupPath + "/metadata.json");
        this.metadata = toJSON();
        refreshMetadata();
        this.currentId = currentId;
        this.currentUsedBase = currentUsedBase;
    }

    public static SepalsBackupCenter fromServer(MinecraftServer server) throws IOException {
        Path path = ((MinecraftServerAccessor) server).getSession().getDirectory().path().toAbsolutePath().getParent().toAbsolutePath();

        String backupPath = path + "/backups";

        File metadataFile = new File(backupPath + "/metadata.json");
        if (!metadataFile.isFile()) {
            return new SepalsBackupCenter(backupPath, 0, -1);
        }

        return new SepalsBackupCenter(
                backupPath
        );
    }

    public SepalsBackup getBackup(int id) {
        return this.backups.get(id);
    }

    public boolean backupInProgress() {
        return this.backupInProgress;
    }

    public int wantRollback() {
        return this.wantRollback;
    }

    public void wantRollback(int wantRollback) {
        this.wantRollback = wantRollback;
    }

    public void showBackups(BiConsumer<Text, Boolean> feedback) {
        int edge = this.backups.size() - 1;

        showBackups(feedback, edge);
    }

    public int backupCount() {
        return this.backups.size();
    }

    public void noticeDeleted(int id) throws IOException {
        this.backups.remove(id);
        refreshMetadata();
    }

    public void showBackups(BiConsumer<Text, Boolean> feedback, int start) {
        int i = Math.min(start, this.backups.size());
        int end = Math.max(-1, start - 5);

        List<SepalsBackup> backupList = ApricotCollectionFactor.arrayList(this.backups.values()).stream().sorted(Comparator.comparingInt(SepalsBackup::id)).toList();

        while (i > end) {
            SepalsBackup backup = backupList.get(i);

            MutableText backupId = MutableText.of(PlainTextContent.of("Backup #" + backup.id()));
            backupId.setStyle(backupId.getStyle().withColor(Formatting.DARK_AQUA));
            feedback.accept(backupId, false);

            StringBuilder builder = new StringBuilder();
            builder.append("Create time: ").append(this.dateFormat.format(new Date(backup.createTime())));
            if (backup.tips() != null) {
                builder.append("\n");
                builder.append("Tips: ").append(backup.tips());
            }

            MutableText backupDetails = MutableText.of(PlainTextContent.of(builder.toString()));
            backupDetails.setStyle(backupDetails.getStyle().withColor(Formatting.AQUA));
            feedback.accept(backupDetails, false);
            i--;
        }
    }

    public int makeBackup(Set<String> excludes, String tips, MinecraftServer server, BiConsumer<Text, Boolean> feedback) {
        if (this.wantRollback != -1) {
            MutableText errorDetails = MutableText.of(PlainTextContent.of("Server are current doing rollback, cannot start the backup"));
            errorDetails.setStyle(errorDetails.getStyle().withColor(Formatting.RED));
            feedback.accept(errorDetails, false);
            return -1;
        }

        if (this.backupInProgress) {
            MutableText errorDetails = MutableText.of(PlainTextContent.of("Server are current doing backup, cannot start the new backup"));
            errorDetails.setStyle(errorDetails.getStyle().withColor(Formatting.RED));
            feedback.accept(errorDetails, false);
            return -1;
        }

        SepalsBackup backup = new SepalsBackup(++this.currentId, System.currentTimeMillis());
        backup.tips(tips);
        try {
            doBackup(backup, excludes, server, feedback);
        } catch (Exception e) {
            MutableText happenedError = MutableText.of(PlainTextContent.of("Failed to backup, this backup will be delete, error: "));
            MutableText errorDetails = MutableText.of(PlainTextContent.of(SepalsBackup.makeErrorString(e, true)));
            happenedError.setStyle(happenedError.getStyle().withColor(Formatting.DARK_RED));
            errorDetails.setStyle(errorDetails.getStyle().withColor(Formatting.RED));
            feedback.accept(happenedError, false);
            feedback.accept(errorDetails, false);

            try {
                backup.doDelete(server, this, feedback);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.currentId--;
            return -1;
        }

        try {
            this.backups.put(backup.id(), backup);

            refreshMetadata();

            this.currentUsedBase = this.currentId;
            return backup.id();
        } catch (Exception e) {
            MutableText happenedError = MutableText.of(PlainTextContent.of("Failed to refresh the metadata, this backup will be delete, error: "));
            MutableText errorDetails = MutableText.of(PlainTextContent.of(SepalsBackup.makeErrorString(e, true)));
            happenedError.setStyle(happenedError.getStyle().withColor(Formatting.DARK_RED));
            errorDetails.setStyle(errorDetails.getStyle().withColor(Formatting.RED));
            feedback.accept(happenedError, false);
            feedback.accept(errorDetails, false);

            try {
                backup.doDelete(server, this, feedback);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.currentId--;
            return -1;
        }
    }

    public void doBackup(SepalsBackup backup, Set<String> excludes, MinecraftServer server, BiConsumer<Text, Boolean> feedback) {
        if (this.currentUsedBase != -1) {
            backup.doBackup(server, this.backups.get(this.currentUsedBase), excludes, feedback);
        } else {
            backup.doBackup(server, null, excludes, feedback);
        }
    }

    public void doRollback(MinecraftServer server, BiConsumer<Text, Boolean> feedback) {
        if (this.wantRollback == -1) {
            return;
        }

        doRollback(this.wantRollback, server, feedback);
    }

    public void doRebase(int id, MinecraftServer server, BiConsumer<Text, Boolean> feedback) {
        SepalsBackup backup = getBackup(id);
        try {
            if (backup == null) {
                return;
            }

            if (this.currentUsedBase != -1) {
                backup.doRebase(server, this, feedback);
            } else {
                backup.doRebase(server, this, feedback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doRollback(int id, MinecraftServer server, BiConsumer<Text, Boolean> feedback) {
        if (feedback == null) {
            feedback = (x1, x2) -> {};
        }

        if (this.wantRollback != -1) {
            MutableText errorDetails = MutableText.of(PlainTextContent.of("Server are current doing rollback, cannot start the backup"));
            errorDetails.setStyle(errorDetails.getStyle().withColor(Formatting.RED));
            feedback.accept(errorDetails, false);
            return;
        }

        LOGGER.info("Doing rollback for '#" + id + "'");

        this.wantRollback = id;

        SepalsBackup backup = getBackup(id);
        try {
            if (backup == null) {
                return;
            }

            if (this.currentUsedBase != -1) {
                backup.doRollback(server, null, this);
            } else {
                backup.doRollback(server, null, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.wantRollback = -1;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.fluentPut("current_id", this.currentId)
                .fluentPut("current_base", this.currentUsedBase);

        JSONObject backups = new JSONObject();
        for (SepalsBackup backup : this.backups.values()) {
            backups.put(String.valueOf(backup.id()), backup.toJSON());
        }
        json.put("backups", backups);

        return json;
    }

    private void refreshMetadata() throws IOException {
        if (!this.metadataFile.isFile()) {
            this.metadataFile.getParentFile().mkdirs();
            this.metadataFile.createNewFile();
        }
        this.metadata = toJSON();
        IOUtil.write(new FileWriter(this.metadataFile, StandardCharsets.UTF_8), this.metadata.toString(JSONWriter.Feature.PrettyFormat));
    }
}
