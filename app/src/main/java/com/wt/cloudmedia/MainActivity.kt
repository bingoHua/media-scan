package com.wt.cloudmedia

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.http.IHttpRequest
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import com.wt.cloudmedia.databinding.ActivityMainBinding
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private val TAG = MainActivity::class.java.simpleName
    private var authenticationResult: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
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
            authenticationResult?.let { it1 -> callGraphAPI(it1) }
        }
    }

    private fun getScopes(): Array<String> {
        return arrayOf("user.read","Files.Read")
    }

    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {

            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(TAG, "Successfully authenticated")
                Log.d(TAG, "ID Token: " + authenticationResult.account.claims!!["id_token"])

                /* call graph */
                this@MainActivity.authenticationResult =  authenticationResult.accessToken
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
                this@MainActivity.authenticationResult = (activeAccount as Account).getIdToken()
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

                /* Successfully got a token, use it to call a protected resource - MSGraph */
                callGraphAPI(authenticationResult.accessToken)
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

    /**
     * Make an HTTP request to obtain MSGraph data
     */
/*    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
        MSGraphRequestWrapper.callGraphAPIWithVolley(
            this@MainActivity,
            "https://graph.microsoft.com/v1.0/me",
            authenticationResult.accessToken,
            { response ->
                *//* Successfully called graph, process data and send to UI *//*
                Log.d(TAG, "Response: $response")
            },
            { error ->
                Log.d(TAG, "Error: $error")
            })

    }*/

    private fun callGraphAPI(accessToken: String) {
        val graphClient: IGraphServiceClient = GraphServiceClient
            .builder()
            .authenticationProvider(object : IAuthenticationProvider {
                override fun authenticateRequest(request: IHttpRequest) {
                    Log.d(TAG, "Authenticating request," + request.requestUrl)
                    request.addHeader("Authorization", "Bearer $accessToken")
                }
            })
            .buildClient()
        thread {
            try {
                val result = graphClient.me().drives().buildRequest().get()
            }catch (e : ClientException) {
                e.printStackTrace()
            }
        }

    }
}