package com.yourname.earthdownloader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EarthDownloader extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("EarthDownloader enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("EarthDownloader disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("downloadearthmap")) {
            sender.sendMessage("⏬ Starting Earth map download...");
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    File serverRoot = getDataFolder().getParentFile().getParentFile();
                    File zipFile = new File(serverRoot, "earth1-543.zip");
                    File extractDir = new File(serverRoot, "EarthWorld");

                    String fileURL = "https://cdn.earthmc.org/downloads/earth1-543.zip";
                    downloadWithProgress(fileURL, zipFile);

                    sender.sendMessage("✅ Download complete. Extracting...");
                    unzip(zipFile, extractDir);
                    sender.sendMessage("✅ Extracted to: " + extractDir.getName());

                } catch (Exception e) {
                    sender.sendMessage("❌ Error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            return true;
        }
        return false;
    }

    private void downloadWithProgress(String fileURL, File destination) throws IOException {
        URL url = new URL(fileURL);
        URLConnection connection = url.openConnection();
        long fileSize = connection.getContentLengthLong();

        try (InputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fos = new FileOutputStream(destination)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            AtomicLong totalRead = new AtomicLong(0);

            long lastLogTime = System.currentTimeMillis();
            long nextLogDelay = 5000; // 5 seconds

            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead.addAndGet(bytesRead);

                long now = System.currentTimeMillis();
                if (now - lastLogTime >= nextLogDelay) {
                    double percent = (100.0 * totalRead.get()) / fileSize;
                    getLogger().info(String.format("Download progress: %.2f%%", percent));
                    lastLogTime = now;
                }
            }

            getLogger().info("Download finished at 100%");
        }
    }

    private void unzip(File zipFile, File destDir) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
