package com.wt.cloudmedia

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import com.wt.cloudmedia.api.OneDriveService2
import com.wt.cloudmedia.constant.CANCEL
import com.wt.cloudmedia.constant.OK
import com.wt.cloudmedia.db.AppDatabase
import com.wt.cloudmedia.repository.MovieRepository2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class CloudMediaApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    private val appExecutors by lazy { AppExecutors() }
    private val oneDriveService by lazy { OneDriveService2(appExecutors) }
    val repository by lazy { MovieRepository2(appExecutors, database.movieDao(), database.recentMovieDao(), oneDriveService) }
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private var mAccount: IAccount? = null

    private var authenticationResult: IAuthenticationResult? = null
        set(value) {
            field = value
            value?.let {
                oneDriveService.setClient(GraphServiceClient.builder().authenticationProvider { request ->
                    request.addHeader("Authorization", "Bearer ${it.accessToken}")
                }.buildClient())
            }
        }

    override fun onCreate() {
        super.onCreate()
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

    fun getUserName(): MutableLiveData<String> {
        val userName = MutableLiveData<String>()
        mSingleAccountApp?.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                userName.value = activeAccount?.username
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                TODO("Not yet implemented")
            }

            override fun onError(exception: MsalException) {
                TODO("Not yet implemented")
            }
        })
        return userName
    }

    fun login(activity: Activity, callback: (Int, String?) -> Unit) {
        mSingleAccountApp?.signIn(activity, null, getScopes(), object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* call graph */
                this@CloudMediaApplication.authenticationResult = authenticationResult
                callback.invoke(OK, authenticationResult.account.username)
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                if (exception is MsalClientException) {
                    callback.invoke(exception.errorCode.toInt(), exception.message)
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception is MsalServiceException) {
                    callback.invoke(exception.errorCode.toInt(), exception.message)
                    /* Exception when communicating with the STS, likely config issue */
                }
            }

            override fun onCancel() {
                callback.invoke(CANCEL, "User canceled the authentication")
            }
        })
    }

    fun loginOut() {
        mSingleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
            }

            override fun onError(exception: MsalException) {
            }

        })
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

    private fun getAuthSilentCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {

            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                this@CloudMediaApplication.authenticationResult = authenticationResult
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                //callGraphAPI(authenticationResult.accessToken)
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                when (exception) {
                    is MsalClientException -> {
                        /* Exception inside MSAL, more info inside MsalError.java */
                    }
                    is MsalServiceException -> {
                        /* Exception when communicating with the STS, likely config issue */
                    }
                    is MsalUiRequiredException -> {
                        /* Tokens expired or no session, retry with interactive */
                    }
                }
            }

            override fun onCancel() {
                /* User cancelled the authentication */
            }
        }
    }

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

}