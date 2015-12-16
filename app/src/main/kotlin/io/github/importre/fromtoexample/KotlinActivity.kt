package io.github.importre.fromtoexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import io.github.importre.fromto.FromTo
import io.github.importre.fromto.FtAction
import io.github.importre.fromto.FtView
import kotlinx.android.synthetic.main.activity_progress.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

public class KotlinActivity : AppCompatActivity(), FtView {

    companion object {
        private val TAG: String = KotlinActivity::class.java.simpleName
        private lateinit var job1: Observable<Int>
        private lateinit var job2: Observable<Int>
    }

    private lateinit var fromTo: FromTo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        initUi()

        if (savedInstanceState == null) {
            initObservables()
        }

        val actions = initActions()
        fromTo = FromTo.create(actions.toList())
        fromTo.attach(this).execute()
    }

    private fun initActions(): Array<FtAction<*>> {
        val action1 = FtAction.Builder<Int>()
                .from(job1)
                .to { progress1.progress = it }
                .done { Log.e(TAG, "done: progress1") }
                .error { it.printStackTrace() }
                .build()
        val action2 = FtAction.Builder<Int>()
                .from(job2)
                .to { progress2.progress = it }
                .done { Log.e(TAG, "done: progress2") }
                .error { it.printStackTrace() }
                .build()
        return arrayOf(action1, action2)
    }

    private fun initObservables() {
        job1 = Observable
                .create<Int> {
                    for (i in 0..100) {
                        it.onNext(i)
                        Thread.sleep(50)
                    }
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .cache()

        job2 = Observable
                .create<Int> {
                    for (i in 0..50) {
                        it.onNext(i * 2)
                        Thread.sleep(50)
                    }
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .cache()
    }

    override fun onDestroy() {
        fromTo.detach()
        super.onDestroy()
    }

    override fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        restart.setOnClickListener {
            fromTo.detach()
            initObservables()
            val actions = initActions()
            fromTo.attach(this).execute(actions.toList())
        }
    }
}