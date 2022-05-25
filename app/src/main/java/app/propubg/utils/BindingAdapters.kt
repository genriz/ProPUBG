package app.propubg.utils

import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.text.util.Linkify
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.login.model.user
import app.propubg.main.broadcasts.model.broadcast
import app.propubg.main.content.model.content
import app.propubg.main.menu.model.partner
import app.propubg.main.menu.model.resultsOfTournament
import app.propubg.main.news.model.news
import app.propubg.main.news.model.reshuffle
import app.propubg.main.tournaments.model.tournament
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import java.io.InputStream


@BindingAdapter("setImageSvg")
fun setImageSvg (view: ImageView, id: String){
    val imageStream: InputStream = view.resources.openRawResource(id.toInt())
    val bitmap = BitmapFactory.decodeStream(imageStream)
    view.setImageBitmap(bitmap)
}

@BindingAdapter("setImageNews")
fun setImageNews (view: CardView, news: news?){
    news?.let {
        val waitImage = view.findViewById<ImageView>(R.id.newsImageWait)
        val image = view.findViewById<ImageView>(R.id.newsImage)
        if (currentLanguage == "ru"&&news.imageSrc_ru.isNotEmpty()) {
            news.imageSrc_ru[0]?.let {
                if (news.imageSrc_ru[0]!="") {
                    waitImage.isVisible = true
                    waitImage.post {
                        Glide.with(view).asGif().load(R.drawable.wait)
                            .into(waitImage)
                    }
                    Glide.with(view).load(it)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                waitImage.visibility = View.GONE
                                return false
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(ObjectKey(it))
                        .into(image)
                } else {
                    waitImage.isVisible = false
                    Glide.with(view).load(R.drawable.item_holder)
                        .into(image)
                }
            }
        }
        if (currentLanguage == "en"&&news.imageSrc_en.isNotEmpty()) {
            news.imageSrc_en[0]?.let {
                if (news.imageSrc_en[0]!="") {
                    waitImage.post {
                        Glide.with(view).asGif().load(R.drawable.wait)
                            .into(waitImage)
                    }
                    Glide.with(view).load(it)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                waitImage.visibility = View.GONE
                                return false
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(ObjectKey(it))
                        .into(image)
                } else Glide.with(view).load(R.drawable.item_holder)
                    .into(image)
            }
        }
    }
}

@BindingAdapter("setDateItem")
fun setDateItem (view: TextView, date: Date?){
    date?.let{
        if (DateUtils.isToday(date.time)){
            if (System.currentTimeMillis()-date.time<3600000){
                view.text = view.context.getString(R.string.less_hour)
            } else {
                view.text = DateUtils.getRelativeTimeSpanString(date.time,
                    System.currentTimeMillis(), 3600000)
            }
        } else if (DateUtils.isToday(date.time + DateUtils.DAY_IN_MILLIS)){
            view.text = view.context.getString(R.string.yesterday)
        } else {
            view.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(date)
        }
    }
}

@BindingAdapter("setTitleNews")
fun setTitleNews (view: TextView, news: news?){
    news?.let {
        if (currentLanguage == "ru") view.text = news.title_ru
        else view.text = news.title_en
    }
}

@BindingAdapter("setTextNews")
fun setTextNews (view: TextView, news: news?){
    news?.let {
        if (currentLanguage == "ru") {
            news.text_ru?.let {
                if (news.text_ru != "")
                    view.text = news.text_ru
                else view.visibility = View.GONE
            }
        } else {
            news.text_en?.let {
                if (news.text_en != "")
                    view.text = news.text_en
                else view.visibility = View.GONE
            }
        }
        Linkify.addLinks(view, Linkify.ALL)
    }
}

@BindingAdapter("setImageReshuffles")
fun setImageReshuffles (view: CardView, reshuffle: reshuffle?){
    reshuffle?.let {
        val waitImage = view.findViewById<ImageView>(R.id.reshuffleImageWait)
        val image = view.findViewById<ImageView>(R.id.reshuffleImage)
        if (currentLanguage == "ru" && reshuffle.imageSrc_ru.isNotEmpty()) {
            reshuffle.imageSrc_ru[0]?.let {
                if (reshuffle.imageSrc_ru[0]!="") {
                    waitImage.isVisible = true
                    waitImage.post {
                        Glide.with(view).asGif().load(R.drawable.wait)
                            .into(waitImage)
                    }
                    Glide.with(view).load(it)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                waitImage.visibility = View.GONE
                                return false
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(ObjectKey(it))
                        .into(image)
                } else {
                    waitImage.isVisible = false
                    Glide.with(view).load(R.drawable.item_holder)
                        .into(image)
                }
            }
        }
        if (currentLanguage == "en" && reshuffle.imageSrc_en.isNotEmpty()) {
            reshuffle.imageSrc_en[0]?.let {
                if (reshuffle.imageSrc_en[0]!="") {
                    waitImage.isVisible = true
                    waitImage.post {
                        Glide.with(view).asGif().load(R.drawable.wait)
                            .into(waitImage)
                    }
                    Glide.with(view).load(it)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                waitImage.visibility = View.GONE
                                return false
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(ObjectKey(it))
                        .into(image)
                } else {
                    waitImage.isVisible = false
                    Glide.with(view).load(R.drawable.item_holder)
                        .into(image)
                }
            }
        }
    }
}

@BindingAdapter("setTitleReshuffles")
fun setTitleReshuffles (view: TextView, reshuffle: reshuffle?){
    reshuffle?.let {
        if (currentLanguage == "ru") view.text = reshuffle.title_ru
        else view.text = reshuffle.title_en
    }
}

@BindingAdapter("setTextReshuffle")
fun setTextReshuffle (view: TextView, reshuffle: reshuffle?){
    reshuffle?.let{
        if (currentLanguage == "ru") {
            reshuffle.text_ru?.let{
                if (reshuffle.text_ru!="")
                    view.text = reshuffle.text_ru
                else view.visibility = View.GONE
            }
        }
        else {
            reshuffle.text_en?.let{
                if (reshuffle.text_en!="")
                    view.text = reshuffle.text_en
                else view.visibility = View.GONE
            }
        }
        Linkify.addLinks(view, Linkify.ALL)
    }
}

@BindingAdapter("setImageTournament")
fun setImageTournament (view: CardView, tournament: tournament?){
    tournament?.let {
        val waitImage = view.findViewById<ImageView>(R.id.tournamentImageWait)
        val image = view.findViewById<ImageView>(R.id.tournamentImage)
        tournament.imageSrc[0]?.let {
            if (tournament.imageSrc[0]!=""){
                waitImage.isVisible = true
                waitImage.post {
                    Glide.with(view).asGif().load(R.drawable.wait)
                        .into(view.findViewById(R.id.tournamentImageWait))
                }
                Glide.with(view).load(it)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            waitImage.visibility = View.GONE
                            return false
                        }
                    })
                    .signature(ObjectKey(it))
                    .into(image)
            } else {
                waitImage.isVisible = false
                Glide.with(view).load(R.drawable.item_holder)
                    .into(image)
            }
        }
    }
}

@BindingAdapter("setVerificationTournament")
fun setVerificationTournament (view: ImageView, tournament: tournament?){
    tournament?.let {
        when (tournament.verification) {
            "approved" -> view.setImageResource(R.drawable.ic_verification_approved)
            "attention" -> view.setImageResource(R.drawable.ic_verification_attention)
            "scam" -> view.setImageResource(R.drawable.ic_verification_scam)
        }
    }
}

@BindingAdapter("setTextWillClose")
fun setTextWillClose (view: TextView, tournament: tournament?){
    tournament?.let {
        if (tournament.status == "Closed") {
            view.text = view.context.getString(R.string.registration_closed)
        } else {
            if (currentLanguage == "ru") view.text = tournament.willClose_ru
            else view.text = tournament.willClose_en
        }
    }
}

@BindingAdapter("setTextTournament")
fun setTextTournament (view: TextView, tournament: tournament?){
    tournament?.let {
        if (currentLanguage == "ru") view.text = tournament.text_ru
        else view.text = tournament.text_en
        Linkify.addLinks(view, Linkify.ALL)
    }
}

@BindingAdapter("setTextTournamentVisibility")
fun setTextTournamentVisibility (view: LinearLayout, tournament: tournament?){
    tournament?.let {
        val text = if (currentLanguage == "ru") tournament.text_ru ?: ""
        else tournament.text_en ?: ""
        if (text.isEmpty()) view.visibility = View.GONE
        else view.visibility = View.VISIBLE
    }
}

@BindingAdapter("setTextBtnTournament")
fun setTextBtnTournament (view: TextView, tournament: tournament?){
    tournament?.let {
        when (tournament.status) {
            "Open" -> view.text = view.context.getString(R.string.btn_tournament_register)
            "Closed" -> view.text = view.context.getString(R.string.btn_tournament_more)
            "Upcoming" -> view.text = view.context.getString(R.string.btn_tournament_know)
        }
    }
}

@BindingAdapter(value = ["prizePool", "currency"])
fun setTextPrize (view: TextView, prizePool: Long?, currency: String?){
    var prizeString = ""
    currency?.let{ currency_ ->
        prizeString = Currency.getInstance(currency_).symbol
    }
    prizePool?.let{prize->
        prizeString += String.format("%,d", prize)
//        if (prizeString.contains(","))
//            prizeString.replace(",",".")
    }
    view.text = prizeString
}

@BindingAdapter("setBroadcastStage")
fun setBroadcastStage (view: TextView, broadcast: broadcast){
    if (currentLanguage=="ru") view.text = broadcast.stage_ru
    else view.text = broadcast.stage_en
}

@BindingAdapter("setImageBroadcast")
fun setImageBroadcast (view: RelativeLayout, broadcast: broadcast){
    val waitImage = view.findViewById<ImageView>(R.id.broadcastWait)
    val image = view.findViewById<ImageView>(R.id.broadcastImage)
    if (broadcast.imageSrc!=null&&broadcast.imageSrc!="") {
        waitImage.isVisible = true
        waitImage.post {
            Glide.with(view).asGif().load(R.drawable.wait)
                .into(waitImage)
        }
        Glide.with(view).load(broadcast.imageSrc)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    waitImage.visibility = View.GONE
                    return false
                }
            })
            .into(image)
    } else {
        waitImage.isVisible = false
        Glide.with(view).load(R.drawable.app_logo)
            .into(image)
    }
}

@BindingAdapter("setImageContent")
fun setImageContent (view: RelativeLayout, content: content){
    val waitImage = view.findViewById<ImageView>(R.id.contentWait)
    val image = view.findViewById<ImageView>(R.id.contentImage)
    if (content.imageSrc!=null&&content.imageSrc!=""){
        waitImage.isVisible = true
        waitImage.post {
            Glide.with(view).asGif().load(R.drawable.wait)
                .into(waitImage)
        }
        Glide.with(view).load(content.imageSrc)
            .addListener(object: RequestListener<Drawable> {
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
                    waitImage.visibility = View.GONE
                    return false
                }
            })
            .into(image)
    } else {
        waitImage.isVisible = false
        Glide.with(view).load(R.drawable.app_logo)
            .into(image)
    }

}

@BindingAdapter("setTitleContent")
fun setTitleContent (view: TextView, content: content){
    if (currentLanguage=="ru") view.text = content.title_ru
    else view.text = content.title_en
}

@BindingAdapter("setImageAuthor")
fun setImageAuthor (view: ImageView, content: content){
    Glide.with(view).load(content.imageSrc).into(view)
}

@BindingAdapter("setBtnTeamsBroadcast")
fun setBtnTeamsBroadcast(button: MaterialButton, broadcast: broadcast){
    if (broadcast.teamsList.size>0){
        button.isEnabled = true
        button.text = button.context.getString(R.string.teams_list)
        button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    } else {
        button.isEnabled = false
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        button.text = button.context.getString(R.string.teams_list_na)
        button.setTextColor(ContextCompat.getColor(button.context, R.color.text_gray2))
    }
}

@BindingAdapter("setTxtMoreBroadcast")
fun setTxtMoreBroadcast(view:TextView, broadcast: broadcast){
    if (broadcast.objectIDOfTournament!=null
        &&broadcast.objectIDOfTournament!!.isNotEmpty()
        &&broadcast.tournamentExist){
        view.setTextColor(ContextCompat.getColor(view.context, R.color.orange))
        view.text = view.context.getString(R.string.tournament_more)
    } else {
        view.text = view.context.getString(R.string.tournament_more_na)
        view.setTextColor(ContextCompat.getColor(view.context, R.color.text_gray2))
    }
}

@BindingAdapter("setAvatar")
fun setAvatar(view: RelativeLayout, user: user?){
    val waitImage = view.findViewById<ImageView>(R.id.avatarWait)
    val image = view.findViewById<ImageView>(R.id.avatarImage)
    user?.let{
        if (it.avatarUrl!=null){
            waitImage.isVisible = true
            waitImage.post {
                Glide.with(view).asGif().load(R.drawable.wait)
                    .into(waitImage)
            }
            Glide.with(view).load(it.avatarUrl)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        waitImage.visibility = View.GONE
                        return false
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(ObjectKey(it._id!!))
                .into(image)
        } else {
            waitImage.isVisible = true
        }
    }
}

@BindingAdapter("setNick")
fun setNick(view: TextView, user: user?){
    user?.let{
        if (it.nickname==null){
            view.text = view.context.getString(R.string.nick_empty)
        } else view.text = it.nickname
    }
}

@BindingAdapter("setImageResults")
fun setImageResults (view: CardView, resultsOfTournament: resultsOfTournament?){
    resultsOfTournament?.let {
        val waitImage = view.findViewById<ImageView>(R.id.resultsImageWait)
        val image = view.findViewById<ImageView>(R.id.resultsImage)
        if (currentLanguage == "ru" && resultsOfTournament.imageSrc_ru.isNotEmpty()) {
            resultsOfTournament.imageSrc_ru[0]?.let {
                if (resultsOfTournament.imageSrc_ru[0]!="") {
                    waitImage.isVisible = true
                    waitImage.post {
                        Glide.with(view).asGif().load(R.drawable.wait)
                            .into(waitImage)
                    }
                    Glide.with(view).load(it)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                waitImage.visibility = View.GONE
                                return false
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(ObjectKey(it))
                        .into(image)
                } else
                    Glide.with(view).load(R.drawable.item_holder)
                        .into(image)
                waitImage.isVisible = false
            }
        }
        if (currentLanguage == "en" && resultsOfTournament.imageSrc_en.isNotEmpty()) {
            resultsOfTournament.imageSrc_en[0]?.let {
                Log.v("DASD", resultsOfTournament.title?:"")
                Log.v("DASD", it)
                if (resultsOfTournament.imageSrc_en[0]!="") {
                    waitImage.isVisible = true
                    waitImage.post {
                        Glide.with(view).asGif().load(R.drawable.wait)
                            .into(waitImage)
                    }
                    Glide.with(view).load(it)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                waitImage.visibility = View.GONE
                                return false
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(ObjectKey(it))
                        .into(image)
                } else {
                    Glide.with(view).load(R.drawable.item_holder)
                        .into(image)
                    waitImage.isVisible = false
                }
            }
        }
    }
}

@BindingAdapter("setResultsStage")
fun setResultsStage (view: TextView, resultsOfTournament: resultsOfTournament?){
    resultsOfTournament?.let {
        if (currentLanguage == "ru") view.text = resultsOfTournament.stage_ru
        else view.text = resultsOfTournament.stage_en
    }
}

@BindingAdapter("setSimpleDate")
fun setSimpleDate (view: TextView, date: Date?){
    date?.let{
        view.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(date)
    }
}

@BindingAdapter("setFullDate")
fun setFullDate (view: TextView, date: Date?){
    date?.let{
        view.text = SimpleDateFormat("dd.MM.yyyy | HH:mm", Locale.getDefault())
            .format(date)
    }
}

@BindingAdapter("setResultsText")
fun setResultsText (view: TextView, resultsOfTournament: resultsOfTournament?){
    resultsOfTournament?.let {
        if (currentLanguage == "ru") {
            if (resultsOfTournament.text_ru=="")
                view.visibility = View.GONE
            else
                view.text = resultsOfTournament.text_ru
        }
        else {
            if (resultsOfTournament.text_en=="")
                view.visibility = View.GONE
            else
                view.text = resultsOfTournament.text_en
        }
        Linkify.addLinks(view, Linkify.ALL)
    }
}

@BindingAdapter("setImagePartners")
fun setImagePartners (view: CardView, partner: partner?){
    partner?.let {
        val waitImage = view.findViewById<ImageView>(R.id.partnersImageWait)
        val image = view.findViewById<ImageView>(R.id.partnersImage)
        val img =
        if (currentLanguage == "ru") {
            partner.imageSrc_ru
        } else partner.imageSrc_en
        img?.let {
            if (img!="") {
                waitImage.isVisible = true
                waitImage.post {
                    Glide.with(view).asGif().load(R.drawable.wait)
                        .into(waitImage)
                }
                Glide.with(view).load(img)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            waitImage.visibility = View.GONE
                            return false
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .signature(ObjectKey(img))
                    .into(image)
            } else {
                waitImage.isVisible = false
                Glide.with(view).load(R.drawable.item_holder)
                    .into(image)
            }
        }
    }
}

@BindingAdapter("setPartnerDescription")
fun setPartnerDescription (view: TextView, partner: partner?){
    partner?.let {
        val text =
        if (currentLanguage == "ru") partner.descriptionOfPartner_ru
        else partner.descriptionOfPartner_en
        if (text!=null&&text!="") view.text = text
        else view.isVisible = false
    }
}

@BindingAdapter("setPartnerText")
fun setPartnerText (view: LinearLayout, partner: partner?){
    partner?.let {
        val text =
            if (currentLanguage == "ru") partner.text_ru
            else partner.text_en
        if (text!=null&&text!="") view.findViewById<TextView>(R.id.partnerText).text = text
        else view.isVisible = false
    }
}


