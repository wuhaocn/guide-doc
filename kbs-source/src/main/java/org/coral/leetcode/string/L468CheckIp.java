package org.coral.leetcode.string;

public class L468CheckIp {
    public static void main(String[] args) {
        L468CheckIp l468CheckIp = new L468CheckIp();
        l468CheckIp.validIPAddress("2001:0db8:85a3:0:0:8A2E:0370:7334:");
    }
    public String validIPAddress(String IP) {

        String[] splitted = IP.split("\\.");
        if(splitted.length==4){
            int num=-1;
            for(int i=0;i<splitted.length;i++){
                try{
                    num=Integer.parseInt(splitted[i]);
                }catch(NumberFormatException e){
                    return "Neither";
                }
                if(num<0||num>255) return "Neither";
                if(splitted[i].length()>1
                        &&(splitted[i].startsWith("0")||splitted[i].startsWith("-")))
                    return "Neither";
            }
            return "IPv4";
        }else{
            splitted = IP.split(":");
            if(splitted.length==8){
                long num=-1;
                for(int i=0;i<splitted.length;i++){
                    if(splitted[i].length()>4) return "Neither";
                    try{
                        num=Long.parseLong(splitted[i],16);
                    }catch(NumberFormatException e){
                        return "Neither";
                    }
                    if(num<0) return "Neither";
                }return "IPv6";
            }else return "Neither";
        }
    }

}
