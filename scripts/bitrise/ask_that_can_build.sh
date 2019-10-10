canBuild=$(curl $AUTO_BUILD_ANDROID_SERVER_URL/builds/can?app_version=$APP_VERSION_NAME)

echo $canBuild

if [ $canBuild == "false" ]
then
  exit 1
fi