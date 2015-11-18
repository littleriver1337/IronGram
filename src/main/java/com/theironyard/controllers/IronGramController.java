package com.theironyard.controllers;

import com.theironyard.entities.Photo;
import com.theironyard.entities.User;
import com.theironyard.services.PhotoRepository;
import com.theironyard.services.UserRepository;
import com.theironyard.utils.PasswordHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by MattBrown on 11/17/15.
 */
@RestController
public class IronGramController {

    @Autowired
    UserRepository users;

    @Autowired
    PhotoRepository photos;

    @RequestMapping("/login")
    public User login(HttpSession session,
                      HttpServletResponse response,
                      String username,
                      String password
    ) throws Exception {
        User user = users.findOneByUsername(username);//sees if it exists in data base
        if(user == null){
            user = new User();
            user.username = username;
            user.password = PasswordHash.createHash(password);
            users.save(user);
        }
        else if (!PasswordHash.validatePassword(password, user.password)){
            throw new Exception ("Wrong Password");
        }
        session.setAttribute("username", username);
        response.sendRedirect("/");
        return user;
    }
    @RequestMapping("/logout")
    public void logOut(HttpSession session,
                       HttpServletResponse response) throws IOException {
        session.invalidate();
        response.sendRedirect("/");
    }
    @RequestMapping("/user")
    public User user(HttpSession session){
        String username = (String) session.getAttribute("username");
        if(username == null){
            return null;
        }
        return users.findOneByUsername(username);
    }
    @RequestMapping("/upload")
    public Photo upload(HttpSession session,
                        HttpServletResponse response,
                        String receiver,
                        MultipartFile photo,
                        long deletePhoto,
                        boolean isPublic
    ) throws Exception {
        String username = (String) session.getAttribute("username");
        if(username == null){
            throw new Exception("Not Logged In");
        }
        User senderUser = users.findOneByUsername(username);
        User receiverUser = users.findOneByUsername(receiver);

        if (receiverUser == null) {
            throw new Exception ("Receiver name doesn't exist!");
        }
        if (!photo.getContentType().startsWith("image")){
            throw new Exception("Only images are allowed!");
        }
        File photoFile = File.createTempFile("photo",
                photo.getOriginalFilename(),
                new File("public"));
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes());

        Photo p = new Photo();
        p.sender = senderUser;
        p.receiver = receiverUser;
        p.fileName = photoFile.getName();
        p.deletePhoto = deletePhoto;
        p.isPublic = isPublic;
        photos.save(p);
        response.sendRedirect("/");
        return p;
    }

    @RequestMapping("/photos")
    public List<Photo> showPhotos(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        if(username == null){
            throw new Exception ("Not logged in");
        }
        User user = users.findOneByUsername(username);
        List<Photo> photoList = photos.findByReceiver(user);
        for (Photo p : photoList) {
            if (p.accessTime == null) {
                p.accessTime = LocalDateTime.now();
                photos.save(p);
                //waitToDelete(p, p.deleteSeconds);
            } else if (p.accessTime.isBefore(LocalDateTime.now().minusSeconds(p.deletePhoto))) {
                photos.delete(p);
                File file = new File("public", p.fileName);
                file.delete();
            }
        }
        return photos.findByReceiver(user);
    }
    /*public void waitToDelete(Photo photo, int seconds){
        /*Thread thread = new Thread(()->{
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            photos.delete(photo);
            File file = new File("public", photo.fileName);
            file.delete();
        });
        thread.start();

        Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run(){
                photos.delete(photo);
                File file = new File ("public", photo.fileName);
                file.delete();
            }
        }, seconds * 1000);
    }
*/
    @RequestMapping("/public-photos")
    public List<Photo> publicPhotos(String username) {
        User user = users.findOneByUsername(username);
        List<Photo> publicList = photos.findBySender(user);
        ArrayList<Photo> photoList = new ArrayList<>();

        for(Photo p : publicList){
            if(p.isPublic){
               photoList.add(p);
            }
        }
        return photoList;
    }
}
