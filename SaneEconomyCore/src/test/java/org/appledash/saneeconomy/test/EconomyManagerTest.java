package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.test.mock.MockEconomyStorageBackend;
import org.appledash.saneeconomy.test.mock.MockOfflinePlayer;
import org.appledash.saneeconomy.test.mock.MockSaneEconomy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DecimalFormat;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class EconomyManagerTest {
    private EconomyManager economyManager;

    @Before
    public void setupEconomyManager()  {
        economyManager = new EconomyManager(new MockSaneEconomy(),
                new Currency("test dollar", "test dollars", new DecimalFormat("0.00")),
                new MockEconomyStorageBackend());
    }

    @Test
    public void testEconomyManager() {
        Economable playerOne = Economable.wrap(new MockOfflinePlayer("One"));
        Economable playerTwo = Economable.wrap(new MockOfflinePlayer("Two"));

        // Accounts should not exist
        Assert.assertFalse(economyManager.accountExists(playerOne));
        Assert.assertFalse(economyManager.accountExists(playerTwo));
        Assert.assertEquals(economyManager.getBalance(playerOne), 0.0D, 0.0);
        Assert.assertEquals(economyManager.getBalance(playerTwo), 0.0D, 0.0);

        economyManager.setBalance(playerOne, 100.0D);

        // Now one should have an account, but two should not
        Assert.assertTrue(economyManager.accountExists(playerOne));
        Assert.assertFalse(economyManager.accountExists(playerTwo));

        // One should have balance, two should not
        Assert.assertEquals(economyManager.getBalance(playerOne), 100.0, 0.0);
        Assert.assertEquals(economyManager.getBalance(playerTwo), 0.0, 0.0);

        // One should be able to transfer to two
        Assert.assertTrue(economyManager.transact(new Transaction(playerOne, playerTwo, 50.0, TransactionReason.PLAYER_PAY)).getStatus() == TransactionResult.Status.SUCCESS);

        // One should now have only 50 left, two should have 50 now
        Assert.assertEquals(economyManager.getBalance(playerOne), 50.0, 0.0);
        Assert.assertEquals(economyManager.getBalance(playerTwo), 50.0, 0.0);

        // Ensure that balance addition and subtraction works...
        Assert.assertEquals(economyManager.transact(
                new Transaction(playerOne, Economable.CONSOLE, 25.0, TransactionReason.TEST)
        ).getFromBalance(), 25.0, 0.0);

        Assert.assertEquals(economyManager.transact(
                new Transaction(Economable.CONSOLE, playerOne, 25.0, TransactionReason.TEST)
        ).getToBalance(), 50.0, 0.0);

        Assert.assertEquals(economyManager.transact(
                new Transaction(playerTwo, Economable.CONSOLE, Double.MAX_VALUE, TransactionReason.TEST)
        ).getFromBalance(), 0.0, 0.0);

        // Ensure that hasBalance works
        Assert.assertTrue(economyManager.hasBalance(playerOne, 50.0));
        Assert.assertFalse(economyManager.hasBalance(playerOne, 51.0));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBalance() {
        Economable economable = Economable.wrap(new MockOfflinePlayer("Bob"));
        economyManager.setBalance(economable, -1.0);
    }
}
