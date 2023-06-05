package simpledb.transaction;

import simpledb.common.Permissions;
import simpledb.storage.PageId;
import simpledb.transaction.TransactionId;
import simpledb.transaction.Lock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LockManager {
    private ConcurrentHashMap<PageId, List<Lock>> lockTable;
    private ConcurrentHashMap<PageId, List<Lock>> waitTable;
    private ConcurrentHashMap<TransactionId, List<PageId>> transactionTable;

    public LockManager() {
        lockTable = new ConcurrentHashMap<>();
        waitTable = new ConcurrentHashMap<>();
        transactionTable = new ConcurrentHashMap<>();
    }

    public synchronized boolean acquireSharedLock(TransactionId tid, PageId pid){
        // System.out.println("acquireSharedLock");
        if (!lockTable.containsKey(pid)) {
            lockTable.put(pid, new ArrayList<>());
        }
        List<Lock> locks = lockTable.get(pid);
        for (Lock lock : locks) {
            // check if there is an exclusive lock
            if (lock.getPermission().equals(Permissions.READ_WRITE)) {
                // if there is an exclusive lock, check if it is the same transaction
                if (lock.getTransactionId().equals(tid)) {
                    // if it is the same transaction, return true
                    return true;
                } else {
                    // if it is not the same transaction, return false
                    Lock pendingLock = new Lock(tid, Permissions.READ_ONLY);
                    if (!waitTable.containsKey(pid)) {
                        waitTable.put(pid, new ArrayList<>());
                    }
                    if (!waitTable.get(pid).contains(pendingLock)) {
                        waitTable.get(pid).add(pendingLock);
                    }
                    return false;
                }
            }
        }
        // if there is no exclusive lock, add a shared lock
        Lock newLock = new Lock(tid, Permissions.READ_ONLY);
        locks.add(newLock);
        lockTable.put(pid, locks);
        if (!transactionTable.containsKey(tid)) {
            transactionTable.put(tid, new ArrayList<>());
        }
        transactionTable.get(tid).add(pid);
        // remove from waitTable if it is in waitTable
        if (waitTable.containsKey(pid)) {
            waitTable.get(pid).remove(newLock);
        }
        return true;   
    }

    public synchronized boolean acquireExclusiveLock(TransactionId tid, PageId pid){
        // System.out.println("acquireExclusiveLock");
        if (!lockTable.containsKey(pid)) {
            lockTable.put(pid, new ArrayList<>());
        }
        List<Lock> locks = lockTable.get(pid);
        for (Lock lock : locks) {
            // if there is any lock that is not the same transaction, return false
            if (!lock.getTransactionId().equals(tid)) {
                Lock pendingLock = new Lock(tid, Permissions.READ_WRITE);
                if (!waitTable.containsKey(pid)) {
                    waitTable.put(pid, new ArrayList<>());
                }
                // add to waitTable if it is not in waitTable
                if (!waitTable.get(pid).contains(pendingLock)) {
                    waitTable.get(pid).add(pendingLock);
                }
                return false;
            } 
            else if (lock.getPermission().equals(Permissions.READ_WRITE)){
                return true;
            }
        }

        Lock newLock = new Lock(tid, Permissions.READ_WRITE);
        locks.add(newLock);
        lockTable.put(pid, locks);
        if (!transactionTable.containsKey(tid)) {
            transactionTable.put(tid, new ArrayList<>());
        }
        transactionTable.get(tid).add(pid);
        // remove from waitTable if it is in waitTable
        if (waitTable.containsKey(pid)) {
            waitTable.get(pid).remove(newLock);
        }
        return true;   
    }


    // release lock for a specific transaction on a specific page
    public synchronized boolean releaseLock(TransactionId tid, PageId pid){
        List<Lock> locks = lockTable.get(pid);
        if (locks == null) {
            return false;
        }
        for (Lock lock : locks) {
            if (lock.getTransactionId().equals(tid)) {
                locks.remove(lock);
                break;
            }
        }
        lockTable.put(pid, locks);

        // check if there is any lock in waitTable
        if (waitTable.containsKey(pid)) {
            List<Lock> pendingLocks = waitTable.get(pid);
            for (Lock pendingLock : pendingLocks) {
                if (pendingLock.getTransactionId().equals(tid)) {
                    if (pendingLock.getPermission().equals(Permissions.READ_ONLY)) {
                        acquireSharedLock(tid, pid);
                    } else {
                        acquireExclusiveLock(tid, pid);
                        break;
                    }
                }
            }
        }

        if (locks.size() == 0) {
            lockTable.remove(pid);
        }

        // remove from transactionTable
        List<PageId> dirtyPages = transactionTable.get(tid);
        if (dirtyPages != null) {
            dirtyPages.remove(pid);
        }
        transactionTable.put(tid, dirtyPages);
        return true;
    }

    // release all locks for a specific transaction
    public synchronized boolean releaseAllLocks(TransactionId tid){
        List<PageId> dirtyPages = transactionTable.get(tid);
        if (dirtyPages == null) {
            return false;
        }
        List<PageId> tempDirtyPages = new ArrayList<>(dirtyPages); // create a copy of dirtyPages
        for (PageId pid : tempDirtyPages) {
            releaseLock(tid, pid);
        }
        return true;
    }

    /**
     * Return true if the specified transaction has a lock on the specified page
     */
    public synchronized boolean holdsLock(TransactionId tid, PageId pid){
        // check if the transaction has a lock on the page
        return transactionTable.containsKey(tid) && transactionTable.get(tid).contains(pid);
    }

    public synchronized boolean isPageLocked(PageId pid){
        return lockTable.containsKey(pid) && lockTable.get(pid).size() > 0;
    }

    // return dirty pages for a specific transaction
    public synchronized List<PageId> getDirtyPages(TransactionId tid){
        return transactionTable.get(tid);
    }

}
