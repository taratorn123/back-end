package com.angular.donationblock.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity 
{
    @Id
    @GeneratedValue
    private Long id;
    private boolean deleted;

    public Long getId() 
    {
        return id;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public boolean isDeleted() 
    {
        return deleted;
    }

    public void setDeleted(boolean deleted) 
    {
        this.deleted = deleted;
    }
}
