package org.example.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.example.model.FetchRequest;
import org.example.repository.FetchLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
public class LogFetcherController {

    Logger logger = LoggerFactory.getLogger(LogFetcherController.class);
    @Autowired
    private FetchLogRepository fetchLogRepository;


    @RequestMapping(
            value = "/fetch/logs",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )

    @ResponseBody
    ResponseEntity<InputStreamResource> getLogs(@RequestBody FetchRequest fetchRequest) {
        try {
            logger.info("Received Request {}",fetchRequest);
            String filePath = fetchLogRepository.getLogs(fetchRequest.logFilePath, fetchRequest.numOfLogs, fetchRequest.filterKeyWord);
            File f = new File(filePath);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(f));
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f.getName() + "\"")
                    .contentLength(f.length())
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error Happened", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

}

