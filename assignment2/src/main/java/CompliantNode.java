import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    boolean[] followees;
    Set<Transaction> pending;
    Set<Transaction> valid;
    
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.valid = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pending = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        return this.valid;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate c: candidates) {
            if (!this.pending.add(c.tx)) {
                valid.add(c.tx);
            }
        }

    }
}
