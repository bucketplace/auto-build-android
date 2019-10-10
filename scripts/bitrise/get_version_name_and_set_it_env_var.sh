appVersionName=$(curl https://api.github.com/repos/bucketplace/ohs-android/contents/app/build.gradle?ref=qa -H "Authorization: token $BSSCCO_GITHUB_ACCCESS_TOKEN" -H "Accept: application/vnd.github.v3.raw" | grep -oE 'versionName ".+"' | grep -oE '[0-9]+.[0-9]+.[0-9]+')

envman add --key APP_VERSION_NAME --value $appVersionName