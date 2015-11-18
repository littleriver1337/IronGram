package com.theironyard.entities;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by MattBrown on 11/17/15.
 */
@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    public int id;

    @Column(nullable = false)
    public long deletePhoto;

    public boolean isPublic;

    public LocalDateTime accessTime;

    @ManyToOne
    public User sender;

    @ManyToOne
    public User receiver;

    @Column(nullable = false)
    public String fileName;
}
