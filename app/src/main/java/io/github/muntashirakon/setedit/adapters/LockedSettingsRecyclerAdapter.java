package io.github.muntashirakon.setedit.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.muntashirakon.setedit.TableTypeInt;
import io.github.muntashirakon.setedit.utils.SettingsUtils;

class LockedSettingsRecyclerAdapter extends AbsRecyclerAdapter {
    private final List<Pair<String, String>> mLockedItems = new ArrayList<>();
    private final List<Integer> mMatchedIndexes = new ArrayList<>();
    private Filter mFilter;

    public LockedSettingsRecyclerAdapter(FragmentActivity context) {
        super(context);
        refresh();
    }

    @Override
    public void refresh() {
        mLockedItems.clear();
        SharedPreferences lockedPrefs = context.getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = lockedPrefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            mLockedItems.add(new Pair<>(entry.getKey(), String.valueOf(entry.getValue())));
        }
        Collections.sort(mLockedItems, (o1, o2) -> o1.first.compareToIgnoreCase(o2.first));
        filter();
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        return new ArrayList<>(mLockedItems);
    }

    @Override
    public int getListType() {
        return TableTypeInt.TABLE_LOCKED;
    }

    @Override
    public boolean canEdit() {
        return true;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public void update(String keyName, String newValue) {
        try {
            SharedPreferences lockedPrefs = context.getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
            lockedPrefs.edit().putString(keyName, newValue).apply();

            // Also update the actual setting
            int lastColon = keyName.lastIndexOf(':');
            if (lastColon > 0 && lastColon < keyName.length() - 1) {
                String key = keyName.substring(0, lastColon);
                String tableType = keyName.substring(lastColon + 1);
                if ("property".equals(tableType)) {
                    io.github.muntashirakon.setedit.utils.AndroidPropertyUtils.update(key, newValue);
                } else {
                    SettingsUtils.update(context, tableType, key, newValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        refresh();
    }

    @Override
    public void delete(String keyName) {
        try {
            SharedPreferences lockedPrefs = context.getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
            lockedPrefs.edit().remove(keyName).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
        refresh();
    }

    @Override
    public Pair<String, String> getItem(int position) {
        if (position >= 0 && position < mMatchedIndexes.size()) {
            int index = mMatchedIndexes.get(position);
            if (index >= 0 && index < mLockedItems.size()) {
                return mLockedItems.get(index);
            }
        }
        return new Pair<>("", "");
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < mMatchedIndexes.size()) {
            int index = mMatchedIndexes.get(position);
            if (index >= 0 && index < mLockedItems.size()) {
                return mLockedItems.get(index).first.hashCode();
            }
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        return mMatchedIndexes.size();
    }

    @Override
    protected Filter getFilter() {
        if (mFilter == null) {
            mFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(mLockedItems.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < mLockedItems.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < mLockedItems.size(); ++i) {
                            if (mLockedItems.get(i).first.toLowerCase(Locale.ROOT).contains(constraint)) {
                                matchedIndexes.add(i);
                            }
                        }
                    }
                    results.count = matchedIndexes.size();
                    results.values = matchedIndexes;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //noinspection unchecked
                    AdapterUtils.notifyDataSetChanged(LockedSettingsRecyclerAdapter.this, mMatchedIndexes,
                            (List<Integer>) results.values);
                }
            };
        }
        return mFilter;
    }
}
