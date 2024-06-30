import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.R
import com.example.myrecipe.activities.OtherProfile
import com.example.myrecipe.activities.OtherUserActivity
import com.example.myrecipe.pojo.Comment
import com.google.firebase.auth.FirebaseAuth

class OtherUserCommentsAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<OtherUserCommentsAdapter.CommentViewHolder>() {
    lateinit var onItemClick:((Comment)->Unit)

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
        val commentImageView: ImageView = itemView.findViewById(R.id.Commentimg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentComment = comments[position]
        holder.usernameTextView.text = currentComment.mealName
        holder.commentTextView.text = currentComment.comment
        currentComment.mealThubm?.let { url ->
            Glide.with(holder.itemView.context)
                .load(url)
                .into(holder.commentImageView)
        }
        holder.itemView.setOnClickListener{
            onItemClick.invoke(comments[position])
        }
    }

    override fun getItemCount() = comments.size
}
