package com.zosh.serviceImpl;

import com.zosh.model.Coin;
import com.zosh.model.User;
import com.zosh.model.WatchList;
import com.zosh.repository.UserRepository;
import com.zosh.repository.WatchListRepository;
import com.zosh.services.WatchListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WatchListServiceImpl implements WatchListService {
    @Autowired
    private WatchListRepository watchListRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public WatchList findUserWatchlist(Long userId) throws Exception {

        WatchList watchList = watchListRepository.findByUserId(userId);

        if (watchList == null) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("User not found"));

            WatchList newWatchList = new WatchList();
            newWatchList.setUser(user);

            return watchListRepository.save(newWatchList);
//            throw new Exception("Watchlist not found");
        }

        return watchList;
    }

    @Override
    public WatchList createWatchList(User user) {
        WatchList watchList = new WatchList();
        watchList.setUser(user);
        return watchListRepository.save(watchList);
    }

    @Override
    public WatchList findById(Long id) throws Exception {
        Optional<WatchList> watchListOptional = watchListRepository.findById(id);
        if(watchListOptional.isEmpty()){
            throw new Exception("Watchlist not found");
        }
        return watchListOptional.get();
    }

    @Override
    public Coin addItemToWatchList(Coin coin, User user) throws Exception {
        WatchList watchList = findUserWatchlist(user.getId());
        if(watchList.getCoins().contains(coin)){
            watchList.getCoins().remove(coin);
        }
        else watchList.getCoins().add(coin);
        watchListRepository.save(watchList);
        return coin;
    }
}
