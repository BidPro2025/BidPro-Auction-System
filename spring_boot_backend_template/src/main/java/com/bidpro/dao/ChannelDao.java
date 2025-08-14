package com.bidpro.dao;

import com.bidpro.entities.Channel;
import com.bidpro.entities.Channel.ChannelName; // Correct import for the inner enum
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelDao extends JpaRepository<Channel, Integer> {
    Channel findByName(ChannelName name);
}