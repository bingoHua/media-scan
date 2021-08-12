package com.wt.cloudmedia.api

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.wt.cloudmedia.CloudMediaApplication
import com.wt.cloudmedia.R
import com.wt.cloudmedia.constant.OK
import com.wt.cloudmedia.request.DataResult
import com.wt.cloudmedia.request.ResponseStatus
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

object LoginService {

    private lateinit var mSingleAccountApp: ISingleAccountPublicClientApplication
    private var authenticationResult: IAuthenticationResult? = null

    fun getAuthenticationResult() = authenticationResult

    private fun singleAppBuild() =
        Single.create(SingleOnSubscribe<ISingleAccountPublicClientApplication> { emitter ->
            if (::mSingleAccountApp.isInitialized) {
                emitter.onSuccess(mSingleAccountApp)
            } else {
                mSingleAccountApp = PublicClientApplication.createSingleAccountPublicClientApplication(
                    CloudMediaApplication.instance(),
                    R.raw.auth_config_single_account)
                emitter.onSuccess(mSingleAccountApp)
            }
        })

    private fun slienceLogin(mSingleAccountApp: ISingleAccountPublicClientApplication): Observable<IAuthenticationResult> {
        return Observable.create(ObservableOnSubscribe<IAccount> { emitter ->
            emitter.onNext(mSingleAccountApp.currentAccount?.currentAccount) })
            .flatMap { t ->
                Observable.create { emitter ->
                    t?.also {
                        emitter.onNext(mSingleAccountApp.acquireTokenSilent(getScopes(), it.authority))
                    }?.run {
                        emitter.onComplete()
                    }
                }
            }
    }

    private fun bigLogin(mSingleAccountApp: ISingleAccountPublicClientApplication, activity: Activity): Observable<IAuthenticationResult> {
        return Observable.create { emitter ->
            mSingleAccountApp.signIn(activity, null, getScopes(), object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    /* call graph */
                    emitter.onNext(authenticationResult)
                }

                override fun onError(exception: MsalException) {
                    /* Failed to acquireToken */
                    emitter.onError(exception)
                }

                override fun onCancel() {
                    emitter.onError(Throwable("canceled"))
                }
            })
        }
    }

    fun login(activity: Activity, result: DataResult.Result<IAuthenticationResult>) {
        singleAppBuild().toObservable().flatMap { t ->
            Observable.concat(slienceLogin(t), bigLogin(t, activity))
        }.firstOrError()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<IAuthenticationResult> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: IAuthenticationResult) {
                    authenticationResult = t
                    result.onResult(DataResult(t, ResponseStatus(OK.toString(), true)))
                }

                override fun onError(exception: Throwable) {
                    if (exception is MsalClientException) {
                        result.onResult(DataResult(null, ResponseStatus(exception.message, false)))
                        /* Exception inside MSAL, more info inside MsalError.java */
                    } else if (exception is MsalServiceException) {
                        result.onResult(DataResult(null, ResponseStatus(exception.message, false)))
                        /* Exception when communicating with the STS, likely config issue */
                    }
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