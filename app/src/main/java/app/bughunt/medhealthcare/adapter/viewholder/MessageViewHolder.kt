package app.bughunt.medhealthcare.adapter.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.bughunt.medhealthcare.R
import de.hdodenhof.circleimageview.CircleImageView


class MessageViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    var tvMessage: TextView? = null
    var tvName: TextView? = null
    var ivProfile: CircleImageView? = null
    var ivMessage: ImageView? = null

    init {

        tvMessage = itemView?.findViewById(R.id.tv_message) as TextView
        tvName = itemView.findViewById(R.id.tv_name) as TextView
        ivProfile = itemView.findViewById(R.id.iv_profile) as CircleImageView
        ivMessage = itemView.findViewById(R.id.iv_message) as ImageView
    }

}
