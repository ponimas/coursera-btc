import java.util.ArrayList;
import java.util.TreeMap;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    
    // Yeah
    TreeMap<Integer, ArrayList<Block>> chain;

    TxHandler txHandler;
    TransactionPool txPool;

    public BlockChain(Block genesisBlock) {
        chain = new TreeMap<Integer, ArrayList<Block>>();
        
        ArrayList<Block> x = new ArrayList<Block>();
        x.add(genesisBlock);
        chain.put(0, x);

        UTXOPool pool = new UTXOPool();
        txHandler = new TxHandler(pool);

        txPool = new TransactionPool();
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        ArrayList<Block> x = chain.lastEntry().getValue();
        return x.get(0);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return txHandler.getUTXOPool();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        int height = chain.lastKey();
        int minHeight = Integer.max(height - CUT_OFF_AGE, 0);
        boolean found = false;

        
        while (height >= minHeight) {
            if (found) break;
            for (Block b: chain.get(height)) {
                if (b.getHash() == block.getPrevBlockHash()) {
                    found = true;
                    break;
                }
            }
            height--;
        }

        
        if (!found) return false;
        height++;
        ArrayList<Block> x = chain.get(height);
        if (x == null ) x = new ArrayList<Block>();
        
        x.add(block);
        chain.put(height, x);
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
    }
}
