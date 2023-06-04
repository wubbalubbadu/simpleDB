package simpledb.transaction;
import simpledb.transaction.TransactionId;
import simpledb.common.Permissions;

public class Lock {
    private TransactionId transactionId;
    private Permissions permission;

    public Lock(TransactionId transactionId, Permissions permission) {
        this.transactionId = transactionId;
        this.permission = permission;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public Permissions getPermission() {
        return permission;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public void setPermission(Permissions permission) {
        this.permission = permission;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Lock)) {
            return false;
        }
        Lock other = (Lock) o;
        return this.transactionId.equals(other.transactionId) && this.permission.equals(other.permission);
    }

    public int hashCode() {
        return transactionId.hashCode() + permission.hashCode();
    }

    public String toString() {
        return "Lock(" + transactionId + ", " + permission + ")";
    }
}
