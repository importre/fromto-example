package io.github.importre.fromtoexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.jetbrains.annotations.NotNull;

import io.github.importre.fromto.FromTo;
import io.github.importre.fromto.FtAction;
import io.github.importre.fromto.FtView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class JavaActivity extends AppCompatActivity implements FtView {

    private static final String TAG = JavaActivity.class.getSimpleName();
    private static Observable<Integer> job1;
    private static Observable<Integer> job2;

    private FromTo fromTo;
    private ProgressBar progress;
    private ProgressBar progress1;
    private ProgressBar progress2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        initUi();

        if (savedInstanceState == null) {
            initObservables();
        }

        FtAction[] actions = initActions();
        fromTo = FromTo.create(actions);
        fromTo.attach(this).execute();
    }

    private FtAction[] initActions() {
        FtAction<Integer> action1 = new FtAction.Builder<Integer>()
                .from(job1)
                .to(new FtAction.Result<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        progress1.setProgress(integer);
                    }
                })
                .done(new FtAction.Done() {
                    @Override
                    public void call() {
                        Log.e(TAG, "done: progress1");
                    }
                })
                .error(new FtAction.Error() {
                    @Override
                    public void call(@NotNull Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
                .build();

        FtAction<Integer> action2 = new FtAction.Builder<Integer>()
                .from(job2)
                .to(new FtAction.Result<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        progress2.setProgress(integer);
                    }
                })
                .done(new FtAction.Done() {
                    @Override
                    public void call() {
                        Log.e(TAG, "done: progress2");
                    }
                })
                .error(new FtAction.Error() {
                    @Override
                    public void call(@NotNull Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
                .build();

        return new FtAction[]{action1, action2};
    }

    private void initObservables() {
        job1 = Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        for (int i = 0; i <= 100; i++) {
                            subscriber.onNext(i);
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();

        job2 = Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        for (int i = 0; i <= 50; i++) {
                            subscriber.onNext(i * 2);
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    @Override
    protected void onDestroy() {
        fromTo.detach();
        super.onDestroy();
    }

    @Override
    public void showLoading(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    private void initUi() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = (ProgressBar) findViewById(R.id.progress);
        progress1 = (ProgressBar) findViewById(R.id.progress1);
        progress2 = (ProgressBar) findViewById(R.id.progress2);

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
            }
        });
    }

    private void restart() {
        fromTo.detach();
        initObservables();
        FtAction[] actions = initActions();
        fromTo.attach(JavaActivity.this).execute(actions);
    }
}
