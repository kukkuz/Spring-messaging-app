package com.kukkuz.chat.web;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by kukku on 17/6/16.
 */
@Controller
public class StreamController {

    private final String PATH = "/home/kukku/Desktop/Vettah [2016] Malayalam 720p HD AVC x264 1.4GB/Vettah [2016] Malayalam 720p HD AVC x264 1.4GB.mp4";
    private final String PATH1 = "/home/kukku/Desktop/videos/1.mp4";

    @RequestMapping(value="/upload", method = RequestMethod.POST)
    @ResponseBody
    public String singleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        InputStream stream = file.getInputStream();
        FileOutputStream fileOuputStream = new FileOutputStream("/home/kukku/Desktop/videos/temp/" + file.getOriginalFilename());
        int len = -1;
        byte[] buffer = new byte[8192];
        while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
            fileOuputStream.write(buffer, 0, len);
        }
        fileOuputStream.close();
        return String.valueOf(file.getSize());
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void streamWithByteRange(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //stream with HTTP byte range
        MultipartFileSender.fromURIString(PATH1).with(request).with(response).serveResource();
    }

    @RequestMapping(value = "/download/norange", method = RequestMethod.GET, produces = "application/octet-stream")
    @ResponseBody
    public FileSystemResource streamWithoutByteRange() {
        return new FileSystemResource(PATH1);
    }

    @RequestMapping("/download/norange/other")
    @ResponseBody
    public StreamingResponseBody handle() {
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                    File file = new File(PATH1);
                    InputStream iStream = new FileInputStream(file);
                    IOUtils.copy(iStream, outputStream);
            }
        };
    }

    /**
     * stream copy
     */
    /*
    try {
        String path = repositoryService.findVideoLocationById(id);
        File file = new File(path)
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename="+file.getName().replace(" ", "_"));
        InputStream iStream = new FileInputStream(file);
        IOUtils.copy(iStream, response.getOutputStream());
        response.flushBuffer();
    } catch (java.nio.file.NoSuchFileException e) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    } catch (Exception e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    */
}
