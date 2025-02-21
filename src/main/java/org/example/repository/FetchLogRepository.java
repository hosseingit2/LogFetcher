package org.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@Component
public class FetchLogRepository {

    Logger logger = LoggerFactory.getLogger(FetchLogRepository.class);
    private final static String tmpPath = "/tmp/log_fetcher";

    public String getLogs(String filePath, Long requestedNumOfLogs, String keyWord) throws Exception {
        Long currentLogsWritten = 0L;
        Random r = new Random();
        String tmpFileName = tmpPath+"/"+Math.abs(r.nextLong())+".txt";
        try (SeekableByteChannel ch = java.nio.file.Files.newByteChannel(Paths.get(filePath), StandardOpenOption.READ)) {
            ByteBuffer bf = ByteBuffer.allocate(2);
            int numOfBytes = 0;
            StringBuilder sb = new StringBuilder();
            String halfSentence = "";
            while ((numOfBytes = ch.read(bf)) > 0) {
                List<String> logLines = new ArrayList<>();
                for(int i=0; i < numOfBytes ; i++) {
                    if (i==0 && halfSentence.length() > 0){
                       sb.append(halfSentence);
                    }
                    sb.append((char)bf.get(i));
                    if (i==numOfBytes-1) {
                        if ((char)bf.get(i) != '\n') {
                            halfSentence = sb.toString();
                        }
                    }
                    if ((char)bf.get(i) == '\n') {
                        halfSentence = "";
                        if (keyWord.length() == 0 || (keyWord.length() > 0 && sb.toString().contains(keyWord))) {
                            logLines.add(sb.toString());
                        }
                        int length = sb.length();
                        sb.delete(0, length);
                    }
                }
                int length = sb.length();
                sb.delete(0, length);
                if (logLines.size() > 0) {
                    updateFileContent(tmpFileName, logLines, currentLogsWritten, requestedNumOfLogs);
                    currentLogsWritten += logLines.size();
                }
                bf.clear();
            }
            updateContentFileSize(tmpFileName, requestedNumOfLogs, currentLogsWritten);
            return tmpFileName;
        } catch (Exception e) {
            logger.error("Error Repo Happened", e);
            throw new Exception("Failed to read data!!!");
        }
    }

    private void updateFileContent(String tmpFileName, List<String> logLines, Long currentLogSize, Long requestedNumOfLogs) throws  Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File d = new File(tmpPath);
            if (!d.exists()) {
                Files.createDirectory(Path.of(tmpPath));
            }
            File f = new File(tmpFileName);
            boolean append = currentLogSize == 0L ? false : true;
            FileWriter fw = new FileWriter(f, append);
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < logLines.size(); i++) {
                try {
                    bw.write(logLines.get(i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            bw.close();
        } catch (Exception e) {
            throw  new Exception("Failed to write to file");
        }
    }

    private void updateContentFileSize(String tmpFileName, Long requestedNumOfLogs, Long currentLogSize) {
        Long linesToDelete = currentLogSize - requestedNumOfLogs;
        if (linesToDelete <= 0) {
            return;
        }
        File f = new File(tmpFileName);
        File tmpFile = new File(tmpFileName+"_tmp.txt");
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter(tmpFile, false);
            BufferedWriter bw = new BufferedWriter(fw);
            String line = br.readLine();
            Long lineCounter = 0L;
            while (line != null) {
                if (linesToDelete > 0 && lineCounter < Math.abs(linesToDelete)) {
                    line = br.readLine();
                    lineCounter += 1;
                    continue;
                }
                bw.write(line);
                bw.newLine();
                line = br.readLine();
                lineCounter += 1;
            }
            bw.close();
            br.close();
            tmpFile.renameTo(f);
            tmpFile.delete();
        } catch (Exception e) {
            logger.error("Failed to resize file.", e);
            new Exception("Failed to resize file.");
        }
    }
}
