# BabelView

This is the implementation of BabelView, accompanying the paper ["BabelView: Evaluating the Impact of Code Injection Attacks in Mobile Webviews"](http://www.cs.rhul.ac.uk/home/kinder/papers/raid18-babelview.pdf), by Claudio Rizzo, Lorenzo Cavallaro, and [Johannes Kinder](http://www.cs.rhul.ac.uk/home/kinder/index.html) of Royal Holloway, University of London. 

BabelView allows to determine the impact of a code injection attack on Webviews in a hybrid Android application. That is, it measures the app-specific attacker capabilities to manipulate or extract personal data through JavaScript interfaces in Webviews, given that an attacker manages to insert malicious JavaScript into a website (e.g., through cross-site scripting or man-in-the-middle injection). Together with assessing the difficulty of the injection attack, this allows developers or store operators to highlight dangerous interface methods in Android Webviews.

BabelView instruments an Android APK such that an information flow analysis (in particular, [FlowDroid](https://github.com/secure-software-engineering/FlowDroid)) will detect flows that can be enabled by malicious JavaScript executing inside the Webview. This does not require any JavaScript support from the flow analysis. BabelView achieves this by inserting a pure Java model of malicious JavaScript interacting with the available JavaScript interfaces.


## Running from BabelView.jar

To run BabelView, you will need:

- OpenJDK 7 or 8. For the experiments in the paper we used `openjdk version "1.8.0_151"`. In principle, newer JDK versions should work, but we have not tested them. 

- Android Jars from the Android SDK. Assuming that your Android SDK is installed in `$ANDROID_HOME`, you will find `android.jar` in `$ANDROID_HOME/platforms`. You can use the `extract_jar.sh` script to extract the required files from `android.jar`. Note that `android.jar` has to be stored in a folder named `android-x` where `x` is the API version considered. The resulting folder structure should be something like `Android-Platforms/android-x/android.jar`

- `SourcesAndSinks.txt`, `AndroidCallbacks.txt`, and `EasyTaintWrapperSource.txt` from this repository. Make sure these files are in the same folder as `BabelView.jar`. 

- A target Android APK implementing a Webview to instrument.

You can run BabelView with the following command:
`java -jar BabelView.jar -apk /path/to/test.apk -jars /path/to/Android-Platforms -saveflows /path/to/flow/file -chain`

providing the `-chain` parameter will execute these phases in order:

1. BabelView generation and instrumentation
2. FlowDroid analysis
3. Post analysis on FlowDroid results

The instrumented APK will be placed in `sootOutput/` , and the report will be available in `babelReport/` 


Other useful flags are:
- `-intents` will perform a deeper intent analysis that precisely determines actions. Without this flag, only a true/false report on intent-related flows will be generated.

- `-js` will generate a list of vulnerable interfaces represented as JSON files in the `interfaces` directory
- `-lib` will generate a list of package names for interfaces that are considered dangerous (e.g., to detect libraries).

- `-luw` activates the load URL wrapper analysis, which adds a `loadUrl` wrapper method overriding the superclass call. Sometimes `loadUrl` is called as `super.loadUrl` and we need to take this into consideration to fully support all occurrences.

- `-ftimeout` timeout in seconds for FlowDroid's information flow analysis.

Alternatively, you can run BabelView standalone to obtain an instrumented APK and run a custom flow analysis:
`java -jar BabelView.jar -apk /path/to/test.apk -jars /path/to/Android-Platforms [-luw]`

Similarly, you can run the post analysis as a standlone:
`java -jar BabelView.jar -apk /path/to/test.apk -jars /path/to/Android-Platforms -pa /path/to/flowdroid/analysis.xml`

## Running RAID 2018 Experiments

`dataset.txt` contains the SHA256 hashes of all APKs we used for the evaluation in our RAID 2018 paper. The APKs themselves can be downloaded from [AndroZoo](https://androzoo.uni.lu/). 

### How to run the whole analysis

Assuming `JARS` is the `Android-Platform` directory, `APKS` is the directory containing the APKs, `APK_IDS` is the list of all APK hashes, and that you want a timeout of 600 seconds, follow these steps:

1. `mkdir $HOME/BabelViewWorkSpace`
2. `scripts/run_analysis.sh dataset.txt EasyTaintWrapperSource.txt AndroidCallbacks.txt $HOME/BabelViewWorkSpace SourcesAndSinks.txt`
3. `wget -P $HOME/BabelViewWorkSpace https://github.com/ClaudioRizzo/BabelView/releases/download/v1/BabelView.jar`
4. `./run_analysis.sh APK_IDS APKS JARS 600`

Once `run_analysis` finishes (when running all 25K samples on a single machine, this can take a while), run `/scripts/get_stats.py` to extract the alarms found.


### Notes

- The standalone post analysis is currently still unstable and should be avoided.

- Part of the post analysis is implemented in `get_stats.py`. We are planning to integrate this direcly in BabelView to make it easier to use in the future.
