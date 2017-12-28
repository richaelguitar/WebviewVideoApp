// IWebviewService.aidl
package com.richaelguitar.webviewvideoapp;

// Declare any non-default types here with import statements
import com.richaelguitar.webviewvideoapp.entity.User;
interface IWebviewService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    User getUserInfo();
}
