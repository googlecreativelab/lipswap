package com.androidexperiments.lipflip.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.BaseAdapter;

//import com.nhaarman.listviewanimations.util.Insertable;
//import com.nhaarman.listviewanimations.util.Swappable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Updated with additional {@link #removeAll(Collection)} method, helping to further match
 * ArrayList methods, since removing multiple items from the grid with just remove called
 * multiple notifyDataSetChange's which was no bueno
 */
public abstract class ActualArrayAdapter<T> extends BaseAdapter { //} implements Swappable, Insertable<T> {
    @NonNull
    private final List<T> mItems;
    private BaseAdapter mDataSetChangedSlavedAdapter;

    protected ActualArrayAdapter() {
        this((List) null);
    }

    protected ActualArrayAdapter(@Nullable List<T> objects) {
        if (objects != null) {
            this.mItems = objects;
        } else {
            this.mItems = new ArrayList();
        }

    }

    public int getCount() {
        return this.mItems.size();
    }

    public long getItemId(int position) {
        return (long) position;
    }

    @NonNull
    public T getItem(int position) {
        return this.mItems.get(position);
    }

    @NonNull
    public List<T> getItems() {
        return this.mItems;
    }

    public boolean add(@NonNull T object) {
        boolean result = this.mItems.add(object);
        this.notifyDataSetChanged();
        return result;
    }

    public void add(int index, @NonNull T item) {
        this.mItems.add(index, item);
        this.notifyDataSetChanged();
    }

    public boolean addAll(@NonNull Collection<? extends T> collection) {
        boolean result = this.mItems.addAll(collection);
        this.notifyDataSetChanged();
        return result;
    }

    public boolean contains(T object) {
        return this.mItems.contains(object);
    }

    public void clear() {
        this.mItems.clear();
        this.notifyDataSetChanged();
    }

    public boolean remove(@NonNull Object object) {
        boolean result = this.mItems.remove(object);
        this.notifyDataSetChanged();
        return result;
    }

    @NonNull
    public T remove(int location) {
        T result = this.mItems.remove(location);
        this.notifyDataSetChanged();
        return result;
    }

    public boolean removeAll(@NonNull Collection<?> c) {
        boolean modified = false;
        Iterator<?> e = this.mItems.iterator();
        while (e.hasNext()) {
            if (c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        if(modified)
            this.notifyDataSetChanged();
        return modified;
    }

    public void swapItems(int positionOne, int positionTwo) {
        T firstItem = this.mItems.set(positionOne, this.getItem(positionTwo));
        this.notifyDataSetChanged();
        this.mItems.set(positionTwo, firstItem);
    }

    public void propagateNotifyDataSetChanged(@NonNull BaseAdapter slavedAdapter) {
        this.mDataSetChangedSlavedAdapter = slavedAdapter;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (this.mDataSetChangedSlavedAdapter != null) {
            this.mDataSetChangedSlavedAdapter.notifyDataSetChanged();
        }

    }
}