package com.wt.cloudmedia

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout.LayoutParams.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import cn.jzvd.Jzvd
import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.http.IHttpRequest
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import com.microsoft.onedrivesdk.picker.IPicker
import com.microsoft.onedrivesdk.picker.LinkType
import com.microsoft.onedrivesdk.picker.Picker
import com.wt.cloudmedia.databinding.ActivityMainBinding
import com.wt.cloudmedia.ui.RecyclerViewAdapter


class MainActivity : AppCompatActivity() {
    private var adapter: RecyclerViewAdapter? = null
    private var mAccount: IAccount? = null
    private lateinit var binding: ActivityMainBinding
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private val TAG = MainActivity::class.java.simpleName
    private var authenticationResult: IAuthenticationResult? = null
        set(value) {
            field = value
            value?.let {
                (application as CloudMediaApplication).oneDriveService.setClient(GraphServiceClient.builder().authenticationProvider(object : IAuthenticationProvider {
                    override fun authenticateRequest(request: IHttpRequest) {
                        Log.d(TAG, "Authenticating request," + request.requestUrl)
                        request.addHeader("Authorization", "Bearer ${it.accessToken}")
                    }
                }).buildClient())
                movieViewModel.loadMoves()
            }
        }

    private val picker: IPicker = Picker.createPicker("d985f794-0720-4351-924c-a93994834ae4")

    private val movieViewModel: MovieViewModel by viewModels {
        MovieViewModelFactory((application as CloudMediaApplication).repository)
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
        movieViewModel.allMovie.observe(this) { movies ->
            movies.let { adapter?.addItems(it) }
        }
        PublicClientApplication.createSingleAccountPublicClientApplication(applicationContext,
            R.raw.auth_config_single_account, object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
                    loadAccount()
                }

                override fun onError(exception: MsalException?) {
                }
            })
    }

    private fun initView() {
        binding.login.setOnClickListener {
            mSingleAccountApp?.signIn(this@MainActivity, null, getScopes(), getAuthInteractiveCallback())
        }
        binding.logout.setOnClickListener {
            mSingleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                }

                override fun onError(exception: MsalException) {
                }

            })
        }
        binding.request.setOnClickListener {
            movieViewModel.loadMoves()
        }
        binding.picker.setOnClickListener {
            picker.startPicking(this@MainActivity, LinkType.WebViewLink)
        }
        adapter = RecyclerViewAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}
            override fun onChildViewDetachedFromWindow(view: View) {
                val jzvd: Jzvd = view.findViewById(R.id.videoplayer)
                if (Jzvd.CURRENT_JZVD != null && jzvd.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)) {
                    if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                        Jzvd.releaseAllVideos()
                    }
                }
            }
        })
    }

/*    private fun getFileList(accessToken: String) {
        val client = GraphServiceClient.builder().authenticationProvider(object : IAuthenticationProvider {
            override fun authenticateRequest(request: IHttpRequest) {
                Log.d(TAG, "Authenticating request," + request.requestUrl)
                request.addHeader("Authorization", "Bearer $accessToken")
            }
        }).buildClient()
        val handlerThread = HandlerThread("load")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        handler.post {
            try {
                processItems(client)
            } catch (e: ClientException) {
                e.printStackTrace()
            }
        }
    }*/

    private fun getScreenSize(): IntArray {
        return intArrayOf(this.resources.displayMetrics.widthPixels, this.resources.displayMetrics.widthPixels)
    }

    /*private fun processItems(client: IGraphServiceClient) {
        val children = client.me().drive().root().itemWithPath("aria").children().buildRequest().get()
        val items = ArrayList<DriveItem>()
        children.currentPage.map {
            if (it.folder != null) {
                processFolder(client, it)
            } else if (it.file != null && it.video != null) {
                val item = client.me().drive().items(it.id).buildRequest().get()
                println("lzh.${item.name} ${item.id} ${item.webUrl}")
                val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                runOnUiThread {
                    item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.let { videoUrl ->
                        adapter?.addItem(MediaMetaData(videoUrl, thumbnail.currentPage[0].large.url, item.name))
                    }
                }
            }
        }
    }

    private fun processFolder(client: IGraphServiceClient, folderItem: DriveItem) {
        val result = client.me().drive().items(folderItem.id).children().buildRequest().get().currentPage
        result.forEach {
            if (it.folder != null) {
                processFolder(client, it)
            } else if (it.file != null && it.video != null){
                val item = client.me().drive().items(it.id).buildRequest().get()
                println("lzh.${item.name} ${item.id} ${item.webUrl}")
                val thumbnail = client.me().drive().items(it.id).thumbnails().buildRequest().get()
                runOnUiThread {
                    item.additionalDataManager()["@microsoft.graph.downloadUrl"]?.asString?.let { url ->
                        adapter?.addItem(MediaMetaData(url, thumbnail.currentPage[0].large.url, item.name))
                    }
                }
            }
        }
    }*/

    private fun getScopes(): Array<String> {
        return arrayOf(
            "User.ReadBasic.All",
            "User.Read",
            "User.ReadWrite",
            "User.Read.All",
            "User.ReadWrite.All",
            "Directory.Read.All",
            "Directory.ReadWrite.All",
            "Directory.AccessAsUser.All",
            "Files.ReadWrite",
            "Files.ReadWrite.All",
            "Files.ReadWrite.Selected",
            "Files.ReadWrite.AppFolder")
    }

    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {

            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(TAG, "Successfully authenticated")
                Log.d(TAG, "ID Token: " + authenticationResult.account.claims!!["id_token"])

                /* call graph */
                this@MainActivity.authenticationResult = authenticationResult
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: $exception")

                if (exception is MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception is MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                }
            }

            override fun onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.")
            }
        }
    }

    private fun loadAccount() {
        if (mSingleAccountApp == null) {
            return
        }

        mSingleAccountApp?.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                mAccount = activeAccount
                mAccount?.let {
                    /**
                     * Once you've signed the user in,
                     * you can perform acquireTokenSilent to obtain resources without interrupting the user.
                     */
                    mSingleAccountApp?.acquireTokenSilentAsync(getScopes(), it.authority, getAuthSilentCallback())
                }
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                }
            }

            override fun onError(exception: MsalException) {
            }
        })
    }

    /**
     * Callback used in for silent acquireToken calls.
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     */
    private fun getAuthSilentCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {

            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                Log.d(TAG, "Successfully authenticated")
                this@MainActivity.authenticationResult = authenticationResult
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                //callGraphAPI(authenticationResult.accessToken)
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: $exception")

                if (exception is MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception is MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                } else if (exception is MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                }
            }

            override fun onCancel() {
                /* User cancelled the authentication */
                Log.d(TAG, "User cancelled login.")
            }
        }
    }

}