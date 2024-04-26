package com.gempukku.lotro.merchant;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.packs.SetDefinition;
import org.apache.commons.collections4.map.LRUMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MerchantService {
    private final Merchant _merchant;
    private final long _priceGuaranteeExpire = 1000 * 60 * 5;
    private final Map<String, PriceGuarantee> _priceGuarantees = Collections.synchronizedMap(new LRUMap(100));

    private final ReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final Set<BasicCardItem> _merchantableItems = new HashSet<>();
    private final Set<String> _merchantableStrings = new HashSet<>();

    private final Map<String, Integer> _fixedPriceItems = new HashMap<>();

    private final CollectionType _permanentCollection = CollectionType.MY_CARDS;
    private final CollectionsManager _collectionsManager;

    public MerchantService(LotroCardBlueprintLibrary library, CollectionsManager collectionsManager) {
        _collectionsManager = collectionsManager;

        _merchant = new RarityBasedMerchant(library);

        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            if (setDefinition.Merchantable()) {
                for (String blueprintId : setDefinition.getAllCards()) {
                    String baseBlueprintId = library.getBaseBlueprintId(blueprintId);
                    _merchantableItems.add(new BasicCardItem(baseBlueprintId));
                    _merchantableStrings.add(baseBlueprintId);
                }
            }
        }

        addFixedItem("FotR - Booster", 1000);
        addFixedItem("MoM - Booster", 1000);
        addFixedItem("RotEL - Booster", 1000);
        addFixedItem("TTT - Booster", 1000);
        addFixedItem("BoHD - Booster", 1000);
        addFixedItem("EoF - Booster", 1000);
        addFixedItem("RotK - Booster", 1000);
        addFixedItem("SoG - Booster", 1000);
        addFixedItem("MD - Booster", 1000);
        addFixedItem("SH - Booster", 1000);
        addFixedItem("BR - Booster", 1000);
        addFixedItem("BL - Booster", 1000);
        addFixedItem("HU - Booster", 1000);
        addFixedItem("RoS - Booster", 1000);
        addFixedItem("TaD - Booster", 1000);

        addFixedItem("REF - Booster", 3000);

        addFixedItem("FotR - Gandalf Starter", 2500);
        addFixedItem("FotR - Aragorn Starter", 2500);
        addFixedItem("MoM - Gandalf Starter", 2500);
        addFixedItem("MoM - Gimli Starter", 2500);
        addFixedItem("RotEL - Boromir Starter", 2500);
        addFixedItem("RotEL - Legolas Starter", 2500);

        addFixedItem("TTT - Aragorn Starter", 2500);
        addFixedItem("TTT - Theoden Starter", 2500);
        addFixedItem("BoHD - Eowyn Starter", 5500);
        addFixedItem("BoHD - Legolas Starter", 5500);
        addFixedItem("EoF - Faramir Starter", 5500);
        addFixedItem("EoF - Witch-king Starter", 5500);

        addFixedItem("RotK - Aragorn Starter", 2500);
        addFixedItem("RotK - Eomer Starter", 2500);
        addFixedItem("SoG - Merry Starter", 2500);
        addFixedItem("SoG - Pippin Starter", 2500);
        addFixedItem("MD - Frodo Starter", 2500);
        addFixedItem("MD - Sam Starter", 2500);

        addFixedItem("SH - Aragorn Starter", 2500);
        addFixedItem("SH - Eowyn Starter", 2500);
        addFixedItem("SH - Gandalf Starter", 2500);
        addFixedItem("SH - Legolas Starter", 2500);
        addFixedItem("BR - Mouth Starter", 2500);
        addFixedItem("BR - Saruman Starter", 2500);
        addFixedItem("BL - Arwen Starter", 2500);
        addFixedItem("BL - Boromir Starter", 2500);

        addFixedItem("Expanded", 1000*15);
        addFixedItem("Wraith", 1000*6);
        addFixedItem("AgesEnd", 1000*40);
    }

    private void addFixedItem(String blueprintId, int price) {
        _fixedPriceItems.put(blueprintId, price);
        _merchantableItems.add(new BasicCardItem(blueprintId));
        _merchantableStrings.add(blueprintId);
    }

    public Set<BasicCardItem> getSellableItems() {
        return Collections.unmodifiableSet(_merchantableItems);
    }

    public PriceGuarantee priceCards(Player player, Collection<CardItem> cardBlueprintIds) {
        Lock lock = _lock.readLock();
        lock.lock();
        try {
            Date currentTime = new Date();
            Map<String, Integer> buyPrices = new HashMap<>();
            Map<String, Integer> sellPrices = new HashMap<>();
            for (CardItem cardItem : cardBlueprintIds) {
                String blueprintId = cardItem.getBlueprintId();

                Integer fixedPrice = _fixedPriceItems.get(blueprintId);
                if (fixedPrice != null) {
                    sellPrices.put(blueprintId, fixedPrice);
                } else {
                    Integer buyPrice = _merchant.getCardBuyPrice(blueprintId, currentTime);

                    if (buyPrice != null)
                        buyPrices.put(blueprintId, buyPrice);
                    if (_merchantableStrings.contains(blueprintId)) {
                        Integer sellPrice = _merchant.getCardSellPrice(blueprintId, currentTime);
                        if (sellPrice != null)
                            sellPrices.put(blueprintId, sellPrice);
                    }
                }
            }
            PriceGuarantee priceGuarantee = new PriceGuarantee(sellPrices, buyPrices, currentTime);
            _priceGuarantees.put(player.getName(), priceGuarantee);
            return priceGuarantee;
        } finally {
            lock.unlock();
        }
    }

    public void merchantBuysCard(Player player, String blueprintId, int price) throws MerchantException, SQLException, IOException {
        priceCards(player, Collections.singleton(new BasicCardItem(blueprintId)));

        Date currentTime = new Date();
        Lock lock = _lock.writeLock();
        lock.lock();
        try {
            PriceGuarantee guarantee = _priceGuarantees.get(player.getName());
//            if (guarantee == null || guarantee.getDate().getTime() + _priceGuaranteeExpire < currentTime.getTime())
//                throw new MerchantException("Price guarantee has expired");
            Integer guaranteedPrice = guarantee.getBuyPrices().get(blueprintId);
//            if (guaranteedPrice == null || price != guaranteedPrice)
//                throw new MerchantException("Guaranteed price does not match the user asked price");

            boolean success = _collectionsManager.sellCardInPlayerCollection(player, _permanentCollection, blueprintId, price);
            if (!success)
                throw new MerchantException("Unable to remove the sold card from your collection");

            _priceGuarantees.remove(player.getName());
            _merchant.cardBought(blueprintId, currentTime, price);
        } finally {
            lock.unlock();
        }
    }

    public void merchantSellsCard(Player player, String blueprintId, int price) throws MerchantException, SQLException, IOException {
        priceCards(player, Collections.singleton(new BasicCardItem(blueprintId)));

        Date currentTime = new Date();
        Lock lock = _lock.writeLock();
        lock.lock();
        try {
            PriceGuarantee guarantee = _priceGuarantees.get(player.getName());
//            if (guarantee == null || guarantee.getDate().getTime() + _priceGuaranteeExpire < currentTime.getTime())
//                throw new MerchantException("Price guarantee has expired");
            Integer guaranteedPrice = guarantee.getSellPrices().get(blueprintId);
//            if (guaranteedPrice == null || price != guaranteedPrice)
//                throw new MerchantException("Guaranteed price does not match the user asked price");

            boolean success = _collectionsManager.buyCardToPlayerCollection(player, _permanentCollection, blueprintId, price);
            if (!success)
                throw new MerchantException("Unable to remove required currency from your collection");

            _priceGuarantees.remove(player.getName());
            _merchant.cardSold(blueprintId, currentTime, price);
        } finally {
            lock.unlock();
        }
    }

    public void tradeForFoil(Player player, String blueprintId) throws MerchantException, SQLException, IOException {
        if (!blueprintId.contains("_") || blueprintId.endsWith("*"))
            throw new MerchantException("Unable to trade in this type of item");
        Lock lock = _lock.writeLock();
        lock.lock();
        try {
            boolean success = _collectionsManager.tradeCards(player, _permanentCollection, blueprintId, 4, blueprintId + "*", 1, 400);
            if (!success)
                throw new MerchantException("Unable to remove the required cards or currency from your collection");
        } finally {
            lock.unlock();
        }
    }

    public static class PriceGuarantee {
        private final Map<String, Integer> _sellPrices;
        private final Map<String, Integer> _buyPrices;
        private final Date _date;

        private PriceGuarantee(Map<String, Integer> sellPrices, Map<String, Integer> buyPrices, Date date) {
            _sellPrices = sellPrices;
            _buyPrices = buyPrices;
            _date = date;
        }

        public Date getDate() {
            return _date;
        }

        public Map<String, Integer> getBuyPrices() {
            return _buyPrices;
        }

        public Map<String, Integer> getSellPrices() {
            return _sellPrices;
        }
    }
}
