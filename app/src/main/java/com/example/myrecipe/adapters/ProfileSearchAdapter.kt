import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.databinding.PopulerMealItemBinding
import com.example.myrecipe.databinding.ProfileSearchItemBinding
import com.example.myrecipe.pojo.FirebaseMeal
import com.example.myrecipe.pojo.Meal
import com.example.myrecipe.pojo.User

class ProfileSearchAdapter() : RecyclerView.Adapter<ProfileSearchAdapter.ProfileSearchViewHolder>() {
    private var userList = ArrayList<User>()

    var onItemClick : ((User)->Unit)? = null

    fun setUsers(userList:ArrayList<User>){
        this.userList=userList
        notifyDataSetChanged()
    }
    class ProfileSearchViewHolder( val binding: ProfileSearchItemBinding):RecyclerView.ViewHolder(binding.root)


    // ViewHolder'ı oluşturmak için kullanılan yöntem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileSearchViewHolder {

        return   ProfileSearchViewHolder(ProfileSearchItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    // Veri bağlama işleminin gerçekleştirildiği yöntem
    override fun onBindViewHolder(holder: ProfileSearchViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(userList[position].pp)
            .into(holder.binding.profileImg)

        holder.binding.nameTextView.text=userList[position].name
        holder.binding.usernameTextView.text=userList[position].username

        holder.itemView.setOnClickListener{
            onItemClick!!.invoke(userList[position])
        }

    }

    // Veri kümesinin boyutunu döndüren yöntem
    override fun getItemCount(): Int {
        return userList.size
    }
}