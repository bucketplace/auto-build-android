kill -9 $(ps x | grep "auto-build-android" | grep -v grep | grep -oE "^[0-9]+")