package com.zosh.services;

import com.zosh.model.Coin;
import com.zosh.model.User;
import com.zosh.model.WatchList;

public interface WatchListService {

    WatchList findUserWatchlist(Long userId  ) throws Exception;
    WatchList createWatchList(User user);
    WatchList findById(Long id) throws Exception;

    Coin addItemToWatchList(Coin coin , User user) throws Exception;
}
