import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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

    public boolean isValidTx(Transaction tx) {
        Set<UTXO> setUTXO = new HashSet<UTXO>();
        double inputSum = 0;
        double outputSum = 0;
        
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
        List<Transaction> txs = new ArrayList<Transaction>();

        for (Transaction tx: possibleTxs) {
            if (isValidTx(tx)) {
                txs.add(tx);
            }
        }

        
        return txs.toArray(new Transaction[txs.size()]);
    }

}
