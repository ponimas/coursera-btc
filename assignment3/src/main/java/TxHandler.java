import java.util.HashSet;
import java.util.Set;

public class TxHandler {
    public UTXOPool pool;
    
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    
    public TxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
    }

    public UTXOPool getUTXOPool() {
        return pool;
    };
    
    public boolean isValidTx(Transaction tx) {
        Set<UTXO> setUTXO = new HashSet<UTXO>();
        double inputSum = 0;
        double outputSum = 0;
        if (tx.isCoinbase()) return true;
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            byte[] data = tx.getRawDataToSign(i);
            
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);

            // * (3) no UTXO is claimed multiple times by {@code tx}
            if (!setUTXO.add(u)) {
                return false;
            }

            Transaction.Output out = pool.getTxOutput(u);

            // * (1) all outputs claimed by {@code tx} are in the current UTXO pool 
            if (out == null) {
                return false;
            }

            // * (2) the signatures on each input of {@code tx} are valid,
            if (!Crypto.verifySignature(out.address, data, in.signature)) {
                return false;
            }
            // 
            inputSum += out.value;
        }

        for (Transaction.Output out: tx.getOutputs()) {
            // * (4) all of {@code tx}s output values are non-negative, and

            if (out.value < 0) {
                return false;
            }
            outputSum += out.value;
        }
        // * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        if (outputSum > inputSum) {
            return false;
        }

        return true;
    }


    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Set<Transaction> txs = new HashSet<Transaction>();
        Set<Transaction.Input> inputs = new HashSet<Transaction.Input>();
        
        for (Transaction tx: possibleTxs) {
            if (!isValidTx(tx)) {
                continue;
            }
            
            for (int i = 0; i < tx.numInputs(); i++) {
                Transaction.Input in = tx.getInput(i);
                UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
                pool.removeUTXO(u);
            }

            for (int i = 0; i < tx.numOutputs(); i++) {
                Transaction.Output out = tx.getOutput(i);
                UTXO u = new UTXO(tx.getHash(), i);
                pool.addUTXO(u, out);
            }

            txs.add(tx);
        }

        
        return txs.toArray(new Transaction[txs.size()]);
    }

}
