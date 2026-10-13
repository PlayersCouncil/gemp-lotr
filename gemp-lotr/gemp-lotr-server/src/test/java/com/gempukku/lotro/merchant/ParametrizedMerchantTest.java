package com.gempukku.lotro.merchant;

import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.db.MerchantDAO;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertTrue;

public class ParametrizedMerchantTest extends LoadedLibraries {
    @Test
    public void checkTransactionRevertsPriceAfterFiveDays() {
        Date setupDate = new Date(-1000 * 60 * 60 * 24 * 50L);
        Date firstTrans = new Date(0);

        MerchantDAO merchantDao = new MockMerchantDAO();

        ParametrizedMerchant merchant = new ParametrizedMerchant(_cardLibrary);
        merchant.setMerchantDao(merchantDao);
        merchant.setMerchantSetupDate(setupDate);

        merchant.cardSold("1_1", firstTrans, 1000);
        merchant.cardBought("1_2", firstTrans, 1000);

        Date currentTime = new Date(1000 * 60 * 60 * 24 * 5L);

        assertEqualsMoreOrLess(1100, (int) merchant.getCardSellPrice("1_1", firstTrans));
        assertEqualsMoreOrLess(900, (int) merchant.getCardBuyPrice("1_2", firstTrans));

        assertEqualsMoreOrLess(1000, (int) merchant.getCardSellPrice("1_1", currentTime));
        assertEqualsMoreOrLess(1000, (int) merchant.getCardBuyPrice("1_2", currentTime));
    }

    //    @Test
    public void plotPricesAfterTransactions() {
        Date setupDate = new Date(-1000 * 60 * 60 * 24 * 50L);
        Date firstTrans = new Date(0);

        MerchantDAO merchantDao = new MockMerchantDAO();

        ParametrizedMerchant merchant = new ParametrizedMerchant(_cardLibrary);
        merchant.setMerchantDao(merchantDao);
        merchant.setMerchantSetupDate(setupDate);

        merchant.cardSold("1_1", firstTrans, 1000);
        merchant.cardBought("1_2", firstTrans, 700);

        long hour = 1000 * 60 * 60;

        System.out.println("-2,1000,700,1000,700");
        for (long time = 0; time < hour * 24 * 35L; time += hour * 2) {
            System.out.println(time / hour + "," + ((int) merchant.getCardSellPrice("1_1", new Date(time))) + "," + ((int) merchant.getCardBuyPrice("1_1", new Date(time)))
                    + "," + ((int) merchant.getCardSellPrice("1_2", new Date(time))) + "," + ((int) merchant.getCardBuyPrice("1_2", new Date(time))));
        }
    }


    //    @Test
    public void plotPriceEasingFromSetup() {
        Date setupDate = new Date(0);

        MerchantDAO merchantDao = new MockMerchantDAO();

        ParametrizedMerchant merchant = new ParametrizedMerchant(_cardLibrary);
        merchant.setMerchantDao(merchantDao);
        merchant.setMerchantSetupDate(setupDate);

        long hour = 1000 * 60 * 60;

        for (long time = 0; time < hour * 24 * 35L; time += hour * 2)
            System.out.println(time / hour + "," + ((int) merchant.getCardSellPrice("1_1", new Date(time))) + "," + ((int) merchant.getCardBuyPrice("1_1", new Date(time))));
    }

    private void assertEqualsMoreOrLess(int expected, int given) {
        assertTrue("Expected " + expected + " more or less, but received " + given, given <= expected + 1 && given >= expected - 1);
    }
}
