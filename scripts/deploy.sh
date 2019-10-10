sudo ./gradlew shadowJar
sudo gcloud compute scp build/libs/* instance-1:/home/bsscco/auto-build-android