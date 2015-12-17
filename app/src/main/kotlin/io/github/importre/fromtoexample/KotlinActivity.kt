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

        /*
         * Declare [job1] and [job2].
         * They should be static variables.
         * See also [onCreate] and [initObservables].
         */
        private lateinit var job1: Observable<Int>
        private lateinit var job2: Observable<Int>
    }

    private lateinit var fromTo: FromTo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        initUi()

        if (savedInstanceState == null) {
            /*
             * Initialize cached observables when Activity is created.
             * When recreated, [FromTo] will connect to the observables again.
             */
            initObservables()
        }

        // Initialize actions.
        val actions = initActions()

        // Create [FromTo] with actions
        fromTo = FromTo.create(actions.toList())

        // Attach view to [FromTo] and execute
        fromTo.attach(this).execute()
    }

    private fun initActions(): Array<FtAction<*>> {
        val action1 = FtAction.Builder<Int>()
                .from(job1)                             // set from to your observable
                .to { progress1.progress = it }         // invoked when job1.onNext is called
                .done { Log.e(TAG, "done: progress1") } // invoked when job1.onCompleted is called
                .error { it.printStackTrace() }         // invoked when job1.onError is called
                .build()
        val action2 = FtAction.Builder<Int>()
                .from(job2)                             // required
                .to { progress2.progress = it }         // optional
                .done { Log.e(TAG, "done: progress2") } // optional
                .error { it.printStackTrace() }         // optional
                .build()
        return arrayOf(action1, action2)
    }

    private fun initObservables() {
        // YOU SHOULD CALL cache()
        // if you want to show data even though activity is recreated

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
        // detach!
        fromTo.detach()
        super.onDestroy()
    }

    // this method will be invoked by FromTo.actions
    override fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        restart.setOnClickListener { restart() }
    }

    private fun restart() {
        fromTo.detach()
        initObservables()
        val actions = initActions()
        fromTo.attach(this).execute(actions.toList())
    }
}
