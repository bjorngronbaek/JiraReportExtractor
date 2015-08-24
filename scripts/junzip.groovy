import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader

import java.util.zip.GZIPInputStream

def builder = new CliBuilder(usage: 'unzips a file using the standard JDK ZIP utils.')
def parse = builder.parse(args);

def zipFilePath = parse.arguments()[0]
if(zipFilePath.endsWith('.gzip'))
{
  println 'UnZipping....'

  try
  {
    //copyRead(zipFilePath)
  }
  catch (Exception e)
  {
    println "copyRead failed "+e;
  }

  try
  {
    lineRead(zipFilePath)
  }
  catch (Exception e)
  {
    println "lineRead failed "+e;
  }

}

private void lineRead(String zipFilePath){
  println 'Reading line by line....'
  def zipFile = new File(zipFilePath)
  def fileInputStream = new FileInputStream(zipFile)
  def is = new GZIPInputStream(fileInputStream)

  def reader = new BufferedReader(new UTF8Reader(is))
  while ((line = reader.readLine()) != null)
  {
    //println line;
    if(line.contains('US/Eastern')) println line
  }
}

private void copyRead(String zipFilePath)
{
  println 'Creating byte[] before reading....'
  def os = new ByteArrayOutputStream()

  def zipFile = new File(zipFilePath)
  def fileInputStream = new FileInputStream(zipFile)
  def is = new GZIPInputStream(fileInputStream)

  while ((len = is.read(buffer, 0, buffer.length)) != -1)
  {
    os.write(buffer, 0, len);
  }

  byte[] bytes = os.toByteArray();
  is.close();
  os.close()

  def reader = new BufferedReader(new UTF8Reader(new InputStreamReader(new ByteArrayInputStream(bytes))))
  while ((line = reader.readLine()) != null)
  {
    println line;
  }
}