package oric;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class UniformRandomTransport2 implements Transport {
    private static final String PAR_MINDELAY = "mindelay";
    private static final String PAR_MAXDELAY = "maxdelay";
    private long min;
    private long range;

    private long static_min;
    private long static_max;

    public UniformRandomTransport2(String var1) {
        this.static_min = Configuration.getLong(var1 + "." + "mindelay");
        this.static_max = Configuration.getLong( var1 + "." + "maxdelay");


        this.min = Configuration.getLong(var1 + "." + "mindelay");
        long var2 = Configuration.getLong(var1 + "." + "maxdelay", this.min);
        if (var2 < this.min) {
            throw new IllegalParameterException(var1 + "." + "maxdelay", "The maximum latency cannot be smaller than the minimum latency");
        } else {
            this.range = var2 - this.min + 1L;
        }
    }

    public Object clone() {
        return this;
    }

    public void setMinMaxDelay(long min, long max){
        if (max < min) {
            throw new IllegalParameterException("maxdelay", "The maximum latency cannot be smaller than the minimum latency");
        } else {
            this.min = min;
            this.range = max - min + 1L;
        }
    }

    public long getMinDelay(){
        return this.min;
    }

    public long getMaxDelay(){
        return this.min+this.range-1L;
    }

    public long getStatic_min(){
        return this.static_min;
    }
    public long getStatic_max(){
        return this.static_max;
    }



    public void send(Node var1, Node var2, Object var3, int var4) {
        long var5 = this.range == 1L ? this.min : this.min + CommonState.r.nextLong(this.range);
        EDSimulator.add(var5, var3, var2, var4);
    }

    public long getLatency(Node var1, Node var2) {
        return this.range == 1L ? this.min : this.min + CommonState.r.nextLong(this.range);
    }
}
