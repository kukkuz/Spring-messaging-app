package com.kukkuz.chat.web;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.Principal;
import java.util.HashMap;

/**
 * Created by kukku on 22/6/16.
 */
@Controller
public class MongoStreamController {

    @Autowired
    GridFsTemplate gridFsTemplate;

    @RequestMapping(value="/chat/upload", method = RequestMethod.POST)
    @ResponseBody
    public HashMap<String, String> singleFileUpload(@RequestParam("file") MultipartFile file,
            @RequestParam("name") String username, Principal principal) {
        DBObject metaData = new BasicDBObject();
        // receiver and sender as metadata
        metaData.put("user_1", principal.getName());
        metaData.put("user_2", username);
        InputStream inputStream = null;
        String id = "";
        try {
            inputStream = file.getInputStream();
            id = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType(), metaData).getId().toString();
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("I/O Exception");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    System.out.println("Failed to close");
                }
            }
        }
        HashMap<String, String> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @RequestMapping(value = "/chat/download/{id}", method = RequestMethod.GET)
    public void streamWithByteRange(@PathVariable("id") String id, Principal principal, HttpServletRequest request, HttpServletResponse response) {
        GridFSDBFile gridFSDBFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));

        // check if user has access
        DBObject metaData = gridFSDBFile.getMetaData();
        if(!((String)metaData.get("user_1")).equalsIgnoreCase(principal.getName())
                && !((String)metaData.get("user_2")).equalsIgnoreCase(principal.getName()))
            return;

        //stream with HTTP byte range
        try {
            MultipartFileSender.fromInputStream(gridFSDBFile.getInputStream(), gridFSDBFile.getFilename(), gridFSDBFile.getLength(),
                    gridFSDBFile.getUploadDate(), gridFSDBFile.getContentType()).with(request).with(response).serveStreamResource();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
