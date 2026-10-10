package com.gempukku.lotro.merchant;

import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.db.MerchantDAO;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class StorageBasedMerchantTest extends LoadedLibraries {
    private StorageBasedMerchant _merchant;
    private static final long DAY = 1000 * 60 * 60 * 24;
    private MockMerchantDAO _merchantDao;

    @Before
    public void setup() {
        Date setupDate = new Date(0);
        _merchantDao = new MockMerchantDAO();

        _merchant = new StorageBasedMerchant(_cardLibrary, _merchantDao, setupDate);
    }

    @Test
    public void cardTradingWithNoStock() {
        // Now merchant sells also cards out of stock, but at double price
        assertNull(_merchant.getCardSellPrice("1_1", new Date(0)));
        assertNotNull(_merchant.getCardBuyPrice("1_1", new Date(0)));
    }

    @Test
    public void cardTradingWithSomeStock() {
        _merchant.cardBought("1_1", new Date(0), 1000);
        assertNotNull(_merchant.getCardSellPrice("1_1", new Date(0)));
        assertNotNull(_merchant.getCardBuyPrice("1_1", new Date(0)));
    }

    @Test
    public void cardTradingWithFullStock() {
        for (int i=0; i<100; i++)
            _merchant.cardBought("1_1", new Date(0), 1000);
        assertNotNull(_merchant.getCardSellPrice("1_1", new Date(0)));
        assertNull(_merchant.getCardBuyPrice("1_1", new Date(0)));
    }

    @Test
    public void cardPriceLowersAfterMerchantBuys() {
        _merchant.cardBought("1_1", new Date(0), 700);
        int cardInitialPrice = _merchant.getCardSellPrice("1_1", new Date(0));
        assertEqualsMoreOrLess((int) (1000 / 1.1f), cardInitialPrice);
    }

    @Test
    public void cardPriceRisesAfterMerchantSells() {
        // Setup some stock
        _merchant.cardBought("1_1", new Date(0), 700);
        _merchant.cardBought("1_1", new Date(0), 700);

        _merchant.cardSold("1_1", new Date(0), 1000);
        int cardInitialPrice = _merchant.getCardSellPrice("1_1", new Date(0));
        assertEqualsMoreOrLess((int) (1000*1.1f), cardInitialPrice);
    }

    @Test
    public void cardPriceRisesOverTimeWhenNoStock() {
        int initialPrice = _merchant.getCardBuyPrice("1_1", new Date(0));
        assertTrue(initialPrice < _merchant.getCardBuyPrice("1_1", new Date(DAY)));
    }

    @Test
    public void cardPriceLowersOverTimeWhenInStock() {
        for (int i=0; i<11; i++)
            _merchant.cardBought("1_1", new Date(0), 700);
        int initialPrice = _merchant.getCardBuyPrice("1_1", new Date(0));
        assertTrue(initialPrice > _merchant.getCardBuyPrice("1_1", new Date(DAY)));
    }

    @Test
    public void buyAndSellLosesOnValue() {
        _merchantDao.addTransaction("1_1", 1000, new Date(0), MerchantDAO.TransactionType.BUY);
        _merchantDao.setStock("1_1", 5);

        long time = 0;
        // Buy 5 times the same card every 10 seconds, then sell the 5, also every 10 sec
        int moneySpent = 0;
        int moneyEarned = 0;
        for (int i=0; i<5; i++) {
            int sellPrice = _merchant.getCardSellPrice("1_1", new Date(time));
            moneySpent+=sellPrice;
            _merchant.cardSold("1_1", new Date(time), sellPrice);
            System.out.println("Bought for: "+sellPrice);
            time+=10*1000;
        }

        System.out.println("Selling for : "+_merchant.getCardSellPrice("1_1", new Date(time)));
        System.out.println("Buying for: "+_merchant.getCardBuyPrice("1_1", new Date(time)));

        for (int i=0; i<5; i++) {
            int buyPrice = _merchant.getCardBuyPrice("1_1", new Date(time));
            moneyEarned+=buyPrice;
            _merchant.cardBought("1_1", new Date(time), buyPrice);
            System.out.println("Sold for: "+buyPrice);
            time+=10*1000;
        }
        System.out.println("Money spent: "+moneySpent);
        System.out.println("Money earned: "+moneyEarned);
        assertTrue(moneySpent>moneyEarned);
    }

    // @Test
    public void plotPricesAfterTransactions() {
        Date setupDate = new Date(-1000 * 60 * 60 * 24 * 50L);
        Date firstTrans = new Date(0);

        MerchantDAO merchantDao = new MockMerchantDAO();

        StorageBasedMerchant merchant = new StorageBasedMerchant(_cardLibrary, merchantDao, setupDate);

        merchant.cardSold("1_1", firstTrans, 1000);
        merchant.cardBought("1_2", firstTrans, 700);

        long hour = 1000 * 60 * 60;

        System.out.println("-2,1000,700,1000,700");
        for (long time = 0; time < hour * 24 * 35L; time += hour * 2) {
            System.out.println("After "+formatTimeInHours(hour, time) + "," + merchant.getCardSellPrice("1_1", new Date(time)) + "," + merchant.getCardBuyPrice("1_1", new Date(time))
                    + "," + merchant.getCardSellPrice("1_2", new Date(time)) + "," + merchant.getCardBuyPrice("1_2", new Date(time)));
        }
    }

    private String formatTimeInHours(long hour, long time) {
        return (time / (24*hour))+" days, "+((time/hour)%24)+" hours";
    }


    // @Test
    public void plotPriceEasingFromSetup() {
        Date setupDate = new Date(0);

        MerchantDAO merchantDao = new MockMerchantDAO();

        StorageBasedMerchant merchant = new StorageBasedMerchant(_cardLibrary, merchantDao, setupDate);

        long hour = 1000 * 60 * 60;

        for (long time = 0; time < hour * 24 * 35L; time += hour * 2)
            System.out.println("After "+formatTimeInHours(hour, time) + "," + merchant.getCardSellPrice("1_1", new Date(time)) + "," + merchant.getCardBuyPrice("1_1", new Date(time)));
    }

    private void assertEqualsMoreOrLess(int expected, int given) {
        assertTrue("Expected " + expected + " more or less, but received " + given, given <= expected + 1 && given >= expected - 1);
    }
    
}
