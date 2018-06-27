# BabelView

Within this project, we try to instrument an Android APK in order to be able to detect possible flows of sensite information that may happen through the JavaScript Interface. In particular, this would be useful for all those applications implementing the WebView component, allowing the application to run JavaScript and exposing java method to the JavaScript world via the addJavaScriptInterface API.

## Running from BabelView.jar

To run BabelView you will need:

- openjdk 7 or 8. For the experiments we used: `openjdk version "1.8.0_151"`. In principle, there should be no problem using newer jdk version. However, we never used them. 

- Android Jars that you can get from your Android SDK. Assuming that your Android SDK is installed in `$ANDROID_HOME`, you will find the android.jar in `$ANDROID_HOME/platforms`. You can use the `extract_jar.sh` script to properly extract the android.jar. Notice that the androi.jar file have to be sotred in a folder named `andoroid-x` where `x` is the API considered. For example this is the Folder structure you will have:
`Android-Platforms/android-x/android.jar`

- SourcesAndSinks.txt, AndroidCallbacks.txt and EasyTaintWrapperSource.txt files. Make sure this files are in the same folder you run babelview.jar from. You should use the files provided in this repo.

- An Android APK implementing WebView to instrument.

The easiest way to run babelview is trhough this command:
`java -jar BabelView.jar -apk /path/to/test.apk -jars /path/to/Android-Platforms -saveflows /path/to/flow/file -chain`

providing `-chain`  parameter will execute these phases in order:

1. BabelView generation and instrumentation
2. FlowDroid analysis
3. Post analysis on Flowdroid results

The instrumented apk will be placed in `sootOutput/` , and the report will be available in `babelReport/` 


Other useful flags are:
- `-intents` will perform a deeper intent analysis. It may slowdow the overall analysis, but the findings will be much more precise. If not specified, only a true/false report on intent will be generated.

- `-js` will generate a list of vulnerable interfaces represented as json files. You will find them in the `interfaces` fodler which will be generated.
- `-lib` will generate a list of package names for interfaces which are considered dangerous. It can help detect libraries.

- `-luw` activate the load url wrapper analysis, which add a `loadUrl` wrapper method overriding the superclass call. Sometimes `loadUrl()` is called as `super.loadUrl` and we need to take this into consideration. Infact, we won't explicitly find this method, hence our analysis would fail. We then instrument the subclass to explicitly implement this method.

- `-ftimeout` timeout expressed in second to apply to flowdroid analysis.

Alternatively, you can run babelview as a standalone to obtain an instrumented apk and run your own flow analysis on it:
`java -jar BabelView.jar -apk /path/to/test.apk -jars /path/to/Android-Platforms [-luw]`

Similarly, you can run the post analysis as a standlone:
`java -jar BabelView.jar -apk /path/to/test.apk -jars /path/to/Android-Platforms -pa /path/to/flowdroid/analysis.xml`

## Running RAID 2018 experiments

Along with BabelView implementation, we provide the dataset we used to run our experiment for the paper published in RAID 2018 <Link to appear>.

We used apks from AndroZoo (https://androzoo.uni.lu/) for our analysis. `dataset.txt` contains all the SHA256 of the apks we used for our evaluation. To run the whole analysis, please use `run_analysis.sh` that you can find in `scripts/`.

### How to run the whole analysis

Assuming that you downloaded all the apk samples in dataset.txt, `JARS` is the `Android-Platform` directory, `APKS` is the folder containing all the apks, `APK_IDS` is the file with all the apk id listed and that you want a timeout of 600sec; follow these steps:

1. `mkdir $HOME/BabelViewWorkSpace`
2. `cp scrips/run_analysis.sh dataset.txt EasyTaintWrapperSource.txt AndroidCallbacks.txt $HOME/BabelViewWorkSpace SourcesAndSinks.txt`
3. `wget -P $HOME/BabelViewWorkSpace https://github.com/ClaudioRizzo/BabelView/releases/download/v1/BabelView.jar`
4. `./run_analysis.sh APK_IDS APKS JARS 600`

Once `run_analysis` finishes (it may require very long if you run all the 25K sample on one single machine), you can use `/scripts/get_stats.py` to extract the alarms found.


### Notes

- Currenly the post analysis standalone is not very stable and should be avoided.

- Currently, part of the post analysis is implemented in a python script which has to run on the obtained flows. It can be found in the `scripts/` folder. We are planning to integrate this direcly in BabelView to make it easier to use in the future. For an example on how to use it, please refer to RAID2018 experiment section. 


