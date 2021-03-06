import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import java.util.stream.*;


public class MaxFeeTxHandler {
    public UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    public MaxFeeTxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
    }

    private double sumOfOutputs(Transaction tx) {
        double sum = 0.0;
        for (Transaction.Output out: tx.getOutputs()) {
            sum += out.value;
        }
        return sum;
    }

    private double sumOfInputs(Transaction tx) {
        double sum = 0.0;
        for (Transaction.Input in: tx.getInputs()) {
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output out = pool.getTxOutput(u);
            if (out != null) {
                sum += out.value;
            }
        }
        return sum;

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
        List<Transaction> txsList = new ArrayList<Transaction>(Arrays.asList(possibleTxs));

        Queue<List<Transaction>> pq = new PriorityQueue<>((List<Transaction> txl1, List<Transaction> txl2) -> {
                double s1 = txl1.stream().mapToDouble((Transaction tx) -> sumOfInputs(tx) - sumOfOutputs(tx)).sum();
                double s2 = txl1.stream().mapToDouble((Transaction tx) -> sumOfInputs(tx) - sumOfOutputs(tx)).sum();
                return -Double.compare(s1, s2);
        });

        Map<UTXO, Transaction> txm = new HashMap<>();
        
        for (Transactoin tx: possibleTxs) {
            for (Transaction.Input in: tx.getInputs()) {
                UTXO u = new UTXO(in.prevTxHash, in.outputIndex);

                Transaction.Output out = pool.getTxOutput(u);
                if (out == null) {
                    txm.add(u, tx);
                }
            }
        }

        for (Transactoin tx: possibleTxs) {

        }


        Set<Transaction> txs = new HashSet<Transaction>();

        for (Transaction tx: txsList) {
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
