import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem

object WordCount {
  def main(args: Array[String]) {

    // read values for the command line arguments

    val input: String = try {
      ParameterTool.fromArgs(args).get("input")
    } catch {
      case e: Exception => {
        System.err.println("No input file specified. " +
          "Please run 'WordCount --input <input-file-path> --output <output-file-path>'")
        return
      }
    }

    val output: String = try {
      ParameterTool.fromArgs(args).get("output")
    } catch {
      case e: Exception => {
        System.err.println("No output file specified. " +
          "Please run 'WordCount --input <input-file-path> --output <output-file-path>'")
        return
      }
    }

    // set up the execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment

    // read input data as text file
    val inputData = env.readTextFile(input)

    // perform word count on the input data
    val counts = inputData.flatMap { _.toLowerCase.split("\\W+") }
      .map { (_, 1) }
      .groupBy(0)
      .sum(1)

    // write out the result to a single file
    counts.writeAsCsv(output, "\n", ",", writeMode = FileSystem.WriteMode.OVERWRITE)

    // this emits a single output file
    //counts.writeAsCsv(output, "\n", ",").setParallelism(1)

    // execute the job flow
    env.execute()
  }
}
