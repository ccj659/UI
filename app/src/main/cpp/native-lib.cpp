#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL

Java_com_ccj_ui_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string url = "https://api.smzdm.com/v1/user/info";
    std::string cookieTrue = "sess=MGJkNWJ8MTQ5OTU4MTg3NXw5OTg5NTUwNjYxfDdhMWY1NTc2Yzk0NGZlMGIzMzEzZGEyNWE4OWE0YTIz;ab_test=a;pid=860916030324657;partner_id=72;partner_id=72;device_id=eb53f1ab40a76b4ea4cce23474284d8e;imei=9bdb0ffde8fcfd0a986759203242546b;partner_name=360cpc2;mac=d4:a1:48:8b:27:47;smzdm_id=;login=1;device_push=1;network=wifi;device_smzdm_version=8.0;device_smzdm_version_code=385;device_s=OxCbbWSY2Ap6t536oLjCpLwB70uZ8xwG;device_type=HUAWEIEDI-AL10;device_system_version=7.0;device_smzdm=android;rs_id1=;rs_id2=;rs_id3=;rs_id4=;rs_id5=;smzdm_device=android;smzdm_user_source=OxCbbWSY2Ap6t536oLjCpLwB70uZ8xwG;smzdm_version=8.0;";


    return env->NewStringUTF(url.c_str());
}



/*
Java_com_ccj_ui_MainActivity_stringFromJNI1(
        JNIEnv *env,
jobject */
/* this *//*
) {
std::string url = "https://api.smzdm.com/v1/user/info";
std::string cookieTrue = "sess=MGJkNWJ8MTQ5OTU4MTg3NXw5OTg5NTUwNjYxfDdhMWY1NTc2Yzk0NGZlMGIzMzEzZGEyNWE4OWE0YTIz;ab_test=a;pid=860916030324657;partner_id=72;partner_id=72;device_id=eb53f1ab40a76b4ea4cce23474284d8e;imei=9bdb0ffde8fcfd0a986759203242546b;partner_name=360cpc2;mac=d4:a1:48:8b:27:47;smzdm_id=;login=1;device_push=1;network=wifi;device_smzdm_version=8.0;device_smzdm_version_code=385;device_s=OxCbbWSY2Ap6t536oLjCpLwB70uZ8xwG;device_type=HUAWEIEDI-AL10;device_system_version=7.0;device_smzdm=android;rs_id1=;rs_id2=;rs_id3=;rs_id4=;rs_id5=;smzdm_device=android;smzdm_user_source=OxCbbWSY2Ap6t536oLjCpLwB70uZ8xwG;smzdm_version=8.0;";


return env->NewStringUTF(cookieTrue.c_str());
}
*/
