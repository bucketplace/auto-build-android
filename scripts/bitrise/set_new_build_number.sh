newBuildNumber=$(curl $AUTO_BUILD_ANDROID_SERVER_URL/build_numbers/new?app_version=$APP_VERSION_NAME)

echo $newBuildNumber

envman add --key APP_BUILD_NUMBER --value $newBuildNumber