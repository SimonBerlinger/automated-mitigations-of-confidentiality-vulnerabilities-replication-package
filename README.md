# Reproducibility Package for the Masters Thesis "Automated Mitigations of Confidentiality Vulnerabilities using Architectural Runtime Models"

This replication package contains all models used for our evaluation, as well as our implementation. The implementation is a maven project within the `transformation_pipeline_and_mitigation` folder. Systems with their corresponding PCMs are located in the `systems` folder divided into accuracy and scalability.

## Running the Evaluations

To run specified cases for the evaluation, the `de.uulm.masterthesis.eval` package offers an `EvalRunner.java` class, which contains one method for each case of the accuracy evaluations as well as one method for each scalability evaluation. To perform the respective evaluation, uncomment the desired case(s) and run the `main` method of the `EvalRunner`.

### Accuracy

For the accuracy cases, we provide the base DFD model in `systems/accuracy`. Then, for each case the DFD is stored on the root level i a dataflowdiagram and a datadictionary file, which can then be compared to the base case to verify the correct elements have been changed. The expected change is then listed in the accuracy.ods.

### Scalability

The results of each individual time measurement for scalability cases is then created in the `eval_run_output` folder on the top level of the replication package. For scalability runs, the number of repetitions can be set. We used the value 104. Then, in the `raw_data_and_plot_script` folder contains the `ods` table with our original measurements as well as the calculation of the mean value, median, max and min together with a line plot. More beautiful line plots can be generated with the adjacent `plot_csv.py` script using the csv files where the results from the ods sheets have been copied to. To generate a plot, run:

```
python plot_csv.py <file-to-plot>.csv
```

### Re-Generating the Monitoring Records

You can always re-generate the monitoring records by running the system with kieker instrumentation enabled. All of the systems come packaged with the necessary probes to be able to perform this. To regenerate the monitoring records, run:

```
mvn clean compile dependency:copy-dependencies

rm -rf kieker-logs && mkdir -p kieker-logs

CP="target/classes:$(find target/dependency -name '*.jar' | tr '\n' ':')"

java -cp "$CP" \
  -Dkieker.monitoring.enabled=true \
  -Dkieker.monitoring.configuration=kieker.monitoring.properties \
  -Dkieker.monitoring.writer=kieker.monitoring.writer.filesystem.FileWriter \
  -Dkieker.monitoring.writer.filesystem.FileWriter.customStoragePath=kieker-logs \
  edu.kit.kastel.sdq.coupling.casestudy.travelplanner.Main
```

on the same level as the pom.xml of the respective project. For the record scalability evaluation, the amount of repetitions of the run can be specified by giving the application the desired number of repetitions as parameter, i.e. for 100 runs change the last line to 

```
edu.kit.kastel.sdq.coupling.casestudy.travelplanner.Main 100
```
