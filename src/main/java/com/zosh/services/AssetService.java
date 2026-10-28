package com.zosh.services;

import com.zosh.model.Asset;
import com.zosh.model.Coin;
import com.zosh.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface AssetService {

    Asset createAsset(User user , Coin coin ,double quantity);

    Asset getAssetById(Long assetId) throws Exception;

    Asset getAssetByUserIdAndId(Long userId , Long assetId );

    List<Asset>getUsersAssets(Long userId);

    Asset updateAsset(Long assetId ,double quantity) throws Exception;

    Asset findAssetByUserIdAndCoinId(Long userId , String coinId);

    void deleteAsset(Long assetId);
}
