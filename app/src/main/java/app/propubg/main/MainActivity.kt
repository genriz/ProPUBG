package app.propubg.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import app.propubg.*
import app.propubg.databinding.ActivityMainBinding
import app.propubg.login.model.UserRealm
import app.propubg.login.ui.StartActivity
import app.propubg.main.broadcasts.adapters.TeamListAdapter
import app.propubg.main.broadcasts.model.broadcast
import app.propubg.main.menu.model.partner
import app.propubg.main.menu.model.resultsOfTournament
import app.propubg.main.news.model.NewsViewModel
import app.propubg.main.news.model.news
import app.propubg.main.news.model.reshuffle
import app.propubg.main.news.ui.FragmentNews
import app.propubg.main.tournaments.model.TournamentItem
import app.propubg.main.tournaments.model.TournamentsViewModel
import app.propubg.main.tournaments.model.tournament
import app.propubg.utils.LocalData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.bson.types.ObjectId
import java.util.*
import kotlin.collections.ArrayList
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    private lateinit var sheetTeamsBehavior: BottomSheetBehavior<*>
    private lateinit var sheetTournamentBehavior: BottomSheetBehavior<*>
    var mixpanelAPI: MixpanelAPI? = null
    private val tournamentsViewModel:TournamentsViewModel by viewModels()
    private val newsViewModel:NewsViewModel by viewModels()

    private val requestForAuth = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()){
        if (it.resultCode==Activity.RESULT_OK){
            currentUserRealm = realmApp.currentUser()
            it.data?.extras?.let{ data ->
                intent.putExtra("screen", data["screen"].toString())
            }
            initUI()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Gson().fromJson(getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("user", null), UserRealm::class.java)
        getSharedPreferences("prefs", Activity.MODE_PRIVATE)
            .getString("language", null)?.let{
                currentLanguage = it
            }

        if (currentUser==null) {
            val intent = Intent(this, StartActivity::class.java)
            intent.putExtra("needAuth", true)
            requestForAuth.launch(intent)
        } else {
            currentUserRealm = realmApp.currentUser()
            initUI()
        }
    }

    private fun initUI() {

        setMixPanel()
        setFCM()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val bottomBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        NavigationUI.setupWithNavController(bottomBar, navController)

        getDynamicLink(intent)

        intent.extras?.let{extras ->
            if (extras.containsKey("screen"))
                processScreen(extras["screen"].toString())
        }

        setupBottomSheetTeams()
        setupBottomSheetTournament()
    }

    private fun setFCM() {
        val fcm = FirebaseMessaging.getInstance()
        if (getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getBoolean("firstStart", true)){
            getSharedPreferences("prefs", MODE_PRIVATE).edit()
                .putBoolean("firstStart", false).apply()
            fcm.unsubscribeFromTopic("news")
            fcm.unsubscribeFromTopic("reshuffle")
            fcm.unsubscribeFromTopic("results")
            fcm.unsubscribeFromTopic("tournaments")
            fcm.unsubscribeFromTopic("broadcasts")
            fcm.unsubscribeFromTopic("informativeContent")
            fcm.unsubscribeFromTopic("newsContent")
            fcm.unsubscribeFromTopic("interviewContent")
        }
        subscribeToTopics(currentLanguage, fcm)
    }

    private fun subscribeToTopics(lang: String, fcm: FirebaseMessaging){
        LocalData.topics.forEach { topic ->
            fcm.subscribeToTopic("$topic$lang")
            getSharedPreferences("prefs", MODE_PRIVATE).edit()
                .putBoolean(topic, true).apply()
        }
    }

    private fun setMixPanel() {
        mixpanelAPI = MixpanelAPI.getInstance(this, "f82f2052da3cc649282f13fd81728714")
        val props = JSONObject()
        props.put("UID", currentUser!!.UID)
        mixpanelAPI?.registerSuperProperties(props)
        mixpanelAPI!!.identify(currentUser!!.UID)
    }

    private fun setupBottomSheetTeams() {
        sheetTeamsBehavior = BottomSheetBehavior.from(binding.bottomSheetTeams.root)
        sheetTeamsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        sheetTeamsBehavior.skipCollapsed = true
        sheetTeamsBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("ClickableViewAccessibility")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState==BottomSheetBehavior.STATE_EXPANDED){
                    binding.dimBack.setOnTouchListener { _, _ ->
                        sheetTeamsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        true
                    }
                } else {
                    binding.dimBack.setOnTouchListener { _, _ -> false }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimBack.alpha = 1+slideOffset
            }
        })
    }

    private fun setupBottomSheetTournament() {
        sheetTournamentBehavior = BottomSheetBehavior.from(binding.bottomSheetTournament.root)
        sheetTournamentBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        sheetTournamentBehavior.skipCollapsed = true
        sheetTournamentBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("ClickableViewAccessibility")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState==BottomSheetBehavior.STATE_EXPANDED){
                    binding.dimBack.setOnTouchListener { _, _ ->
                        sheetTournamentBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        true
                    }
                } else {
                    binding.dimBack.setOnTouchListener { _, _ -> false }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimBack.alpha = 1+slideOffset
            }
        })
    }

    fun openNewsDetails(news: news){
        val bundle = Bundle()
        bundle.putSerializable("newsId", news._id)
        navController.navigate(R.id.action_fragmentNews_to_fragmentNewsDetails, bundle)
    }

    fun openReshufflesDetails(reshuffle: reshuffle){
        val bundle = Bundle()
        bundle.putSerializable("reshuffleId", reshuffle._id)
        navController.navigate(R.id.action_fragmentNews_to_fragmentReshufflesDetails, bundle)
    }

    fun openTournamentDetails(tournament: tournament){
        val bundle = Bundle()
        bundle.putSerializable("tournamentId", tournament._id)
        navController.navigate(R.id.action_fragmentTournaments_to_fragmentTournamentDetails, bundle)

    }

    private fun openNewsScreen(id: ObjectId?){
        newsViewModel.realmReady.observe(this,{
            if (it) {
                val news = newsViewModel.getNewsById(id!!)
                news?.let {
                    ((supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                        .childFragmentManager.fragments[0] as FragmentNews).setPage(1)
                    openNewsDetails(news)
                }
                if (news==null) Toast.makeText(this, "wrong ID",
                    Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun openReshufflesScreen(id: ObjectId?){
        newsViewModel.realmReady.observe(this, {
            if (it) {
                val reshuffle = newsViewModel.getReshuffleById(id!!)
                reshuffle?.let {
                    openReshufflesDetails(reshuffle)
                }
                if (reshuffle==null) Toast.makeText(this, "wrong ID",
                    Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun openTournamentScreen(id: ObjectId?, page: Int){
        val bundle = Bundle()
        bundle.putSerializable("tournamentId", id)
        bundle.putInt("page", page)
        navController.navigate(R.id.fragmentTournaments, bundle)
    }

    private fun openResultsScreen(id: ObjectId?){
        val bundle = Bundle()
        bundle.putSerializable("resultsId", id)
        bundle.putInt("menu", 0)
        navController.navigate(R.id.fragmentMenu, bundle)
    }

    private fun openPartnerScreen(id: ObjectId?){
        val bundle = Bundle()
        bundle.putSerializable("partnerId", id)
        bundle.putInt("menu", 1)
        navController.navigate(R.id.fragmentMenu, bundle)
    }

    private fun openContentScreen(page: Int){
        val bundle = Bundle()
        bundle.putInt("page", page)
        navController.navigate(R.id.fragmentContent, bundle)
    }

    private fun openBroadcastScreen(page: Int){
        val bundle = Bundle()
        bundle.putInt("page", page)
        navController.navigate(R.id.fragmentBroadcasts, bundle)
    }

    fun shareLink(link: String){
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, link)
        i.clipData = ClipData.newRawUri(null, Uri.parse(link))
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(i, "Share URL"))
    }

    fun openFragmentProfileEdit(){
        navController.navigate(R.id.fragmentProfileEdit)
    }

    fun openFragmentPassword(){
        navController.navigate(R.id.action_fragmentMenu_to_fragmentMenuPasswordChange)
    }

    fun openFragmentNotifications(){
        navController.navigate(R.id.action_fragmentMenu_to_fragmentMenuNotifications)
    }

    fun openFragmentLanguage(){
        navController.navigate(R.id.action_fragmentMenu_to_fragmentMenuLanguage)
    }

    fun openFragmentResults() {
        navController.navigate(R.id.action_fragmentMenu_to_fragmentMenuResults)
    }

    fun openResultsDetails(resultsOfTournament: resultsOfTournament) {
        val bundle = Bundle()
        bundle.putSerializable("resultsId", resultsOfTournament._id)
        navController.navigate(R.id.action_fragmentMenuResults_to_fragmentMenuResultsDetails,
            bundle)
    }

    fun openFragmentPartners() {
        navController.navigate(R.id.action_fragmentMenu_to_fragmentMenuPartners)
    }

    fun openPartnerDetails(partner: partner) {
        val bundle = Bundle()
        bundle.putSerializable("partnerId", partner._id)
        navController.navigate(R.id.action_fragmentMenuPartners_to_fragmentMenuPartnerDetails,
            bundle)
    }

    private fun processScreen(screen: String) {
        when (screen){
            "newsOthers" -> ((supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .childFragmentManager.fragments[0] as FragmentNews).setPage(1)
            "tournamentsClosed" -> openTournamentScreen(null, 0)
            "tournamentsOpen" -> openTournamentScreen(null, 1)
            "tournamentsUpcoming" -> openTournamentScreen(null, 2)
            "contentEducational" -> openContentScreen(0)
            "contentInterview" -> openContentScreen(1)
            "broadcastsLive" -> openBroadcastScreen(1)
            "broadcastsUpcoming" -> openBroadcastScreen(2)
            "resultsOfTournaments" -> openFragmentResults()
            "discordPartners" -> openFragmentPartners()
        }
    }

    private fun getDynamicLink(linkIntent: Intent){
        Firebase.dynamicLinks
            .getDynamicLink(linkIntent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                val deepLink: Uri? = pendingDynamicLinkData?.link
                deepLink?.let{
                    val param = it.queryParameterNames.elementAt(0)
                    val id = it.getQueryParameter(param)
                    when (param){
                        "News" ->{
                            openNewsScreen(ObjectId(id))
                        }
                        "Reshuffle" ->{
                            openReshufflesScreen(ObjectId(id))
                        }
                        "Tournament" ->{
                            val tournament = tournamentsViewModel.getTournamentById(ObjectId(id))
                            tournament?.status?.let{status ->
                                when (status){
                                    "Open" -> openTournamentScreen(ObjectId(id),1)
                                    "Closed" -> openTournamentScreen(ObjectId(id),0)
                                    "Upcoming" -> openTournamentScreen(ObjectId(id),2)
                                }
                            }
                        }
                        "ResultsOfTournament"->{
                            openResultsScreen(ObjectId(id))
                        }
                        "Partner"->{
                            openPartnerScreen(ObjectId(id))
                        }
                    }
                }
            }
            .addOnFailureListener(this) {
                    e -> Log.v("DASD", "getDynamicLink:onFailure", e)
            }
    }

    fun closeFragment(){
        navController.navigateUp()
    }

    fun showBottomSheetTeams(broadcast: broadcast){
        val list = ArrayList<String>().apply {
            addAll(broadcast.teamsList)
        }
        val adapter = TeamListAdapter(list)
        binding.bottomSheetTeams.bottomSheetTitle.text = broadcast.title
        if (currentLanguage =="ru"){
            binding.bottomSheetTeams.bottomSheetSubtitle.text = broadcast.stage_ru
        } else {
            binding.bottomSheetTeams.bottomSheetSubtitle.text = broadcast.stage_en
        }
        binding.bottomSheetTeams.recyclerList.adapter = adapter
        binding.bottomSheetTeams.recyclerList.setHasFixedSize(true)
        sheetTeamsBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.bottomSheetTeams.btnContinue.setOnClickListener {
            sheetTeamsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    fun showBottomSheetTournament(id: ObjectId?){
        tournamentsViewModel.realmReady.observe(this,{ ready ->
            if (ready){
                id?.let{
                    val tournament = tournamentsViewModel.getTournamentById(id)
                    tournament?.let {
                        binding.bottomSheetTournament.headerDetails
                            .headerTitle.text = tournament.title

                        binding.bottomSheetTournament.itemWait.postDelayed({
                            binding.bottomSheetTournament.itemWait
                                .visibility= View.VISIBLE
                            Glide.with(binding.bottomSheetTournament.itemWait)
                                .asGif().load(R.drawable.wait)
                                .into(binding.bottomSheetTournament.itemWait)
                        }, 200)

                        Glide.with(binding.bottomSheetTournament.tournamentImage)
                            .load(tournament.imageSrc[0])
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
                                    binding.bottomSheetTournament.itemWait
                                        .visibility= View.GONE
                                    return false
                                }
                            })
                            .into(binding.bottomSheetTournament.tournamentImage)

                        val tournamentItem = TournamentItem()
                        tournamentItem.tournament = tournament
                        binding.bottomSheetTournament.tournamentItem = tournamentItem
                        binding.bottomSheetTournament.executePendingBindings()
                        sheetTournamentBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                        binding.bottomSheetTournament.headerDetails.btnClose.setOnClickListener {
                            onBackPressed()
                        }

                        binding.bottomSheetTournament.headerDetails.btnOption.setImageResource(R.drawable.ic_share)
                        binding.bottomSheetTournament.headerDetails.btnOption.setOnClickListener {
                            Firebase.dynamicLinks.createDynamicLink()
                                .setDomainUriPrefix("https://link.propubg.app")
                                .setLink(Uri.parse("https://link.propubg.app/Tournament/${tournament._id}"))
                                .setSocialMetaTagParameters(
                                    DynamicLink.SocialMetaTagParameters.Builder()
                                        .setImageUrl(Uri.parse(tournament.imageSrc[0]!!))
                                        .setTitle(tournament.title!!)
                                        .build())
                                .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                                .setIosParameters(
                                    DynamicLink.IosParameters
                                        .Builder("ProPUBG").build())
                                .buildShortDynamicLink()
                                .addOnSuccessListener {
                                    shareLink(it.shortLink.toString())
                                }
                                .addOnFailureListener {
                                    Log.v("DASD", it.toString())
                                }
                        }

                        binding.bottomSheetTournament.btnRegister.setOnClickListener {
                            tournament.link?.let{
                                val intent = Intent()
                                intent.action = Intent.ACTION_VIEW
                                intent.data = Uri.parse(it)
                                startActivity(intent)
                            }
                        }

                        binding.bottomSheetTournament.btnInstagram.setOnClickListener {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data = Uri.parse("https://www.instagram.com/propubg.app")
                            startActivity(intent)
                        }

                        binding.bottomSheetTournament.btnTelegram.setOnClickListener {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data = Uri.parse("https://t.me/propubg_app")
                            startActivity(intent)
                        }

                        binding.bottomSheetTournament.txtCopyLink.setOnClickListener {
                            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).apply {
                                setPrimaryClip(ClipData.newPlainText("", tournament.link?:""))
                                Toast.makeText(this@MainActivity,
                                    getString(R.string.link_copied), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        if (sheetTeamsBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetTeamsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            if (sheetTournamentBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                sheetTournamentBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            } else super.onBackPressed()
        }
    }

}