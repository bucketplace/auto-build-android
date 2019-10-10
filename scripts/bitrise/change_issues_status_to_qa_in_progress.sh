# multi-line string은 ""로 감싸야 공백과 개행이 올바르게 인식됨.
# 참고: https://www.claudiokuenzler.com/blog/762/bash-multi-line-output-saved-one-line-variable

statusChangedIssues="$(curl -X PUT $AUTO_BUILD_ANDROID_SERVER_URL/issues/status?app_version=$APP_VERSION_NAME)"

echo "$statusChangedIssues"

envman add --key STATUS_CHANGED_ISSUES --value "$statusChangedIssues"