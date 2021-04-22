package oric;

public class MyFileNameGen {
   // private long counter = 0L;
    public final String prefix;
    public final String ext;

    public MyFileNameGen(String var1, String var2) {
        this.prefix = var1;
        this.ext = var2;
    }

    public String nextCounterName(String filename) {
//        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
//        (new PrintStream(var1)).printf("%08d", this.counter);
//        ++this.counter;
        //return this.prefix + var1.toString() + this.ext;

        return this.prefix +  filename + this.ext;
    }
}
