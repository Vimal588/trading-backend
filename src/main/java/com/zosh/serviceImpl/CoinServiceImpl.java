
package com.zosh.serviceImpl;

import com.zosh.model.Coin;
import com.zosh.model.Roi;
import com.zosh.repository.CoinRepository;
import com.zosh.services.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CoinServiceImpl implements CoinService {

    @Autowired
    private CoinRepository coinRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ====================================================
    // SHARED TTL CACHE — every CoinGecko-calling method
    // below uses this. CoinGecko's free-tier rate limit is
    // shared across ALL endpoints combined, so leaving even
    // one method uncached (e.g. getCoinList) can exhaust the
    // quota and 429 every other endpoint too.
    // ====================================================
    private final Map<String, CacheEntry<Object>> cache = new ConcurrentHashMap<>();
    private static final long LIST_TTL_MILLIS = 60_000;       // 1 min — coin lists change fast-ish
    private static final long CHART_TTL_MILLIS = 5 * 60_000;  // 5 min — chart data, less urgent
    private static final long DETAILS_TTL_MILLIS = 2 * 60_000; // 2 min — coin details

    private static class CacheEntry<T> {
        final T data;
        final long timestamp;
        CacheEntry(T data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
        boolean isFresh(long ttlMillis) {
            return (System.currentTimeMillis() - timestamp) < ttlMillis;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key, long ttlMillis) {
        CacheEntry<Object> entry = cache.get(key);
        if (entry != null && entry.isFresh(ttlMillis)) {
            return (T) entry.data;
        }
        return null;
    }

    private void putCache(String key, Object data) {
        cache.put(key, new CacheEntry<>(data, System.currentTimeMillis()));
    }


    @Override
    public List<Coin> getCoinList(int page) throws Exception {
        String cacheKey = "coinList:" + page;
        List<Coin> cached = getCached(cacheKey, LIST_TTL_MILLIS);
        if (cached != null) return cached;

        String url ="https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&per_page=10&page="+page;
        RestTemplate restTemplate = new RestTemplate();

        try{
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
            ResponseEntity<String> response = restTemplate.exchange(url , HttpMethod.GET,entity,String.class);
            List<Coin> coinList = objectMapper.readValue(response.getBody(), new TypeReference<List<Coin>>() {});

            putCache(cacheKey, coinList);

            return coinList;
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getMarketChart(String coinId, int days) throws Exception {
        String cacheKey = "chart:" + coinId + ":" + days;
        String cached = getCached(cacheKey, CHART_TTL_MILLIS);
        if (cached != null) return cached;

        String url = "https://api.coingecko.com/api/v3/coins/"
                + coinId + "/market_chart?vs_currency=usd&days=" + days;
        RestTemplate restTemplate = new RestTemplate();

        try{
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
            ResponseEntity<String> response = restTemplate.exchange(url , HttpMethod.GET,entity,String.class);
            String body = response.getBody();

            putCache(cacheKey, body);

            return body;
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new Exception(e.getMessage());
        }
    }
    @Override
    public String getCoinDetails(String coinId) throws Exception {

        String cacheKey = "details:" + coinId;
        String cached = getCached(cacheKey, DETAILS_TTL_MILLIS);
        if (cached != null) return cached;

        String url =
                "https://api.coingecko.com/api/v3/coins/" + coinId;

        RestTemplate restTemplate = new RestTemplate();

        try {

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            String.class
                    );

            JsonNode jsonNode =
                    objectMapper.readTree(response.getBody());

            Coin coin = new Coin();

            // =====================================
            // BASIC INFORMATION
            // =====================================

            coin.setId(
                    jsonNode.path("id").asText()
            );

            coin.setName(
                    jsonNode.path("name").asText()
            );

            coin.setSymbol(
                    jsonNode.path("symbol").asText()
            );


            // =====================================
            // IMAGE
            // =====================================

            coin.setImage(
                    jsonNode.path("image")
                            .path("large")
                            .asText(null)
            );


            // =====================================
            // MARKET DATA
            // =====================================

            JsonNode marketData =
                    jsonNode.path("market_data");


            // Current Price

            JsonNode currentPrice =
                    marketData.path("current_price").path("usd");

            coin.setCurrentPrice(
                    currentPrice.isMissingNode() || currentPrice.isNull()
                            ? null
                            : currentPrice.asDouble()
            );


            // Market Cap

            JsonNode marketCap =
                    marketData.path("market_cap").path("usd");

            coin.setMarketCap(
                    marketCap.isMissingNode() || marketCap.isNull()
                            ? null
                            : marketCap.asLong()
            );


            // Market Cap Rank

            JsonNode marketCapRank =
                    marketData.path("market_cap_rank");

            coin.setMarketCapRank(
                    marketCapRank.isMissingNode() || marketCapRank.isNull()
                            ? null
                            : marketCapRank.asInt()
            );


            // Fully Diluted Valuation

            JsonNode fullyDilutedValuation =
                    marketData
                            .path("fully_diluted_valuation")
                            .path("usd");

            coin.setFullyDilutedValuation(
                    fullyDilutedValuation.isMissingNode()
                            || fullyDilutedValuation.isNull()
                            ? null
                            : fullyDilutedValuation.asLong()
            );


            // Total Volume

            JsonNode totalVolume =
                    marketData.path("total_volume").path("usd");

            coin.setTotalVolume(
                    totalVolume.isMissingNode() || totalVolume.isNull()
                            ? null
                            : totalVolume.asDouble()
            );


            // =====================================
            // 24 HOUR DATA
            // =====================================

            // High 24h

            JsonNode high24h =
                    marketData.path("high_24h").path("usd");

            coin.setHigh24h(
                    high24h.isMissingNode() || high24h.isNull()
                            ? null
                            : high24h.asDouble()
            );


            // Low 24h

            JsonNode low24h =
                    marketData.path("low_24h").path("usd");

            coin.setLow24h(
                    low24h.isMissingNode() || low24h.isNull()
                            ? null
                            : low24h.asDouble()
            );


            // Price Change 24h

            JsonNode priceChange24h =
                    marketData.path("price_change_24h");

            coin.setPriceChange24h(
                    priceChange24h.isMissingNode() || priceChange24h.isNull()
                            ? null
                            : priceChange24h.asDouble()
            );


            // Price Change Percentage 24h

            JsonNode priceChangePercentage24h =
                    marketData.path("price_change_percentage_24h");

            coin.setPriceChangePercentage24h(
                    priceChangePercentage24h.isMissingNode()
                            || priceChangePercentage24h.isNull()
                            ? null
                            : priceChangePercentage24h.asDouble()
            );


            // =====================================
            // MARKET CAP CHANGE
            // =====================================

            // Market Cap Change 24h

            JsonNode marketCapChange24h =
                    marketData
                            .path("market_cap_change_24h");

            coin.setMarketCapChange24h(
                    marketCapChange24h.isMissingNode()
                            || marketCapChange24h.isNull()
                            ? null
                            : marketCapChange24h.asDouble()
            );


            // Market Cap Change Percentage 24h

            JsonNode marketCapChangePercentage24h =
                    marketData
                            .path("market_cap_change_percentage_24h");

            coin.setMarketCapChangePercentage24h(
                    marketCapChangePercentage24h.isMissingNode()
                            || marketCapChangePercentage24h.isNull()
                            ? null
                            : marketCapChangePercentage24h.asDouble()
            );


            // =====================================
            // SUPPLY
            // =====================================

            // Circulating Supply

            JsonNode circulatingSupply =
                    marketData.path("circulating_supply");

            coin.setCirculatingSupply(
                    circulatingSupply.isMissingNode()
                            || circulatingSupply.isNull()
                            ? null
                            : circulatingSupply.asDouble()
            );


            // Total Supply

            JsonNode totalSupply =
                    marketData.path("total_supply");

            coin.setTotalSupply(
                    totalSupply.isMissingNode()
                            || totalSupply.isNull()
                            ? null
                            : totalSupply.asDouble()
            );


            // Max Supply

            JsonNode maxSupply =
                    marketData.path("max_supply");

            coin.setMaxSupply(
                    maxSupply.isMissingNode()
                            || maxSupply.isNull()
                            ? null
                            : maxSupply.asDouble()
            );


            // =====================================
            // ALL TIME HIGH
            // =====================================

            // ATH

            JsonNode ath =
                    marketData.path("ath").path("usd");

            coin.setAth(
                    ath.isMissingNode() || ath.isNull()
                            ? null
                            : ath.asDouble()
            );


            // ATH Change Percentage

            JsonNode athChangePercentage =
                    marketData.path("ath_change_percentage").path("usd");

            coin.setAthChangePercentage(
                    athChangePercentage.isMissingNode()
                            || athChangePercentage.isNull()
                            ? null
                            : athChangePercentage.asDouble()
            );


            // ATH Date

            JsonNode athDate =
                    marketData.path("ath_date").path("usd");

            coin.setAthDate(
                    athDate.isMissingNode() || athDate.isNull()
                            ? null
                            : athDate.asText()
            );


            // =====================================
            // ALL TIME LOW
            // =====================================
            JsonNode roiNode = jsonNode.path("roi");

            if (!roiNode.isMissingNode() && !roiNode.isNull()) {

                Roi roi = new Roi();

                JsonNode timesNode = roiNode.path("times");
                if (!timesNode.isMissingNode() && !timesNode.isNull()) {
                    roi.setTimes(timesNode.asDouble());
                }

                JsonNode currencyNode = roiNode.path("currency");
                if (!currencyNode.isMissingNode() && !currencyNode.isNull()) {
                    roi.setCurrency(currencyNode.asText());
                }

                JsonNode percentageNode = roiNode.path("percentage");
                if (!percentageNode.isMissingNode() && !percentageNode.isNull()) {
                    roi.setPercentage(percentageNode.asDouble());
                }

                coin.setRoi(roi);
            }
            // ATL

            JsonNode atl =
                    marketData.path("atl").path("usd");

            coin.setAtl(
                    atl.isMissingNode() || atl.isNull()
                            ? null
                            : atl.asDouble()
            );


            // ATL Change Percentage

            JsonNode atlChangePercentage =
                    marketData
                            .path("atl_change_percentage")
                            .path("usd");

            coin.setAtlChangePercentage(
                    atlChangePercentage.isMissingNode()
                            || atlChangePercentage.isNull()
                            ? null
                            : atlChangePercentage.asDouble()
            );


            // ATL Date

            JsonNode atlDate =
                    marketData.path("atl_date").path("usd");

            coin.setAtlDate(
                    atlDate.isMissingNode() || atlDate.isNull()
                            ? null
                            : atlDate.asText()
            );


            // =====================================
            // LAST UPDATED
            // =====================================

            JsonNode lastUpdated =
                    jsonNode.path("last_updated");

            coin.setLastUpdated(
                    lastUpdated.isMissingNode() || lastUpdated.isNull()
                            ? null
                            : lastUpdated.asText()
            );


            // =====================================
            // SAVE
            // =====================================

            coinRepository.save(coin);

            String body = response.getBody();
            putCache(cacheKey, body);

            return body;

        } catch (HttpClientErrorException |
                 HttpServerErrorException e) {

            throw new Exception(e.getMessage());
        }
    }
    @Override
    public Coin findById(String coinId) throws Exception {
        Optional<Coin> optionalCoin = coinRepository.findById(coinId);
        if(optionalCoin.isEmpty()) throw new Exception("Coin not found");
        return optionalCoin.get();
    }

    @Override
    public String searchCoin(String keyword) throws Exception {
        String cacheKey = "search:" + keyword;
        String cached = getCached(cacheKey, LIST_TTL_MILLIS);
        if (cached != null) return cached;

        String url ="https://api.coingecko.com/api/v3/search?query="+keyword;
        RestTemplate restTemplate = new RestTemplate();

        try{
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
            ResponseEntity<String> response = restTemplate.exchange(url , HttpMethod.GET,entity,String.class);
            String body = response.getBody();

            putCache(cacheKey, body);

            return body;
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getTop50CoinsByMarketCapRank() throws Exception {
        String cacheKey = "top50";
        String cached = getCached(cacheKey, LIST_TTL_MILLIS);
        if (cached != null) return cached;

        String url =
                "https://api.coingecko.com/api/v3/coins/markets"
                        + "?vs_currency=usd&per_page=50&page=1";
        RestTemplate restTemplate = new RestTemplate();

        try{
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
            ResponseEntity<String> response = restTemplate.exchange(url , HttpMethod.GET,entity,String.class);
            String body = response.getBody();

            putCache(cacheKey, body);

            return body;
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public String getTreadingCoins() throws Exception {
        String cacheKey = "trending";
        String cached = getCached(cacheKey, LIST_TTL_MILLIS);
        if (cached != null) return cached;

        String url =
                "https://api.coingecko.com/api/v3/search/trending";
        RestTemplate restTemplate = new RestTemplate();

        try{
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);
            ResponseEntity<String> response = restTemplate.exchange(url , HttpMethod.GET,entity,String.class);
            String body = response.getBody();

            putCache(cacheKey, body);

            return body;
        }catch (HttpClientErrorException | HttpServerErrorException e){
            throw new Exception(e.getMessage());
        }
    }
}