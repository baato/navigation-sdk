package com.mapbox.services.android.navigation.v5.navigation.telemetry;

import java.util.List;

public interface AttachmentListener {
  void onAttachmentResponse(String message, int code, List<String> fileIds);

  void onAttachmentFailure(String message, List<String> fileIds);
}
