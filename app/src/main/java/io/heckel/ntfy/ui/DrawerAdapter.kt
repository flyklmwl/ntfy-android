package io.heckel.ntfy.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.heckel.ntfy.R
import io.heckel.ntfy.db.Subscription

/**
 * Data items shown in the navigation drawer.
 */
sealed class DrawerItem {
    /** Drawer header (title + subtitle). */
    object Header : DrawerItem()

    /** "All subscriptions" entry — navigates to the unfiltered main list. */
    data class AllSubscriptions(val count: Int) : DrawerItem()

    /** A category group that can be expanded/collapsed. */
    data class CategoryGroup(
        val name: String,
        val subscriptions: List<Subscription>,
        val expanded: Boolean
    ) : DrawerItem()

    /** A single subscription entry inside a category. */
    data class SubscriptionEntry(val subscription: Subscription) : DrawerItem()
}

/**
 * RecyclerView adapter for the navigation drawer.
 *
 * View types:
 *   - HEADER: title bar at the top
 *   - ALL_SUBSCRIBED: "全部订阅 (N)" shortcut
 *   - CATEGORY: expandable category group header
 *   - SUBSCRIPTION: individual subscription item
 */
class DrawerAdapter(
    private val onAllSubscriptionsClick: () -> Unit,
    private val onSubscriptionClick: (Subscription) -> Unit,
    private val onCategoryToggle: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ALL_SUBS = 1
        const val VIEW_TYPE_CATEGORY = 2
        const val VIEW_TYPE_SUBSCRIPTION = 3
    }

    private val items = mutableListOf<DrawerItem>()

    fun submitList(newItems: List<DrawerItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is DrawerItem.Header -> VIEW_TYPE_HEADER
        is DrawerItem.AllSubscriptions -> VIEW_TYPE_ALL_SUBS
        is DrawerItem.CategoryGroup -> VIEW_TYPE_CATEGORY
        is DrawerItem.SubscriptionEntry -> VIEW_TYPE_SUBSCRIPTION
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.nav_drawer_item_header, parent, false)
            )
            VIEW_TYPE_ALL_SUBS -> AllSubsViewHolder(
                inflater.inflate(R.layout.nav_drawer_item_subscription, parent, false)
            )
            VIEW_TYPE_CATEGORY -> CategoryViewHolder(
                inflater.inflate(R.layout.nav_drawer_item_category, parent, false)
            )
            VIEW_TYPE_SUBSCRIPTION -> SubscriptionViewHolder(
                inflater.inflate(R.layout.nav_drawer_item_subscription, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderViewHolder -> { /* static content from XML */ }
            is AllSubsViewHolder -> {
                val all = item as DrawerItem.AllSubscriptions
                holder.bind(all.count, onAllSubscriptionsClick)
            }
            is CategoryViewHolder -> {
                val cat = item as DrawerItem.CategoryGroup
                holder.bind(cat, onCategoryToggle)
            }
            is SubscriptionViewHolder -> {
                val sub = item as DrawerItem.SubscriptionEntry
                holder.bind(sub.subscription, onSubscriptionClick)
            }
        }
    }

    // ---- ViewHolders ----

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class AllSubsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.drawer_sub_icon)
        private val name: TextView = view.findViewById(R.id.drawer_sub_name)
        private val unread: TextView = view.findViewById(R.id.drawer_sub_unread)

        fun bind(count: Int, onClick: () -> Unit) {
            name.text = itemView.context.getString(R.string.drawer_all_subscriptions)
            unread.text = count.toString()
            unread.visibility = View.VISIBLE
            itemView.setOnClickListener { onClick() }
        }
    }

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.drawer_category_name)
        private val count: TextView = view.findViewById(R.id.drawer_category_count)
        private val expandIcon: ImageView = view.findViewById(R.id.drawer_category_expand_icon)

        fun bind(category: DrawerItem.CategoryGroup, onToggle: (String) -> Unit) {
            name.text = category.name
            count.text = category.subscriptions.size.toString()
            expandIcon.rotation = if (category.expanded) 0f else -90f
            itemView.setOnClickListener { onToggle(category.name) }
        }
    }

    class SubscriptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.drawer_sub_name)

        fun bind(sub: Subscription, onClick: (Subscription) -> Unit) {
            name.text = sub.displayName ?: sub.topic
            itemView.setOnClickListener { onClick(sub) }
        }
    }
}
