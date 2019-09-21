daemon=`netstat -tlnp | grep :::18000 | wc -l`
if [ "$daemon" -eq "0" ] ; then
        nohup java -jar /home/bsscco/auto-build-android/auto-build-android-* &
fi