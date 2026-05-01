package com.android.masterdistributormdl.gskDistributor.download;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;


public class DownloadResponse extends ResponseBody {

    private final ResponseBody response;
    private final DownloadListener downloadListener;
    private BufferedSource bufferedSource;


    public DownloadResponse(ResponseBody responseBody, DownloadListener downloadListener) {
        this.response = responseBody;
        this.downloadListener = downloadListener;

    }

    @Override
    public MediaType contentType() {
        return response.contentType();
    }

    @Override
    public long contentLength() {
        return response.contentLength();
    }

    @NonNull
    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(response.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long uploaded = 0L;

            @Override
            public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                long read = super.read(sink, byteCount);
                long length = response.contentLength();
                uploaded += read != -1 ? read : 0;
                int progress = (int) (uploaded * 100 / length);
                Log.d("DownloadUtil", " Progress : " + progress + " Uploaded : " + uploaded + " Length : " + length);
                if (downloadListener != null) {
                    Executor executor = new ThreadExecutor();
                    executor.execute(() -> downloadListener.onProgress(progress));
                }
                return read;
            }
        };
    }
}
