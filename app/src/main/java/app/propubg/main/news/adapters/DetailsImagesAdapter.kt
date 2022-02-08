package app.propubg.main.news.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.databinding.ItemDetailsPageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey

class DetailsImagesAdapter(private val images: ArrayList<String>):
    RecyclerView.Adapter<DetailsImagesAdapter.ItemPageViewHolder>() {

    class ItemPageViewHolder(val binding: ItemDetailsPageBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemPageViewHolder(ItemDetailsPageBinding
                .inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ItemPageViewHolder, position: Int) {

        if (images[position]==""){
            Glide.with(holder.binding.itemImage).load(R.drawable.item_holder_big)
                .into(holder.binding.itemImage)
        } else {
            holder.binding.itemWait.postDelayed({
                Glide.with(holder.binding.itemWait).asGif().load(R.drawable.wait)
                    .into(holder.binding.itemWait)
            }, 200)

            Glide.with(holder.binding.itemImage).load(images[position])
                .addListener(object:RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean): Boolean {
                        holder.binding.itemWait.visibility = View.GONE
                        return false
                    }
                }).signature(ObjectKey(images[position]))
                .into(holder.binding.itemImage)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }
}