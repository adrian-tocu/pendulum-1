package net.helix.pendulum.service.tipselection.impl;

import net.helix.pendulum.conf.MainnetConfig;
import net.helix.pendulum.controllers.TransactionViewModel;
import net.helix.pendulum.model.Hash;
import net.helix.pendulum.model.HashId;
import net.helix.pendulum.service.snapshot.SnapshotProvider;
import net.helix.pendulum.service.snapshot.impl.SnapshotProviderImpl;
import net.helix.pendulum.service.tipselection.RatingCalculator;
import net.helix.pendulum.service.tipselection.TailFinder;
import net.helix.pendulum.storage.Tangle;
import net.helix.pendulum.storage.rocksdb.RocksDBPersistenceProvider;
import net.helix.pendulum.utils.collections.interfaces.UnIterableMap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static net.helix.pendulum.TransactionTestUtils.*;


public class WalkerAlphaTest {

    private static final TemporaryFolder dbFolder = new TemporaryFolder();
    private static final TemporaryFolder logFolder = new TemporaryFolder();
    private static Tangle tangle;
    private static SnapshotProvider snapshotProvider;
    private static WalkerAlpha walker;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @AfterClass
    public static void shutdown() throws Exception {
        tangle.shutdown();
        snapshotProvider.shutdown();
        dbFolder.delete();
        logFolder.delete();
    }

    @BeforeClass
    public static void setUp() throws Exception {
        tangle = new Tangle();
        snapshotProvider = new SnapshotProviderImpl().init(new MainnetConfig());
        dbFolder.create();
        logFolder.create();
        tangle.addPersistenceProvider( new RocksDBPersistenceProvider(
                dbFolder.getRoot().getAbsolutePath(), logFolder.getRoot().getAbsolutePath(), 1000,
                Tangle.COLUMN_FAMILIES, Tangle.METADATA_COLUMN_FAMILY));
        tangle.init();

        TailFinder tailFinder = Mockito.mock(TailFinder.class);
        Mockito.when(tailFinder.findTail(Mockito.any(Hash.class)))
                .then(args -> Optional.of(args.getArgumentAt(0, Hash.class)));
        walker = new WalkerAlpha(tailFinder, tangle, new Random(1), new MainnetConfig());
    }


    @Test
    public void walkEndsOnlyInRatingTest() throws Exception {
        //build a small tangle - 1,2,3,4 point to  transaction
        TransactionViewModel transaction, transaction1, transaction2, transaction3, transaction4;
        transaction = new TransactionViewModel(getTransactionBytes(), getTransactionHash());
        transaction1 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction2 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction3 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());

        transaction.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction1.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction2.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction3.store(tangle, snapshotProvider.getInitialSnapshot());

        //calculate rating
        RatingCalculator ratingCalculator = new RatingOne(tangle);
        UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(transaction.getHash());

        //add 4 after the rating was calculated
        transaction4 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction4.store(tangle, snapshotProvider.getInitialSnapshot());

        for (int i=0; i < 100; i++) {
            //select
            Hash tip = walker.walk(transaction.getHash(), rating, (o -> true));

            Assert.assertNotNull(tip);
            //log.info("selected tip: " + tip.toString());
            Assert.assertFalse(transaction4.getHash().equals(tip));
        }
    }

    @Test
    public void showWalkDistributionAlphaHalfTest() throws Exception {
        //build a small tangle - 1,2,3,4 point to  transaction
        TransactionViewModel transaction, transaction1, transaction2, transaction3;
        transaction = new TransactionViewModel(getTransactionBytes(), getTransactionHash());
        transaction1 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction2 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction3 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());

        transaction.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction1.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction2.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction3.store(tangle, snapshotProvider.getInitialSnapshot());

        //calculate rating
        RatingCalculator ratingCalculator = new RatingOne(tangle);
        UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(transaction.getHash());
        //set a higher rate for transaction2
        rating.put(transaction2.getHash(), 10);

        Map<Hash, Integer> counters = new HashMap<>(rating.size());
        int iterations = 100;

        walker.setAlpha(0.3);
        for (int i=0; i < iterations; i++) {
            //select
            Hash tip = walker.walk(transaction.getHash(), rating, (o -> true));

            Assert.assertNotNull(tip);
            counters.put(tip, 1 + counters.getOrDefault(tip, 0));
        }

        for (Map.Entry<Hash, Integer> entry : counters.entrySet()) {
            log.info(entry.getKey().toString() + " : " + entry.getValue());
        }

        Assert.assertTrue(counters.get(transaction2.getHash()) > iterations / 2);
    }

    @Test
    public void showWalkDistributionAlphaZeroTest() throws Exception {
        //build a small tangle - 1,2,3,4 point to  transaction
        TransactionViewModel transaction, transaction1, transaction2, transaction3, transaction4;
        transaction = new TransactionViewModel(getTransactionBytes(), getTransactionHash());
        transaction1 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction2 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction3 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());

        transaction.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction1.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction2.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction3.store(tangle, snapshotProvider.getInitialSnapshot());

        //calculate rating
        RatingCalculator ratingCalculator = new RatingOne(tangle);
        UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(transaction.getHash());
        //set a higher rate for transaction2
        rating.put(transaction2.getHash(), 10);

        //add 4 after the rating was calculated
        transaction4 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction4.store(tangle, snapshotProvider.getInitialSnapshot());

        Map<Hash, Integer> counters = new HashMap<>(rating.size());
        int iterations = 100;

        walker.setAlpha(0);
        for (int i=0; i < iterations; i++) {
            //select
            Hash tip = walker.walk(transaction.getHash(), rating, (o -> true));

            Assert.assertNotNull(tip);
            counters.put(tip, 1 + counters.getOrDefault(tip, 0));
        }

        for (Map.Entry<Hash, Integer> entry : counters.entrySet()) {
            log.info(entry.getKey().toString() + " : " + entry.getValue());
        }

        Assert.assertTrue(counters.get(transaction1.getHash()) > iterations / 6);
    }

    @Test
    public void walkTest() throws Exception {
        //build a small tangle
        TransactionViewModel transaction, transaction1, transaction2, transaction3, transaction4;
        transaction = new TransactionViewModel(getTransactionBytes(), Hash.NULL_HASH);
        transaction1 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction2 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction1.getHash(),
                transaction1.getHash()), getTransactionHash());
        transaction3 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction2.getHash(),
                transaction1.getHash()), getTransactionHash());
        transaction4 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction2.getHash(),
                transaction3.getHash()), getTransactionHash());
        transaction.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction1.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction2.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction3.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction4.store(tangle, snapshotProvider.getInitialSnapshot());

        //calculate rating
        RatingCalculator ratingCalculator = new RatingOne(tangle);
        UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(transaction.getHash());

        //reach the tips
        Hash tip = walker.walk(transaction.getHash(), rating, (o -> true));

        log.info("selected tip: " + tip.toString());
        Assert.assertEquals(tip, transaction4.getHash());
    }

    @Test
    public void diamondWalkTest() throws Exception {
        //build a small tangle
        TransactionViewModel transaction, transaction1, transaction2, transaction3;
        transaction = new TransactionViewModel(getTransactionBytes(), getTransactionHash());
        transaction1 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction2 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction.getHash(),
                transaction.getHash()), getTransactionHash());
        transaction3 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(transaction1.getHash(),
                transaction2.getHash()), getTransactionHash());
        transaction.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction1.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction2.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction3.store(tangle, snapshotProvider.getInitialSnapshot());

        //calculate rating
        RatingCalculator ratingCalculator = new RatingOne(tangle);
        UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(transaction.getHash());

        //reach the tips
        Hash tip = walker.walk(transaction.getHash(), rating, (o -> true));

        log.info("selected tip: " + tip.toString());
        Assert.assertEquals(tip, transaction3.getHash());
    }

    @Test
    public void chainWalkTest() throws Exception {
        //build a small tangle
        TransactionViewModel transaction = new TransactionViewModel(getTransactionBytes(), getTransactionHash());
        TransactionViewModel transaction1 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(
                transaction.getHash(), transaction.getHash()), getTransactionHash());
        TransactionViewModel transaction2 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(
                transaction1.getHash(), transaction1.getHash()), getTransactionHash());
        TransactionViewModel transaction3 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(
                transaction2.getHash(), transaction2.getHash()), getTransactionHash());
        TransactionViewModel transaction4 = new TransactionViewModel(getTransactionBytesWithTrunkAndBranch(
                transaction3.getHash(), transaction3.getHash()), getTransactionHash());
        transaction.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction1.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction2.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction3.store(tangle, snapshotProvider.getInitialSnapshot());
        transaction4.store(tangle, snapshotProvider.getInitialSnapshot());

        //calculate rating
        RatingCalculator ratingCalculator = new RatingOne(tangle);
        UnIterableMap<HashId, Integer> rating = ratingCalculator.calculate(transaction.getHash());

        //reach the tips
        Hash tip = walker.walk(transaction.getHash(), rating, (o -> true));

        log.info("selected tip: " + tip.toString());
        Assert.assertEquals(tip, transaction4.getHash());
    }

}
