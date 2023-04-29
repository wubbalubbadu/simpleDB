package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.RandomAccessFile;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private File file;
    private TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *          the file that stores the on-disk backing store for this heap
     *          file.
     */
    public HeapFile(File f, TupleDesc td) {
        this.file = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        return this.file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        int offset = BufferPool.getPageSize() * pid.getPageNumber();
        byte[] data = new byte[BufferPool.getPageSize()];
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(this.file, "r");
            raf.seek(offset);
            raf.readFully(data, 0, BufferPool.getPageSize());
            raf.close();
            return new HeapPage((HeapPageId) pid, data);
        } catch (IOException e) {
            return null;
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        return (int) Math.ceil(this.file.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        return new DbFileIterator() {
            private int curPageNum = 0;
            private Iterator<Tuple> curPageTupleIterator = null;
            private boolean isOpen = false;
            PageId curPageId;

            @Override
            public void open() throws DbException, TransactionAbortedException {
                curPageId = new HeapPageId(getId(), curPageNum);
                isOpen = true;
                curPageTupleIterator = ((HeapPage) Database.getBufferPool().getPage(tid, curPageId,
                        Permissions.READ_ONLY)).iterator();
            }

            @Override
            public boolean hasNext() throws DbException, TransactionAbortedException {
                if (!isOpen) {
                    return false;
                }
                if (curPageTupleIterator == null) {
                    return false;
                }
                while (!curPageTupleIterator.hasNext() && curPageNum < numPages() - 1) {
                    curPageNum++;
                    curPageId = new HeapPageId(getId(), curPageNum);
                    curPageTupleIterator = ((HeapPage) Database.getBufferPool().getPage(tid, curPageId,
                            Permissions.READ_ONLY)).iterator();
                }
                return curPageTupleIterator.hasNext();

            }

            @Override
            public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return curPageTupleIterator.next();

            }

            @Override
            public void rewind() throws DbException, TransactionAbortedException {
                close();
                open();
            }

            @Override
            public void close() {
                isOpen = false;
                curPageTupleIterator = null;
            }
        };

    }

}
