1，打开eclipse或者myeclipse，导入existing maven projects,选中myshiyan,导入过程可能需要联网；
2，整个项目代码在src/main/java中，com.shiyan.demo和com.shiyan.init是自己的部分；
3，com.shiyan.init是项目的静态参数，RandomConstants.java没用但不可以删掉;
4，重点看com.shiyan.demo中的MyshiyanMain.java文件，其中调用了各种算法；
5，具体算法过程请看com.shiyan.core包中的DatacenterBroker.java类，所有的算法都添加在其中；
6， 实验时，可选择一种算法，将注释去掉，然后右键运行该类即可。