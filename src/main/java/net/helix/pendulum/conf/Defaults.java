package net.helix.pendulum.conf;

import net.helix.pendulum.model.Hash;
import net.helix.pendulum.model.HashFactory;
import net.helix.pendulum.utils.PendulumUtils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface Defaults {
    //API
    int API_PORT = 8085;
    String API_HOST = "localhost";
    List<String> REMOTE_LIMIT_API = PendulumUtils.createImmutableList(); // "addNeighbors", "getNeighbors", "removeNeighbors", "attachToTangle", "interruptAttachingToTangle" <- TODO: limit these in production!
    InetAddress REMOTE_LIMIT_API_DEFAULT_HOST = InetAddress.getLoopbackAddress();
    List<InetAddress> REMOTE_LIMIT_API_HOSTS = PendulumUtils.createImmutableList(REMOTE_LIMIT_API_DEFAULT_HOST);
    int MAX_FIND_TRANSACTIONS = 100_000;
    int MAX_REQUESTS_LIST = 1_000;
    int MAX_GET_TRANSACTION_STRINGS = 10_000;
    int MAX_BODY_LENGTH = 1_000_000;
    String REMOTE_AUTH = "";
    boolean IS_POW_DISABLED = false;

    //Network
    int UDP_RECEIVER_PORT = 4100;
    int TCP_RECEIVER_PORT = 5100;
    double P_REMOVE_REQUEST = 0.01d;
    int SEND_LIMIT = -1;
    int MAX_PEERS = 0;
    boolean DNS_REFRESHER_ENABLED = true;
    boolean DNS_RESOLUTION_ENABLED = true;

    //XI
    String XI_DIR = "modules";

    //DB
    String DB_PATH = "mainnetdb";
    String DB_LOG_PATH = "mainnet.log";
    int DB_CACHE_SIZE = 100_000;
    String ROCKS_DB = "rocksdb";
    boolean REVALIDATE = false;
    boolean RESCAN_DB = false;

    //Protocol
    double P_REPLY_RANDOM_TIP = 0.66d;
    double P_DROP_TRANSACTION = 0d;
    double P_SELECT_MILESTONE_CHILD = 0.7d;
    double P_SEND_MILESTONE = 0.02d;
    double P_PROPAGATE_REQUEST = 0.01d;
    int MWM = 1;
    int PACKET_SIZE = 800;
    int REQ_HASH_SIZE = 32;
    int QUEUE_SIZE = 1_000;
    double P_DROP_CACHE_ENTRY = 0.02d;
    int CACHE_SIZE_BYTES = 150_000;

    //Zmq
    int ZMQ_THREADS = 1;
    boolean ZMQ_ENABLE_IPC = false;
    String ZMQ_IPC = "ipc://hlx";
    boolean ZMQ_ENABLE_TCP = false;
    int ZMQ_PORT = 5556;

    //TipSel
    int MAX_DEPTH = 15;
    double ALPHA = 0.001d;

    //Tip solidification
    boolean TIP_SOLIDIFIER_ENABLED = true;

    //PoW
    int POW_THREADS = 8;

    //Resource directory:
    String RESOUCER_PATH = "./src/main/resources";
    String DEFAULT_RESOUCE_PATH = "./resources";

    //Validator Manager
    boolean VALIDATOR_MANAGER_ENABLED = false;
    Hash VALIDATOR_MANAGER_ADDRESS = HashFactory.ADDRESS.create("9474289ae28f0ea6e3b8bedf8fc52f14d2fa9528a4eb29d7879d8709fd2f6d37");
    int UPDATE_VALIDATOR_DELAY = 30000;
    int START_ROUND_DELAY = 2;
    String VALIDATOR_MANAGER_KEYFILE = "/ValidatorManager.key";
    int VALIDATOR_MANAGER_KEY_DEPTH = 15;
    int VALIDATOR_MANAGER_SECURITY = 2;

    //Milestone
    String VALIDATOR = null;
    Set<Hash> INITIAL_VALIDATORS = new HashSet<>(Arrays.asList(
            HashFactory.ADDRESS.create("eb0d925c1cfa4067db65e4b93fa17d451120cc5a719d637d44a39a983407d832"),
            HashFactory.ADDRESS.create("a5afe01e64ae959f266b382bb5927fd07b49e7e3180239535126844aaae9bf93"),
            HashFactory.ADDRESS.create("e2debe246b5d1a6e05b57b0fc14edb51d136966a91a803b523586ad032f72f3d"),
            HashFactory.ADDRESS.create("1895a039c85b9a5c4e822c8fc51884aedecddfa09daccef642fff697157657b4"),
            HashFactory.ADDRESS.create("1895a039c85b9a5c4e822c8fc51884aedecddfa09daccef642fff697157657b4"),
            HashFactory.ADDRESS.create("1c6b0ee311a7ddccf255c1097995714b285cb06628be1cef2080b0bef7700e12"),
            HashFactory.ADDRESS.create("eb0d925c1cfa4067db65e4b93fa17d451120cc5a719d637d44a39a983407d832")
    ));

    long GENESIS_TIME = 1569024001000L;
    long GENESIS_TIME_TESTNET = 1568725976628L; //TODO: testnet flag should use this time.
    int ROUND_DURATION = 15000;
    int ROUND_PAUSE = 5000;
    String VALIDATOR_KEYFILE = "/Validator.key";
    int MILESTONE_KEY_DEPTH = 10;
    int VALIDATOR_SECURITY = 2;

    //Snapshot
    boolean LOCAL_SNAPSHOTS_ENABLED = true;
    boolean LOCAL_SNAPSHOTS_PRUNING_ENABLED = true;
    int LOCAL_SNAPSHOTS_PRUNING_DELAY = 50000;
    int LOCAL_SNAPSHOTS_INTERVAL_SYNCED = 10;
    int LOCAL_SNAPSHOTS_INTERVAL_UNSYNCED = 1000;
    String LOCAL_SNAPSHOTS_BASE_PATH = "mainnet";
    int LOCAL_SNAPSHOTS_DEPTH = 100;
    String SNAPSHOT_FILE = "/snapshotMainnet.txt";
    String SNAPSHOT_SIG_FILE = "/snapshotMainnet.sig";
    String PREVIOUS_EPOCHS_SPENT_ADDRESSES_TXT = "/previousEpochsSpentAddresses.txt";
    String PREVIOUS_EPOCHS_SPENT_ADDRESSES_SIG = "/previousEpochsSpentAddresses.sig";
    long GLOBAL_SNAPSHOT_TIME = 1522235533L;
    int MILESTONE_START_INDEX = 0;
    int NUM_KEYS_IN_MILESTONE = 10;
    int MAX_ANALYZED_TXS = 20_000;

    //Logging
    boolean SAVELOG_ENABLED = false;
    String SAVELOG_BASE_PATH = "logs/";
    String SAVELOG_XML_FILE = "/logback-save.xml";

    //Spammer
    int SPAM_DELAY = 0;
}