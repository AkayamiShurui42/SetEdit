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
import io.github.muntashirakon.setedit.boot.ActionItem;
import io.github.muntashirakon.setedit.boot.BootUtils;
import io.github.muntashirakon.setedit.utils.ActionResult;
import io.github.muntashirakon.setedit.utils.SettingsUtils;

class BootAndLockedRecyclerAdapter extends AbsRecyclerAdapter {
    private static class UnifiedItem {
        boolean isLocked;
        String title;
        String value;
        ActionItem bootItem;
        String lockedKey;
        
        UnifiedItem(boolean isLocked, String title, String value, ActionItem bootItem, String lockedKey) {
            this.isLocked = isLocked;
            this.title = title;
            this.value = value;
            this.bootItem = bootItem;
            this.lockedKey = lockedKey;
        }
    }

    private final List<UnifiedItem> allItems = new ArrayList<>();
    private final List<Integer> matchedIndexes = new ArrayList<>();
    private Filter filter;

    public BootAndLockedRecyclerAdapter(FragmentActivity context) {
        super(context);
        refresh();
    }

    @Override
    public void refresh() {
        allItems.clear();
        
        // Add Boot Items
        for (ActionItem item : BootUtils.getBootItems(context)) {
            allItems.add(new UnifiedItem(false, "[Boot] " + item.table + " – " + item.name, getItemValue(item), item, null));
        }

        // Add Locked Settings
        SharedPreferences lockedPrefs = context.getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = lockedPrefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            allItems.add(new UnifiedItem(true, "[Locked] " + entry.getKey(), String.valueOf(entry.getValue()), null, entry.getKey()));
        }
        
        Collections.sort(allItems, (o1, o2) -> o1.title.compareToIgnoreCase(o2.title));
        filter();
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getAllItems() {
        List<Pair<String, String>> items = new ArrayList<>(allItems.size());
        for (UnifiedItem item : allItems) {
            items.add(new Pair<>(item.title, item.value));
        }
        return items;
    }

    @Override
    public int getListType() {
        return TableTypeInt.TABLE_BOOT_AND_LOCKED;
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
        if (keyName.startsWith("[Locked] ")) {
            String lockedKey = keyName.substring(9);
            try {
                SharedPreferences lockedPrefs = context.getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
                lockedPrefs.edit().putString(lockedKey, newValue).apply();

                int lastColon = lockedKey.lastIndexOf(':');
                if (lastColon > 0 && lastColon < lockedKey.length() - 1) {
                    String key = lockedKey.substring(0, lastColon);
                    String tableType = lockedKey.substring(lastColon + 1);
                    if ("property".equals(tableType)) {
                        io.github.muntashirakon.setedit.utils.AndroidPropertyUtils.update(key, newValue);
                    } else {
                        SettingsUtils.update(context, tableType, key, newValue);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refresh();
    }

    @Override
    public void delete(String keyName) {
        if (keyName.startsWith("[Locked] ")) {
            String lockedKey = keyName.substring(9);
            try {
                SharedPreferences lockedPrefs = context.getSharedPreferences("locked_settings", Context.MODE_PRIVATE);
                lockedPrefs.edit().remove(lockedKey).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (keyName.startsWith("[Boot] ")) {
            String bootTitle = keyName.substring(7);
            String[] pair = bootTitle.split(" – ", 2);
            if (pair.length == 2) {
                BootUtils.delete(context, pair[0], pair[1]);
            }
        }
        refresh();
    }

    @Override
    public Pair<String, String> getItem(int position) {
        if (position >= 0 && position < matchedIndexes.size()) {
            UnifiedItem item = allItems.get(matchedIndexes.get(position));
            return new Pair<>(item.title, item.value);
        }
        return new Pair<>("", "");
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < matchedIndexes.size()) {
            return allItems.get(matchedIndexes.get(position)).title.hashCode();
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        return matchedIndexes.size();
    }

    @Override
    protected Filter getFilter() {
        if (filter == null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<Integer> matchedIndexes = new ArrayList<>(allItems.size());
                    if (TextUtils.isEmpty(constraint)) {
                        for (int i = 0; i < allItems.size(); ++i) matchedIndexes.add(i);
                    } else {
                        for (int i = 0; i < allItems.size(); ++i) {
                            if (allItems.get(i).title.toLowerCase(Locale.ROOT).contains(constraint)) {
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
                    AdapterUtils.notifyDataSetChanged(BootAndLockedRecyclerAdapter.this, BootAndLockedRecyclerAdapter.this.matchedIndexes,
                            (List<Integer>) results.values);
                }
            };
        }
        return filter;
    }

    @NonNull
    private String getItemValue(@NonNull ActionItem actionItem) {
        String action;
        switch (actionItem.action) {
            case ActionResult.TYPE_CREATE:
                action = "CREATE";
                break;
            default:
            case ActionResult.TYPE_UPDATE:
                action = "UPDATE";
                break;
            case ActionResult.TYPE_DELETE:
                return "DELETE";
        }
        return action + " – " + actionItem.value;
    }
}
